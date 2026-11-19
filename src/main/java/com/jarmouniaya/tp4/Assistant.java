package com.jarmouniaya.tp4;

/**
 * Interface de l'assistant, utilisée par tous les tests du TP 4.
 * AiServices génère une classe (proxy) qui l'implémente.
 */
public interface Assistant {
    String chat(String userMessage);
}
