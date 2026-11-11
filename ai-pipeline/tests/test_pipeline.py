import pytest
import os
import sys

# Add ai-pipeline directory to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from pii_sanitizer import sanitize_pii
from soap_extractor import extract_soap_from_text, parse_and_validate_soap_json
from tasks import process_audio_consultation


def test_pii_sanitization():
    raw_text = (
        "Meu nome é João da Silva, meu CPF é 123.456.789-00 e meu email é joao.silva@email.com. "
        "Moro com minha mãe Maria e meu telefone é 11 98888-7777. Estou com insônia há dias."
    )
    sanitized = sanitize_pii(raw_text)

    assert "[CPF]" in sanitized
    assert "123.456.789-00" not in sanitized

    assert "[EMAIL]" in sanitized
    assert "joao.silva@email.com" not in sanitized

    assert "[TELEFONE]" in sanitized
    assert "98888-7777" not in sanitized

    assert "[FAMILIAR]" in sanitized
    assert "Maria" not in sanitized

    # Clinical symptom should remain intact
    assert "insônia" in sanitized


def test_soap_extraction_structure():
    text = "Paciente com insônia e palpitações há 3 semanas."
    output = extract_soap_from_text(text, "consult-test-123")

    assert output.consultation_id == "consult-test-123"
    assert output.soap_note.subjective is not None
    assert output.soap_note.objective is not None
    assert "Ansiedade" in output.soap_note.assessment
    assert len(output.soap_note.plan.prescriptions) > 0
    assert output.soap_note.plan.follow_up_days == 30
    assert output.confidence_score >= 0.90


def test_parse_and_validate_soap_json():
    json_sample = """
    {
        "subjective": "Dor de cabeça persistente",
        "objective": "Normotenso",
        "assessment": "Cefaleia tensional",
        "plan": {
            "prescriptions": [
                {"medication": "Dipirona", "dosage": "500mg 6/6h"}
            ],
            "exams_requested": ["Tomografia"],
            "recommendations": "Repouso",
            "follow_up_days": 7
        }
    }
    """
    res = parse_and_validate_soap_json(json_sample, "c-1", 100)
    assert res.soap_note.subjective == "Dor de cabeça persistente"
    assert res.soap_note.plan.prescriptions[0].medication == "Dipirona"


def test_process_audio_consultation_task():
    payload = {
        "consultation_id": "c-test-999",
        "audio_storage_key": "consultation-999.wav"
    }
    res = process_audio_consultation(payload)
    assert res["consultation_id"] == "c-test-999"
    assert "soap_note" in res
    assert "subjective" in res["soap_note"]
    assert "plan" in res["soap_note"]
