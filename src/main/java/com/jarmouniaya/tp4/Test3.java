package com.jarmouniaya.tp4;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test 4 de l'énoncé (« Pas de RAG ») : un QueryRouter personnalisé qui laisse le LLM décider
 * s'il faut utiliser le RAG. Si le routeur renvoie une liste vide, il n'y a pas de RAG.
 * Tester : "Bonjour" puis une question sur le cours.
 */
public class Test3 {

    public static void main(String[] args) {
        Outils.configureLogger();
        ChatModel model = Outils.creerModele(true);
        EmbeddingModel embeddingModel = Outils.creerModeleEmbedding();
        EmbeddingStore<TextSegment> store = Outils.ingerer(RagNaif.PDF, embeddingModel);
        ContentRetriever retriever = Outils.creerRetriever(store, embeddingModel);

        class QueryRouterPourEviterRag implements QueryRouter {
            private final PromptTemplate template = PromptTemplate.from(
                    "Est-ce que la requête '{{question}}' porte sur l'IA ? "
                            + "Réponds seulement par 'oui', 'non' ou 'peut-être'.");

            @Override
            public Collection<ContentRetriever> route(Query query) {
                Prompt prompt = template.apply(Map.of("question", query.text()));
                String reponse = model.chat(prompt.text());   // ChatModel direct, sans AiServices
                System.out.println("[Routeur] Porte sur l'IA ? -> " + reponse);
                if (reponse.toLowerCase().contains("non")) {
                    return Collections.emptyList();           // pas de RAG
                }
                return List.of(retriever);
            }
        }

        RetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(new QueryRouterPourEviterRag())
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .retrievalAugmentor(augmentor)
                .build();

        Outils.conversation(assistant);
    }
}
