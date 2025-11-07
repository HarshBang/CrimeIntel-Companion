"""
Script to embed documents from docs/ folder into ChromaDB
Processes PDFs and text files, chunks them, and stores embeddings
"""

import os
import chromadb
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer
import PyPDF2
from typing import List, Tuple
import hashlib

# Initialize models and database
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
    print(f"Using existing collection: {collection_name}")
except:
    collection = chroma_client.create_collection(
        name=collection_name,
        metadata={"description": "Crime scene SOPs and IPC documents"}
    )
    print(f"Created new collection: {collection_name}")

def extract_text_from_pdf(pdf_path: str) -> str:
    """Extract text from PDF file"""
    try:
        with open(pdf_path, 'rb') as file:
            pdf_reader = PyPDF2.PdfReader(file)
            text = ""
            for page in pdf_reader.pages:
                text += page.extract_text() + "\n"
            return text
    except Exception as e:
        print(f"Error reading PDF {pdf_path}: {e}")
        return ""

def extract_text_from_txt(txt_path: str) -> str:
    """Extract text from text file"""
    try:
        with open(txt_path, 'r', encoding='utf-8') as file:
            return file.read()
    except Exception as e:
        print(f"Error reading text file {txt_path}: {e}")
        return ""

def chunk_text(text: str, chunk_size: int = 500, overlap: int = 100) -> List[str]:
    """
    Split text into chunks with overlap
    Uses simple word-based chunking
    """
    words = text.split()
    chunks = []
    
    if len(words) <= chunk_size:
        return [text]
    
    i = 0
    while i < len(words):
        chunk = ' '.join(words[i:i + chunk_size])
        chunks.append(chunk)
        i += chunk_size - overlap
    
    return chunks

def generate_doc_id(source_file: str, chunk_index: int) -> str:
    """Generate unique document ID"""
    base_name = os.path.basename(source_file)
    return f"{base_name}_chunk_{chunk_index}"

def process_document(file_path: str) -> Tuple[int, str]:
    """
    Process a single document (PDF or TXT) and embed it
    Returns: (number_of_chunks, source_filename)
    """
    file_ext = os.path.splitext(file_path)[1].lower()
    source_filename = os.path.basename(file_path)
    
    # Extract text
    if file_ext == '.pdf':
        text = extract_text_from_pdf(file_path)
    elif file_ext == '.txt':
        text = extract_text_from_txt(file_path)
    else:
        print(f"Skipping unsupported file type: {file_path}")
        return (0, source_filename)
    
    if not text.strip():
        print(f"No text extracted from {file_path}")
        return (0, source_filename)
    
    # Chunk the text
    chunks = chunk_text(text, chunk_size=500, overlap=100)
    print(f"Processing {source_filename}: {len(chunks)} chunks")
    
    # Generate embeddings and store
    embeddings = []
    documents = []
    ids = []
    metadatas = []
    
    for idx, chunk in enumerate(chunks):
        embedding = embedding_model.encode(chunk).tolist()
        doc_id = generate_doc_id(file_path, idx)
        
        embeddings.append(embedding)
        documents.append(chunk)
        ids.append(doc_id)
        metadatas.append({
            "source_file": source_filename,
            "chunk_index": idx,
            "total_chunks": len(chunks)
        })
    
    # Batch add to ChromaDB
    if embeddings:
        collection.add(
            embeddings=embeddings,
            documents=documents,
            ids=ids,
            metadatas=metadatas
        )
        print(f"✓ Embedded {len(chunks)} chunks from {source_filename}")
    
    return (len(chunks), source_filename)

def main():
    """Main function to process all documents in docs/ folder"""
    docs_folder = os.path.join(os.path.dirname(__file__), "docs")
    
    if not os.path.exists(docs_folder):
        print(f"Creating docs folder: {docs_folder}")
        os.makedirs(docs_folder, exist_ok=True)
        print("Please add PDF or TXT files to the docs/ folder and run this script again.")
        return
    
    # Get all PDF and TXT files
    pdf_files = [f for f in os.listdir(docs_folder) if f.lower().endswith('.pdf')]
    txt_files = [f for f in os.listdir(docs_folder) if f.lower().endswith('.txt')]
    all_files = pdf_files + txt_files
    
    if not all_files:
        print(f"No PDF or TXT files found in {docs_folder}")
        print("Please add documents to embed.")
        return
    
    print(f"\nFound {len(all_files)} document(s) to process:")
    for f in all_files:
        print(f"  - {f}")
    
    total_chunks = 0
    processed_files = []
    
    for filename in all_files:
        file_path = os.path.join(docs_folder, filename)
        chunks, source = process_document(file_path)
        total_chunks += chunks
        processed_files.append(source)
    
    print(f"\n{'='*50}")
    print(f"Embedding complete!")
    print(f"Total chunks embedded: {total_chunks}")
    print(f"Files processed: {len(processed_files)}")
    print(f"Collection now contains {collection.count()} documents")
    print(f"{'='*50}")

if __name__ == "__main__":
    main()

