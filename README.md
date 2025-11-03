Homework 2 – Ingegneria dei Dati 2025/2026

Progetto Java che utilizza Apache Lucene per indicizzare e ricercare file di testo in formato `.txt`
Il sistema consente di eseguire query da console sui campi `title` e `content`, restituendo i risultati più pertinenti ordinati per punteggio TF–IDF.

Struttura del progetto:
- `download_txt.py` → scarica automaticamente i paper in PDF da arXiv ed estrae il testo in formato `.txt`  
- `TXTIndexer.java` → crea l’indice Lucene a partire dai file `.txt` presenti nella cartella `file_txt/`  
- `TXTSearcher.java` → permette di interrogare l’indice e visualizzare in console i documenti trovati  

Analyzer utilizzati:
- title → `CustomAnalyzer` con `WhitespaceTokenizer` e `LowerCaseFilter` (ricerca letterale e case-insensitive)  
- content → `StandardAnalyzer` con rimozione delle stopword in inglese (ricerca più semantica)

Flusso di lavoro:
Eseguire:
1. `download_txt.py`
2. `TXTIndexer.java`
3. `TXTSearcher.java`
4. Interagire con `TXTSearcher.java` tramite console, inserendo query del tipo:
- title:<termine>
- content:<termine>
oppure combinando i due campi o utilizzando più termini

Dopo l’esecuzione di una query, il programma stampa in console:
- il numero totale di documenti trovati;
- per ogni documento:
  - Titolo del paper;
  - Nome del file `.txt`;
  - Punteggio di rilevanza (score);
  - tempo di risposta
