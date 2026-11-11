"""
Clinical Prompts for SOAP Extraction.
Instructs the Cognitive LLM to extract Subjective, Objective, Assessment, and Plan
formatted in rigorous JSON schema.
"""

SOAP_SYSTEM_PROMPT = """Você é um assistente médico especialista em triagem clínica e preenchimento de prontuários eletrônicos no padrão SOAP (Subjetivo, Objetivo, Avaliação, Plano).

Diretrizes rigorosas:
1. Ignore conversas triviais ("Como foi o trânsito?", "O tempo esfriou", saudações e amenidades).
2. Foco exclusivo em:
   - SUBJETIVO: queixas principais, história da moléstia atual, sintomas, duração, intensidade e histórico relatado.
   - OBJETIVO: sinais vitais mencionados, achados do exame clínico visual/informado, estado geral, fala, orientação têmporo-espacial.
   - AVALIAÇÃO: hipótese diagnóstica primária com código CID-10 e diagnósticos diferenciais plausíveis.
   - PLANO: prescrições (medicamento e posologia), exames solicitados, orientações comportamentais/recomendações e tempo de retorno em dias.
3. Não invente sintomas ou medicamentos não discutidos.
4. Responda ESTRITAMENTE em formato JSON compatível com o schema definido.
"""

SOAP_SCHEMA_EXAMPLE = {
    "subjective": "Paciente relata episódios recorrentes de insônia e palpitações há 3 semanas, associados a sobrecarga no trabalho. Nega uso de medicamentos prévios.",
    "objective": "Paciente lúcido, orientado no tempo e espaço. Fala acelerada, sem sinais de confusão mental. Frequência cardíaca informada pelo paciente: 88 bpm.",
    "assessment": "Hipótese diagnóstica principal: Transtorno de Ansiedade Generalizada (CID-10: F41.1). Diagnóstico diferencial: Síndrome de Burnout.",
    "plan": {
        "prescriptions": [
            {"medication": "Fitoterápico / Passiflora incarnata", "dosage": "500mg à noite por 30 dias"}
        ],
        "exams_requested": ["Hemograma completo", "TSH e T4 Livre"],
        "recommendations": "Prática de higiene do sono; encaminhamento para psicoterapia.",
        "follow_up_days": 30
    }
}
