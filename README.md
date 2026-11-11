# Telemed-Copilot (HealthTech)

> Copiloto de Atendimento Clínico e Triage com Escuta Ativa para Telemedicina.
> Elimina o preenchimento manual de prontuários médicos através do preenchimento automatizado no padrão SOAP (Subjetivo, Objetivo, Avaliação, Plano), higienização de dados pessoais (LGPD/HIPAA) e videoconferência isolada via Jitsi Meet.

---

## 🏗️ Arquitetura do Sistema

```
[ Consulta Online (Jitsi Meet WebRTC) ]
              │ (Stream / Gravação de Áudio Mono 16kHz)
              ▼
    [ MinIO Storage: audio.wav ] ──> [ RabbitMQ: consultation.audio.ready ]
                                                 │
                                                 ▼
                                    [ AI Worker: STT Whisper ]
                                                 │
                                                 ▼
                                    [ Higienizador de PII (Regex/NER) ]
                                                 │
                                                 ▼
                                    [ Worker LLM: Extração SOAP + CID-10 ]
                                                 │
                                                 ▼
                                    [ RabbitMQ: consultation.soap.generated ]
                                                 │
                                                 ▼
        [ Spring Boot Core + WebSocket STOMP (/topic/consultation/{id}/soap) ]
                                                 │
                                                 ▼
                        [ Frontend Angular 19: Split-Screen SOAP ]
                                                 │
                                    (Revisão e Assinatura Médica)
                                                 ▼
                        [ Prontuário Final Gravado + Áudio Purgado (LGPD) ]
```

---

## 📋 Contrato de Dados SOAP

O payload emitido pela inteligência artificial e consumido pela aplicação médica segue a estrutura clínica universal:

```json
{
  "consultation_id": "c7b508f1-325b-4c07-95dc-c2ecad6b7eb8",
  "soap_note": {
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
  },
  "raw_transcription_length": 1420,
  "confidence_score": 0.94
}
```

---

## 🚀 Como Executar o MVP Localmente

### 1. Pré-requisitos
- Docker & Docker Compose
- Java 21 & Maven 3.9+
- Python 3.11+
- Node.js 18+

### 2. Subir a Infraestrutura
```bash
cd telemed-copilot/infra
docker compose up -d
```
Serviços inicializados:
- **PostgreSQL 16:** Porta `5432`
- **Redis 7:** Porta `6379`
- **RabbitMQ 3.12 (com STOMP):** Portas `5672`, `15672`, `61613`
- **MinIO S3:** Portas `9000` (API) e `9001` (Console)
- **Jitsi Meet Web:** Porta `8443`

### 3. Iniciar o Backend (Spring Boot 3 + Java 21)
```bash
cd telemed-copilot/backend
mvn clean spring-boot:run
```
Testes automatizados do backend:
```bash
mvn clean test -f telemed-copilot/backend/pom.xml
```

### 4. Executar os Workers do AI Pipeline (Python)
```bash
cd telemed-copilot/ai-pipeline
pip install -r requirements.txt
python3 -m pytest tests/
```

### 5. Iniciar a Aplicação Frontend (Angular 19)
```bash
cd telemed-copilot/frontend
npm start
```

---

## 🔒 Segurança e Conformidade (LGPD & CFM)
- **Sem gravação de vídeo por padrão:** Preserva a privacidade e intimidade visual do paciente.
- **Higienização de PII antes de LLMs:** Todos os identificadores diretos (CPF, RG, e-mails, telefones e nomes de familiares) são mascarados com tokens como `[PACIENTE]`, `[FAMILIAR]` e `[CPF]`.
- **Expiração / Purga de Mídia:** Ao finalizar e assinar o prontuário pelo médico, o arquivo de áudio original pode ser permanentemente purgado do MinIO/S3.
- **Soberania do Médico:** Nenhuma prescrição ou hipótese diagnóstica é salva como definitiva sem a aprovação ativa e assinatura digital pelo profissional.
