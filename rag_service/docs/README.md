# Documents Folder

Place your reference documents here for embedding into the RAG system.

## Supported Formats
- **PDF files** (.pdf) - Forensic procedures, IPC sections, SOPs
- **Text files** (.txt) - Checklists, guidelines, notes

## Example Documents
- `Indian_Penal_Code_Sections.pdf` - Relevant IPC sections
- `Forensic_Procedures_Guidelines.pdf` - Standard forensic procedures
- `Crime_Scene_Checklist.txt` - Crime scene documentation checklist
- `Chain_of_Custody_SOP.pdf` - Chain of custody procedures

## How to Add Documents
1. Copy your PDF or TXT files into this `docs/` folder
2. Run the embedding script: `python embed_docs.py`
3. The documents will be chunked, embedded, and stored in ChromaDB
4. Restart the FastAPI server to use the new embeddings

## Notes
- Large PDFs will be automatically chunked into smaller pieces
- Each chunk is embedded separately for better retrieval
- Documents are indexed by filename and chunk index

