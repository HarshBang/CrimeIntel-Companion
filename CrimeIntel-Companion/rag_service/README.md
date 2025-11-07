# CrimeIntel RAG Service

FastAPI-based RAG (Retrieval-Augmented Generation) service for the CrimeIntel Companion Android app.

## Setup

1. **Create virtual environment:**
```bash
python3 -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
```

2. **Install dependencies:**
```bash
pip install -r requirements.txt
```

3. **Install and start Ollama:**
```bash
# Install Ollama from https://ollama.ai
ollama pull phi3
```

4. **Add documents to embed:**
   - Place PDF or TXT files in the `docs/` folder
   - Run the embedding script:
```bash
python embed_docs.py
```

5. **Start / restart the FastAPI server:**
```bash
uvicorn app:app --host 0.0.0.0 --port 8000
```

The service will be available at `http://localhost:8000`

> ℹ️ Use `uvicorn app:app --host 0.0.0.0 --port 8000 --reload` during development so code changes (prompt tuning, limits, etc.) auto-reload.

## API Endpoints

### GET `/`
Health check endpoint.

### GET `/health`
Detailed health check including Ollama and ChromaDB status.

### POST `/embed`
Embed a document text into ChromaDB.

Request:
```json
{
  "doc_text": "Document content here...",
  "doc_id": "unique_document_id"
}
```

Response:
```json
{
  "status": "ok",
  "message": "Document unique_document_id embedded successfully"
}
```

### POST `/ask`
Query the RAG system with a question.

Request:
```json
{
  "query": "How to preserve blood evidence?"
}
```

Response:
```json
{
  "response": "Package wet blood-stained clothes separately in paper bags and label them as soon as possible..."
}
```

**Behavior tuning:**
- Retrieves the top 2 most relevant chunks from ChromaDB (truncated to ~2 KB)
- Prompts Ollama phi3 for a concise 2–4 sentence answer (max ~200 tokens)
- Results longer than ~500 characters are clipped for mobile display

## Android Integration

- **Emulator**: Use `http://10.0.2.2:8000`
- **Physical Device**: Use `http://192.168.x.x:8000` (your local machine's IP)

## Notes

- ChromaDB data is stored in the `db/` directory (included in `.gitignore`)
- The embedding model (`all-MiniLM-L6-v2`) downloads automatically on first use
- Ollama must have the `phi3` model pulled (`ollama pull phi3`) and its service running
- Typical response latency on CPU: **8–12 seconds**; consider GPU or smaller models for faster replies
- Telemetry warnings (`capture() takes 1 positional argument...`) are safe to ignore—they are disabled via `anonymized_telemetry=False`

## Quick sanity test (terminal)

```bash
source .venv/bin/activate
python -c "from app import ask_question, AskRequest;print(ask_question(AskRequest(query='How to preserve blood evidence?')).response)"
```

You should see a short, actionable answer under ~500 characters.

