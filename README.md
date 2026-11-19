# TP 4 - RAG (LangChain4j + Gemini)

Projet Java Maven ordinaire (pas Jakarta EE), package `com.jarmouniaya.tp4`.

## Avant de lancer
1. Copier dans `src/main/resources/` :
   - `rag.pdf` : le support de cours "Fine Tuning et RAG" (renommé)
   - `autre.pdf` : un 2e fichier hors IA (pour `TestRoutage` uniquement)
2. Définir les variables d'environnement (IntelliJ : Run > Edit Configurations > Environment variables) :
   - `GEMINI_KEY` (obligatoire)
   - `GEMINI_MODEL` (optionnel, défaut `gemini-2.0-flash`)
   - `TAVILY_KEY` (pour `Test4`)
3. Ne jamais commiter les clés.

## Classes (une `main` chacune)
| Classe | Test de l'énoncé |
|---|---|
| `RagNaif` | Test 1 - RAG naïf |
| `RagNaifLogging` | Test 1 avec logging (+ "avant" du Test 2) |
| `Test2` | Test 2 - CompressingQueryTransformer |
| `TestRoutage` | Test 3 - LanguageModelQueryRouter |
| `Test3` | Test 4 - Pas de RAG (QueryRouter perso) |
| `Test4` | Test 5 - Web (Tavily) + bonus routage |
`Outils` et `Assistant` sont partagées.
