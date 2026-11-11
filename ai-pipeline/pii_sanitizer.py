"""
PII Sanitizer for Telemedicine Transcription.
Removes or masks Personally Identifiable Information (CPF, phone numbers,
emails, patient and relative names) while preserving clinical symptoms and observations.
"""
import re
from typing import Dict, Any

CPF_REGEX = re.compile(r'\b\d{3}\.?\d{3}\.?\d{3}-?\d{2}\b')
PHONE_REGEX = re.compile(r'\b(?:\+?55\s?)?(?:\(?\d{2}\)?\s?)?(?:9\d{4}|\d{4})[-\s]?\d{4}\b')
EMAIL_REGEX = re.compile(r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b')

# Pattern to mask introductory greetings containing names like "Meu nome é João da Silva", "Aqui é o paciente Carlos"
PATIENT_INTRO_REGEX = re.compile(
    r'(?i)\b(?:meu nome é|eu sou o|eu sou a|aqui é o|aqui é a|paciente)\s+([A-ZÁÉÍÓÚÂÊÔÃÕ][a-záéíóúâêôãõç]+(?:\s+[A-ZÁÉÍÓÚÂÊÔÃÕ][a-záéíóúâêôãõç]+)*)'
)

# Relative / contact patterns like "minha mãe Maria", "meu esposo Pedro"
RELATIVE_REGEX = re.compile(
    r'(?i)\b(minha mãe|meu pai|meu filho|minha filha|meu esposo|minha esposa|meu marido|minha mulher)\s+([A-ZÁÉÍÓÚÂÊÔÃÕ][a-záéíóúâêôãõç]+)'
)


def sanitize_pii(text: str) -> str:
    """
    Sanitizes PII from transcription text.
    """
    if not text:
        return ""

    sanitized = text

    # Mask CPF
    sanitized = CPF_REGEX.sub("[CPF]", sanitized)

    # Mask Email
    sanitized = EMAIL_REGEX.sub("[EMAIL]", sanitized)

    # Mask Phone
    sanitized = PHONE_REGEX.sub("[TELEFONE]", sanitized)

    # Mask relative names
    def replace_relative(match):
        relation = match.group(1)
        return f"{relation} [FAMILIAR]"

    sanitized = RELATIVE_REGEX.sub(replace_relative, sanitized)

    # Mask patient intro names
    def replace_patient(match):
        prefix = match.group(0).split()[0] + " " + match.group(0).split()[1] if len(match.group(0).split()) >= 2 else "paciente"
        return f"{prefix} [PACIENTE]"

    sanitized = PATIENT_INTRO_REGEX.sub(replace_patient, sanitized)

    return sanitized
