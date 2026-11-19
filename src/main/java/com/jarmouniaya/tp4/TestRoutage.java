package com.jarmouniaya.tp4;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Test 3 : routage entre 2 sources, décidé par le LLM.
 * Placer dans src/main/resources : rag.pdf (cours IA) et autre.pdf (sujet non IA, ex. recette, voyage...).
 * Adapter la description de AUTRE ci-dessous au contenu réel de votre second fichier.
 */
public class TestRoutage {

    static final String PDF_IA = "rag.pdf";
    static final String PDF_AUTRE = "autre.pdf";

    public static void main(String[] args) {
        Outils.configureLogger();
        ChatModel model = Outils.creerModele(true);
        EmbeddingModel embeddingModel = Outils.creerModeleEmbedding();

        // Phase 1 : 2 EmbeddingStores
        EmbeddingStore<TextSegment> storeIA = Outils.ingerer(PDF_IA, embeddingModel);
        EmbeddingStore<TextSegment> storeAutre = Outils.ingerer(PDF_AUTRE, embeddingModel);

        // Phase 2 : 2 ContentRetrievers
        ContentRetriever retrieverIA = Outils.creerRetriever(storeIA, embeddingModel);
        ContentRetriever retrieverAutre = Outils.creerRetriever(storeAutre, embeddingModel);

        Map<ContentRetriever, String> descriptions = new HashMap<>();
        descriptions.put(retrieverIA,
                "Cours sur l'intelligence artificielle : RAG, fine-tuning, embeddings, LLM, LangChain4j");
        descriptions.put(retrieverAutre,
                "Document sans lien avec l'IA (à adapter au contenu réel du second fichier)");

        QueryRouter router = new LanguageModelQueryRouter(model, descriptions);

        RetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(router)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .retrievalAugmentor(augmentor)
                .build();

        Outils.conversation(assistant);
    }
}
