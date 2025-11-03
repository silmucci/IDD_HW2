import arxiv
import os
import requests
from PyPDF2 import PdfReader

category = "cs.IR"        # File nella categoria Information Retrieval
max_results = 35          # Numero massimo di paper da scaricare
pdf_dir = "file_pdf"      # Directory per salvare i PDF
txt_dir = "file_txt"      # Directory per salvare i file di testo

# Creazione delle directory se non esistono
os.makedirs(pdf_dir, exist_ok=True)
os.makedirs(txt_dir, exist_ok=True)

print(f"Download di {max_results} paper da arXiv ({category})...\n")

# Ricerca paper su arXiv
search = arxiv.Search(
    query=f"cat:{category}",
    max_results=max_results,
    sort_by=arxiv.SortCriterion.SubmittedDate
)
client = arxiv.Client()

for i, result in enumerate(client.results(search), start=1):
    title = result.title.replace("\n", " ").strip()
    pdf_url = result.pdf_url
    pdf_path = os.path.join(pdf_dir, f"paper_{i}.pdf")
    txt_path = os.path.join(txt_dir, f"paper_{i}.txt")

    print(f"[{i}] {title}")

    # Download PDF
    try:
        r = requests.get(pdf_url, timeout=30)
        r.raise_for_status()
        with open(pdf_path, "wb") as f:
            f.write(r.content)
    except Exception as e:
        print(f"Errore nel download di {pdf_url}: {e}\n")
        continue

    # Estrazione testo dal PDF
    text = ""
    try:
        with open(pdf_path, "rb") as f:
            reader = PdfReader(f)
            for page in reader.pages:
                page_text = page.extract_text()
                if page_text:
                    text += page_text + "\n"
    except Exception as e:
        print(f"Errore durante la lettura di {pdf_path}: {e}")
        text = ""

    # Pulizia testo e rimozione del titolo duplicato
    clean_text = text.strip()

    if clean_text.lower().startswith(title.lower()):
        clean_text = clean_text[len(title):].strip()

    # Scrittura su file .txt
    try:
        with open(txt_path, "w", encoding="utf-8") as f:
            f.write(f"{title}\n\n{clean_text}")
        print(f"Salvato in: {txt_path}\n")
    except Exception as e:
        print(f"Errore nel salvataggio di {txt_path}: {e}\n")

print("Operazione completata.")
