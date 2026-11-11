# AGENTS.md

Este arquivo fornece instruções operacionais para agentes e desenvolvedores trabalhando no projeto **telemed-copilot**.

## Visão Geral do Projeto
Copiloto de Atendimento Clínico e Triage com Escuta Ativa (HealthTech) para telemedicina com geração automatizada de prontuário eletrônico no padrão SOAP (Subjetivo, Objetivo, Avaliação, Plano), transcrição de áudio, anonimização de dados sensíveis (LGPD/HIPAA) e conferência isolada WebRTC via Jitsi Meet.

## Estrutura de Pastas
- `infra/`: Orquestração Docker Compose com PostgreSQL 16, Redis 7, RabbitMQ 3.12 (com STOMP), MinIO (S3 compatível) e Jitsi Meet com autenticação JWT.
- `backend/`: API Spring Boot 3.3.4 (Java 21) com persistência JPA, WebSocket STOMP, geração de tokens JWT seguros para Jitsi e listeners RabbitMQ.
- `ai-pipeline/`: Workers Celery em Python para transcrição de áudio (faster-whisper), mascaramento rigoroso de PII (`pii_sanitizer.py`) e estruturação SOAP validada via Pydantic.
- `frontend/`: Interface médica em Angular 19 com split-screen (60% conferência de vídeo e 40% editor reativo de prontuário SOAP).

## Comandos Principais
- **Backend:** `mvn clean test -f telemed-copilot/backend/pom.xml` ou `mvn spring-boot:run -f telemed-copilot/backend/pom.xml`
- **AI Pipeline:** `python3 -m pytest telemed-copilot/ai-pipeline/tests/`
- **Infra:** `docker compose -f telemed-copilot/infra/docker-compose.yml up -d`
- **Frontend:** `cd telemed-copilot/frontend && npm start`

## Diretrizes Regulatórias e de Privacidade
1. **Anonimização de PII Obrigatória:** Nomes, CPFs, telefones e emails devem ser sanitizados antes de qualquer envio a modelos de linguagem externos.
2. **TTL de Áudio Original:** Após a aprovação e assinatura do prontuário pelo médico (`APPROVED_AND_SIGNED`), o registro original de áudio é purgado (`audioPurgedAt`) conforme normas da LGPD.
3. **Controle Médico:** Nenhuma prescrição ou prontuário é gravado como definitivo sem a aprovação explícita e assinatura digital do profissional de saúde.
