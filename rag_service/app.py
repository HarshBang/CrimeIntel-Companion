"""
FastAPI RAG Service for CrimeIntel Companion
Provides /embed and /ask endpoints for document embedding and RAG queries
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import chromadb
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer
import ollama
import os
from typing import List, Optional

# Initialize FastAPI app
app = FastAPI(title="CrimeIntel RAG Service")

# Enable CORS for Android app
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify exact origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize SentenceTransformer model
print("Loading embedding model...")
embedding_model = SentenceTransformer('all-MiniLM-L6-v2')
print("Embedding model loaded!")

# Initialize ChromaDB
chroma_db_path = os.path.join(os.path.dirname(__file__), "db")
os.makedirs(chroma_db_path, exist_ok=True)

chroma_client = chromadb.PersistentClient(
    path=chroma_db_path,
    settings=Settings(anonymized_telemetry=False)
)

# Get or create collection
collection_name = "crimeintel_docs"
try:
    collection = chroma_client.get_collection(name=collection_name)
    print(f"Loaded existing collection: {collection_name}")
except:
    collection = chroma_client.create_collection(
        name=collection_name,
        metadata={"description": "Crime scene SOPs and IPC documents"}
    )
    print(f"Created new collection: {collection_name}")

# Request/Response models
class EmbedRequest(BaseModel):
    doc_text: str
    doc_id: str

class EmbedResponse(BaseModel):
    status: str
    message: Optional[str] = None

class AskRequest(BaseModel):
    query: str

class AskResponse(BaseModel):
    response: str

@app.get("/")
def root():
    return {"message": "CrimeIntel RAG Service is running", "status": "ok"}

@app.get("/health")
def health():
    """Health check endpoint"""
    try:
        # Check if Ollama is accessible
        ollama.list()  # This will fail if Ollama is not running
        return {
            "status": "healthy",
            "ollama": "connected",
            "chromadb": "connected",
            "collection_count": collection.count()
        }
    except Exception as e:
        return {
            "status": "degraded",
            "ollama": "disconnected",
            "chromadb": "connected",
            "error": str(e)
        }

@app.post("/embed", response_model=EmbedResponse)
def embed_document(request: EmbedRequest):
    """
    Embed a document text and store in ChromaDB
    """
    try:
        # Generate embedding
        embedding = embedding_model.encode(request.doc_text).tolist()
        
        # Store in ChromaDB
        collection.add(
            embeddings=[embedding],
            documents=[request.doc_text],
            ids=[request.doc_id],
            metadatas=[{"source": "manual_upload"}]
        )
        
        return EmbedResponse(
            status="ok",
            message=f"Document {request.doc_id} embedded successfully"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error embedding document: {str(e)}")

@app.post("/ask", response_model=AskResponse)
def ask_question(request: AskRequest):
    """
    Query RAG system: retrieve relevant context and generate response using Ollama
    """
    try:
        # Generate query embedding
        query_embedding = embedding_model.encode(request.query).tolist()
        
        # Retrieve similar documents from ChromaDB
        results = collection.query(
            query_embeddings=[query_embedding],
            n_results=2  # Top 2 most relevant chunks (reduced for faster processing)
        )
        
        # Extract context from results
        if results['documents'] and len(results['documents'][0]) > 0:
            context_chunks = results['documents'][0]
            # Limit context length to avoid overly long prompts
            context = "\n\n".join(context_chunks[:2])
            if len(context) > 2000:  # Truncate if too long
                context = context[:2000] + "..."
        else:
            context = "No relevant documents found in the knowledge base."
        
        # Format prompt for Ollama - optimized for concise responses
        prompt = f"""You are a forensic assistant helping law enforcement at crime scenes. Answer concisely and clearly.

Context from documents:
{context}

Question: {request.query}

Provide a brief, practical answer (2-4 sentences max). Focus on actionable steps:"""
        
        # Query Ollama with response length limits
        try:
            response = ollama.generate(
                model='phi3',
                prompt=prompt,
                options={
                    'temperature': 0.3,
                    'top_p': 0.9,
                    'num_predict': 200,  # Limit response to ~200 tokens for faster, concise answers
                }
            )
            answer = response['response']
            # Truncate if still too long (safety check)
            if len(answer) > 500:
                answer = answer[:500] + "..."
        except Exception as ollama_error:
            # Fallback if Ollama is not available
            answer = f"Ollama service error: {str(ollama_error)}. Context retrieved: {context[:200]}..."
        
        return AskResponse(response=answer)
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing query: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

