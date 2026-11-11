"""
Celery Task Definitions for Telemed AI Pipeline.
Consumes consultation.audio.ready, runs STT (faster-whisper), sanitizes PII,
generates structured SOAP with LLM, and publishes to consultation.soap.generated.
"""
import os
import json
import logging
from celery import Celery
from pii_sanitizer import sanitize_pii
from soap_extractor import extract_soap_from_text

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
RABBITMQ_PORT = os.getenv("RABBITMQ_PORT", "5672")
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "guest")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD", "guest")
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = os.getenv("REDIS_PORT", "6379")

broker_url = f"pyamqp://{RABBITMQ_USER}:{RABBITMQ_PASSWORD}@{RABBITMQ_HOST}:{RABBITMQ_PORT}//"
backend_url = f"redis://{REDIS_HOST}:{REDIS_PORT}/0"

app = Celery("telemed_ai_pipeline", broker=broker_url, backend=backend_url)

app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="America/Sao_Paulo",
    enable_utc=True,
)


def mock_stt_transcription(audio_storage_key: str) -> str:
    """
    Simulates speech-to-text (faster-whisper) transcription.
    In real GPU deployments, loads faster_whisper.WhisperModel('large-v3').
    """
    logger.info("Transcribing audio file: %s", audio_storage_key)
    return (
        "Olá doutor, bom dia. Aqui é o paciente Carlos da Silva, CPF 123.456.789-00, "
        "meu telefone é (11) 98765-4321. Há cerca de três semanas venho sofrendo com muita "
        "insônia e palpitações no peito decorrentes de pressão no trabalho. Minha mãe Maria "
        "disse para eu procurar ajuda. Não tomo remédios controlados."
    )


@app.task(name="process_audio_consultation")
def process_audio_consultation(payload: dict) -> dict:
    """
    Main processing workflow:
    1. Ingest audio metadata
    2. Transcribe speech to text
    3. Anonymize PII
    4. Generate structured SOAP
    5. Return JSON payload for publication to consultation.soap.generated
    """
    consultation_id = payload.get("consultation_id")
    audio_key = payload.get("audio_storage_key", "audio.wav")
    logger.info("Started AI pipeline for consultation: %s", consultation_id)

    # 1. Transcribe
    raw_text = mock_stt_transcription(audio_key)

    # 2. Anonymize PII
    sanitized_text = sanitize_pii(raw_text)
    logger.info("Sanitized text: %s", sanitized_text)

    # 3. Extract SOAP with cognitive LLM
    soap_output = extract_soap_from_text(sanitized_text, consultation_id)
    output_dict = soap_output.model_dump()

    # In integration environments, we can also publish directly via pika or celery routing
    logger.info("Successfully generated SOAP for %s", consultation_id)
    return output_dict
