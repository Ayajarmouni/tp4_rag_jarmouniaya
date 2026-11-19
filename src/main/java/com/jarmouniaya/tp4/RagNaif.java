package com.jarmouniaya.tp4;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;

/** Test 1 : RAG naïf, étapes explicites (sans logging). */
public class RagNaif {

    static final String PDF = "rag.pdf";   // à placer dans src/main/resources

    public static void main(String[] args) {
        lancer(false);
    }

    static void lancer(boolean logging) {
        if (logging) {
            Outils.configureLogger();
        }
        ChatModel model = Outils.creerModele(logging);

        // ---- Phase 1 : enregistrement des embeddings ----
        EmbeddingModel embeddingModel = Outils.creerModeleEmbedding();
        EmbeddingStore<TextSegment> store = Outils.ingerer(PDF, embeddingModel);

        // ---- Phase 2 : utilisation des embeddings ----
        ContentRetriever retriever = Outils.creerRetriever(store, embeddingModel);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(retriever)
                .build();

        // Question de départ de l'énoncé
        String question = "Quelle est la signification de 'RAG' ; à quoi ça sert ?";
        System.out.println("Question : " + question);
        System.out.println("Assistant : " + assistant.chat(question));

        // Puis conversation interactive
        Outils.conversation(assistant);
    }
}
