package com.jarmouniaya.tp4;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Test 5 de l'énoncé : RAG avec récupération sur le Web (Tavily) + PDF.
 *
 * Variable d'environnement : TAVILY_KEY (+ GEMINI_KEY).
 *
 * POURQUOI des extraits du PDF sont ajoutés même pour une question sans rapport ?
 * -> DefaultQueryRouter envoie TOUJOURS la requête à TOUS les retrievers. Le retriever du PDF
 *    fait une recherche de similarité vectorielle : il renvoie toujours les segments les "moins
 *    éloignés" dont le score dépasse minScore (0,5). Une question comme "Qui a gagné le match
 *    hier ?" peut dépasser 0,5 avec des segments du cours (mots communs, modèle d'embedding
 *    peu discriminant). Le LLM reçoit donc du bruit dans son prompt.
 *
 * CORRECTION : remplacer DefaultQueryRouter par un LanguageModelQueryRouter qui décrit chaque
 * source ; le LLM choisit le(s) retriever(s) adapté(s) (PDF pour le cours, Web pour l'actualité).
 */
public class Test4 {

    public static void main(String[] args) {
        Outils.configureLogger();
        ChatModel model = Outils.creerModele(true);
        EmbeddingModel embeddingModel = Outils.creerModeleEmbedding();
        EmbeddingStore<TextSegment> store = Outils.ingerer(RagNaif.PDF, embeddingModel);

        ContentRetriever retrieverPdf = Outils.creerRetriever(store, embeddingModel);

        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(Outils.cle("TAVILY_KEY"))
                .build();
        ContentRetriever retrieverWeb = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(3)
                .build();

        // ---------------------------------------------------------------
        // VERSION DE BASE (énoncé) : les 2 retrievers sont toujours utilisés.
        // Mise en commentaire pour le correcteur ; voir la version corrigée plus bas.
        // ---------------------------------------------------------------
        // QueryRouter router = new DefaultQueryRouter(retrieverPdf, retrieverWeb);

        // ---------------------------------------------------------------
        // VERSION CORRIGÉE (bonus) : routage par le LLM.
        // ---------------------------------------------------------------
        Map<ContentRetriever, String> descriptions = new HashMap<>();
        descriptions.put(retrieverPdf,
                "Support de cours sur l'IA générative : RAG, fine-tuning, embeddings, LLM, LangChain4j");
        descriptions.put(retrieverWeb,
                "Recherche sur le Web : actualité, événements récents, informations générales "
                        + "qui ne sont pas dans le cours sur l'IA");
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
