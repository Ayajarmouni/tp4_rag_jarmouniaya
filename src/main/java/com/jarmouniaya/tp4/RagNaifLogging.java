package com.jarmouniaya.tp4;

/**
 * Test 1 avec logging : permet de voir le prompt augmenté envoyé au LLM.
 * Sert aussi de "AVANT" pour le Test 2 (Q1 : Qu'est-ce que le fine-tuning ?
 * puis Q2 : Dans le support de cours fourni, à quoi c'est comparé ?).
 */
public class RagNaifLogging {
    public static void main(String[] args) {
        RagNaif.lancer(true);
    }
}
