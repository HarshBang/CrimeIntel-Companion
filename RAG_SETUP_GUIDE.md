# RAG Virtual Assistant Setup Guide

This guide will help you set up the RAG (Retrieval-Augmented Generation) Virtual Assistant for the CrimeIntel Companion Android app.

## Architecture Overview

The RAG system consists of two parts:
1. **Python Backend Service** (`rag_service/`) - Runs on your local machine or server
2. **Android App** - Connects to the backend via HTTP REST API

## Prerequisites

- Python 3.8 or higher
- Ollama installed on your machine ([Download from ollama.ai](https://ollama.ai))
- Android Studio (for building/running the app)
- Local network connection (WiFi/LAN) between Android device and machine running the service

## Step 1: Setup Python Backend Service

### 1.1 Navigate to RAG Service Directory
```bash
cd CrimeIntel-Companion/rag_service
```

### 1.2 Run Setup Script (Mac/Linux)
```bash
./setup.sh
```

Or manually:
```bash
# Create virtual environment
python3 -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
```

### 1.3 Install and Setup Ollama

1. **Install Ollama** from [https://ollama.ai](https://ollama.ai)

2. **Pull the phi3 model:**
```bash
ollama pull phi3
```

3. **Verify Ollama is running:**
```bash
ollama list
```

### 1.4 Add Documents to Embed

1. Place your PDF or TXT files in `rag_service/docs/` folder
   - Example: `Indian_Penal_Code_Sections.pdf`
   - Example: `Forensic_Procedures_Guidelines.pdf`
   - Example: `Crime_Scene_Checklist.txt`

2. **Run the embedding script:**
```bash
python embed_docs.py
```

This will:
- Extract text from PDFs and text files
- Chunk documents into smaller pieces
- Generate embeddings using SentenceTransformer
- Store embeddings in ChromaDB

Expected output: 2000-5000 embeddings stored in ChromaDB

### 1.5 Start the FastAPI Server

```bash
# Make sure virtual environment is activated
source .venv/bin/activate  # On Windows: .venv\Scripts\activate

# Start the server
uvicorn app:app --host 0.0.0.0 --port 8000
```

The service will be available at:
- `http://localhost:8000` (on your machine)
- `http://192.168.x.x:8000` (from other devices on your network)

### 1.6 Test the Backend

Open a browser or use curl:
```bash
# Health check
curl http://localhost:8000/health

# Test query
curl -X POST http://localhost:8000/ask \
  -H "Content-Type: application/json" \
  -d '{"query": "How to preserve blood evidence?"}'
```

## Step 2: Configure Android App

### 2.1 Find Your Local Machine's IP Address

**On Mac/Linux:**
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

**On Windows:**
```cmd
ipconfig
```
Look for IPv4 address (e.g., `192.168.1.100`)

### 2.2 Update RAG Service URL in Android App

**Option 1: Update Default URL (Quick)**
Edit `RagChatActivity.java`:
```java
private static final String DEFAULT_DEVICE_URL = "http://YOUR_IP_HERE:8000";
```

**Option 2: Use SharedPreferences (Programmatic)**
The app automatically detects emulator vs physical device:
- **Emulator**: Uses `http://10.0.2.2:8000` (maps to host's localhost)
- **Physical Device**: Uses `DEFAULT_DEVICE_URL` or SharedPreferences value

### 2.3 Build and Run Android App

1. Open the project in Android Studio
2. Build the project
3. Run on emulator or physical device

**Important:** Ensure your Android device/emulator is on the same network as the machine running the RAG service.

## Step 3: Test the Integration

1. **Start the RAG service** on your local machine (Step 1.5)
2. **Launch the Android app**
3. **Navigate to Home screen** → Click "Virtual Assistant" card
4. **Ask a question** like:
   - "How to preserve blood evidence?"
   - "What are the steps for chain of custody?"
   - "Explain IPC section 302"

The app should:
- Connect to the RAG service
- Retrieve relevant context from embedded documents
- Generate a response using Ollama (phi3 model)
- Display the answer in the chat interface

## Troubleshooting

### Backend Issues

**Problem:** `Connection refused` or `Failed to connect`
- **Solution:** Ensure FastAPI server is running on port 8000
- Check firewall settings allow connections on port 8000
- Verify `--host 0.0.0.0` is used (not just `localhost`)

**Problem:** `Ollama service error`
- **Solution:** Ensure Ollama is installed and running
- Verify `phi3` model is pulled: `ollama pull phi3`
- Test Ollama directly: `ollama run phi3 "Hello"`

**Problem:** `No relevant documents found`
- **Solution:** Run `python embed_docs.py` to embed documents
- Check that documents exist in `docs/` folder
- Verify ChromaDB has data: Check `db/` folder exists

### Android App Issues

**Problem:** App can't connect to RAG service
- **Solution:** 
  - Verify IP address is correct
  - Ensure device and machine are on same network
  - For emulator, use `10.0.2.2:8000`
  - For physical device, use your machine's local IP (e.g., `192.168.1.100:8000`)

**Problem:** Timeout errors
- **Solution:** Increase timeout in `RagChatActivity.java` (currently 30 seconds)
- Check network connectivity
- Verify RAG service is responding: Test with curl/Postman

**Problem:** Empty responses
- **Solution:** 
  - Check RAG service logs for errors
  - Verify documents are embedded in ChromaDB
  - Test `/ask` endpoint directly with curl

## API Endpoints Reference

### GET `/health`
Health check endpoint
```bash
curl http://localhost:8000/health
```

### POST `/ask`
Query the RAG system
```bash
curl -X POST http://localhost:8000/ask \
  -H "Content-Type: application/json" \
  -d '{"query": "Your question here"}'
```

### POST `/embed`
Manually embed a document
```bash
curl -X POST http://localhost:8000/embed \
  -H "Content-Type: application/json" \
  -d '{"doc_text": "Document content...", "doc_id": "doc_001"}'
```

## Next Steps

- Add more documents to `docs/` folder and re-run `embed_docs.py`
- Customize the prompt in `app.py` for better responses
- Adjust chunk size and overlap in `embed_docs.py` for better retrieval
- Consider adding authentication for production use
- Implement Phase 2 (TensorFlow Lite) for true offline capability

## File Structure

```
CrimeIntel-Companion/
├── rag_service/
│   ├── app.py                 # FastAPI application
│   ├── embed_docs.py          # Document embedding script
│   ├── requirements.txt       # Python dependencies
│   ├── setup.sh               # Setup script
│   ├── README.md              # RAG service documentation
│   ├── docs/                  # Place PDFs/TXT files here
│   └── db/                    # ChromaDB storage (auto-created)
└── app/
    └── src/main/java/com/example/crimeintelcompanion/
        └── RagChatActivity.java  # Android RAG chat activity
```

## Support

For issues or questions, refer to:
- RAG Service README: `rag_service/README.md`
- Main Project README: `README.md`

