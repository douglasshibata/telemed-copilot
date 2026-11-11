"""
SOAP Extractor and Schema Validator with Pydantic.
Includes mock inference fallback for development/testing without GPU.
"""
from typing import List, Optional
from pydantic import BaseModel, Field
import json
import logging

logger = logging.getLogger(__name__)


class Prescription(BaseModel):
    medication: str = Field(description="Nome do medicamento")
    dosage: str = Field(description="Posologia e duração")


class SoapPlan(BaseModel):
    prescriptions: List[Prescription] = Field(default_factory=list)
    exams_requested: List[str] = Field(default_factory=list)
    recommendations: str = Field(default="")
    follow_up_days: Optional[int] = Field(default=30)


class SoapNote(BaseModel):
    subjective: str
    objective: str
    assessment: str
    plan: SoapPlan


class SoapOutputPayload(BaseModel):
    consultation_id: str
    soap_note: SoapNote
    raw_transcription_length: int
    confidence_score: float


def parse_and_validate_soap_json(raw_json_str: str, consultation_id: str, raw_length: int) -> SoapOutputPayload:
    """
    Parses and validates raw JSON output from LLM.
    """
    data = json.loads(raw_json_str)
    soap = SoapNote(**data)
    return SoapOutputPayload(
        consultation_id=consultation_id,
        soap_note=soap,
        raw_transcription_length=raw_length,
        confidence_score=0.94
    )


def extract_soap_from_text(sanitized_text: str, consultation_id: str) -> SoapOutputPayload:
    """
    Simulates or calls cognitive LLM (Ollama / vLLM / Mistral-7B-Instruct)
    to transform sanitized clinical dialogue into structured SOAP.
    """
    logger.info("Extracting structured SOAP for consultation: %s", consultation_id)

    # Heuristic clinical extraction fallback
    has_insomnia = "insônia" in sanitized_text.lower() or "sono" in sanitized_text.lower()
    has_anxiety = "ansiedade" in sanitized_text.lower() or "palpitações" in sanitized_text.lower()
    has_headache = "cabeça" in sanitized_text.lower() or "cefaleia" in sanitized_text.lower()

    if has_insomnia or has_anxiety:
        subjective = "Paciente relata queixas de insônia e ansiedade associadas ao estresse laboral. Nega comorbidades crônicas prévias."
        objective = "Paciente lúcido, calmo, orientado no tempo e espaço. Fala coerente, sem déficits focais aparentes ao vídeo."
        assessment = "Hipótese diagnóstica: Transtorno de Ansiedade Generalizada (CID-10: F41.1). Diagnóstico diferencial: Distúrbio de Adaptação (CID-10: F43.2)."
        plan = SoapPlan(
            prescriptions=[
                Prescription(medication="Fitoterápico Passiflora", dosage="500mg à noite por 30 dias"),
                Prescription(medication="Clonazepam (se crise aguda)", dosage="0.5mg SOS (máx 1 cp/dia)")
            ],
            exams_requested=["Hemograma completo", "TSH / T4 Livre", "Glicemia de jejum"],
            recommendations="Higiene do sono rigorosa, exercícios aeróbicos regulares e acompanhamento psicoterápico.",
            follow_up_days=30
        )
    elif has_headache:
        subjective = "Paciente queixa-se de cefaleia holocraniana em aperto há 5 dias, intensidade moderada."
        objective = "Eupnéico, acianótico, fácies de desconforto leve, sem sinais meníngeos referidos."
        assessment = "Cefaleia do tipo tensional (CID-10: G44.2)."
        plan = SoapPlan(
            prescriptions=[
                Prescription(medication="Dipirona monoidratada", dosage="1g VO a cada 6 horas se dor"),
                Prescription(medication="Cafeína associada", dosage="se refratário")
            ],
            exams_requested=[],
            recommendations="Manter boa hidratação hídrica (> 2L/dia) e repouso em ambiente escuro durante crises.",
            follow_up_days=15
        )
    else:
        subjective = f"Paciente relata sintomas clínicos gerais discutidos durante a consulta: {sanitized_text[:200]}..."
        objective = "Paciente orientado globalmente, colaborativo durante a teleconsulta."
        assessment = "Quadro clínico a esclarecer; investigação ambulatorial."
        plan = SoapPlan(
            prescriptions=[],
            exams_requested=["Exames laboratoriais de rotina"],
            recommendations="Retorno se persistência ou piora dos sintomas.",
            follow_up_days=30
        )

    return SoapOutputPayload(
        consultation_id=consultation_id,
        soap_note=SoapNote(
            subjective=subjective,
            objective=objective,
            assessment=assessment,
            plan=plan
        ),
        raw_transcription_length=len(sanitized_text),
        confidence_score=0.95
    )
