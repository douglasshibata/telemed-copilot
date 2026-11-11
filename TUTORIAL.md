# 📖 Tutorial de Instalação e Execução: Telemed-Copilot

Este tutorial fornece o passo a passo completo para executar a plataforma **Telemed-Copilot** (Copiloto de Atendimento Clínico e Triage com Escuta Ativa para Telemedicina) em ambiente local.

---

## 📋 Arquitetura dos Módulos

O projeto é dividido em quatro componentes principais:
1. **Frontend (`frontend/`)**: Interface web em Vue 3 / Vite com integração de sala de vídeo WebRTC (Jitsi Meet).
2. **Backend API (`backend/`)**: Serviço em Node.js / Express gerador de tokens JWT seguros para autenticação nas salas Jitsi.
3. **AI Pipeline (`ai-pipeline/`)**: Microsserviço Python (FastAPI, LangChain, Pydantic, Whisper) que processa transcrições de áudio, aplica higienização de PII (dados sensíveis do paciente como CPF, RG, telefone) e gera prontuários médicos estruturados no padrão SOAP (Subjetivo, Objetivo, Avaliação, Plano).
4. **Infraestrutura (`infra/`)**: Manifests e Docker Compose para orquestração de containers e serviços de mensageria.

---

## 🛠️ Pré-requisitos

- **Node.js 18+** e npm instalados.
- **Python 3.11+** com pip.
- **Docker** e **Docker Compose** (opcional, para execução de serviços de mensageria e containers).
- Chave de API para LLM (ex: `OPENAI_API_KEY`, `ANTHROPIC_API_KEY` ou instância local Ollama).

---

## 🚀 Passo a Passo de Execução Local

### 1. Configurar e Iniciar o AI Pipeline (Python)

```bash
cd telemed-copilot/ai-pipeline
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Crie o arquivo `.env` na pasta `ai-pipeline`:
```env
OPENAI_API_KEY=sua-chave-aqui
PIPELINE_PORT=8002
ANONYMIZE_PII=true
```

Inicie o servidor do pipeline de IA:
```bash
uvicorn main:app --host 0.0.0.0 --port 8002 --reload
```

### 2. Configurar e Iniciar o Backend (Node.js)

Em outro terminal:
```bash
cd telemed-copilot/backend
npm install
```

Configure as variáveis de ambiente em `backend/.env`:
```env
PORT=3000
JITSI_APP_ID=telemed-copilot
JITSI_APP_SECRET=sua-jitsi-secret-key
AI_PIPELINE_URL=http://localhost:8002
```

Inicie o servidor backend:
```bash
npm run dev
```

### 3. Configurar e Iniciar o Frontend (Vue 3 / Vite)

Em um terceiro terminal:
```bash
cd telemed-copilot/frontend
npm install
npm run dev
```

Acesse a interface no navegador em `http://localhost:5173`.

---

## 🧪 Validando o Fluxo de Geração de Prontuário SOAP

Para testar diretamente o endpoint do copiloto de IA sem passar pela chamada de vídeo:

```bash
curl -X POST http://localhost:8002/api/v1/generate-soap \
  -H "Content-Type: application/json" \
  -d '{
    "consultation_id": "c12345",
    "transcript": "Médico: Olá Sr. João, CPF 123.456.789-00. O que sente hoje? Paciente: Estou com dor de cabeça forte há 3 dias e febre de 38.5 graus. Médico: Vou prescrever dipirona 500mg a cada 6 horas.",
    "patient_metadata": {
      "age": 45,
      "gender": "M"
    }
  }'
```

Resposta esperada:
- Remoção e mascaramento do CPF do paciente antes do envio ao modelo.
- Retorno de estrutura JSON validada:
  ```json
  {
    "subjetivo": "Paciente refere cefaleia de forte intensidade há 3 dias acompanhada de febre...",
    "objetivo": "Temperatura relatada de 38.5C...",
    "avaliacao": "Quadro febril agudo com cefaleia associada...",
    "plano": "Prescrição de dipirona 500mg 6/6h se dor ou febre. Orientação para retorno em caso de piora."
  }
  ```

---

## 🐳 Execução via Docker Compose

Caso prefira rodar a infraestrutura completa conteinerizada:

```bash
cd telemed-copilot/infra
docker compose up -d --build
```
