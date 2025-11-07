#!/bin/bash

# Setup script for RAG Service
# This script sets up the Python environment and installs dependencies

echo "Setting up CrimeIntel RAG Service..."

# Create virtual environment if it doesn't exist
if [ ! -d ".venv" ]; then
    echo "Creating Python virtual environment..."
    python3 -m venv .venv
fi

# Activate virtual environment
echo "Activating virtual environment..."
source .venv/bin/activate

# Upgrade pip
echo "Upgrading pip..."
pip install --upgrade pip

# Install dependencies
echo "Installing dependencies..."
pip install -r requirements.txt

echo ""
echo "Setup complete!"
echo ""
echo "Next steps:"
echo "1. Install Ollama from https://ollama.ai"
echo "2. Pull the phi3 model: ollama pull phi3"
echo "3. Add documents to the docs/ folder"
echo "4. Run: python embed_docs.py (to embed documents)"
echo "5. Start the server: uvicorn app:app --host 0.0.0.0 --port 8000"
echo ""
echo "To activate the virtual environment in the future, run:"
echo "  source .venv/bin/activate"

