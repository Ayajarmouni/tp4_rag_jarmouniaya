package com.jarmouniaya.tp4;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;

/**
 * Test 2 : transformation de la question.
 *
 * Conversation à tester :
 *   Q1 : Qu'est-ce que le fine-tuning ?
 *   Q2 : Dans le support de cours fourni, à quoi c'est comparé ?
 *
 * Problème sans transformation : Q2 ("à quoi c'est comparé ?") est vectorisée telle quelle ;
 * le "c'" ne veut rien dire hors contexte, donc la recherche de similarité ramène des segments
 * non pertinents (ou aucun). Le RAG ne voit pas l'historique de la conversation.
 *
 * Solution : CompressingQueryTransformer. Il demande au LLM de réécrire la question en
 * tenant compte de l'historique ("À quoi le fine-tuning est-il comparé dans le support ?"),
 * question autonome utilisée pour la recherche.
 */
public class Test2 {

    public static void main(String[] args) {
        Outils.configureLogger();
        ChatModel model = Outils.creerModele(true);

        EmbeddingModel embeddingModel = Outils.creerModeleEmbedding();
        EmbeddingStore<TextSegment> store = Outils.ingerer(RagNaif.PDF, embeddingModel);
        ContentRetriever retriever = Outils.creerRetriever(store, embeddingModel);

        QueryTransformer transformer = new CompressingQueryTransformer(model);

        RetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(transformer)
                .contentRetriever(retriever)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .retrievalAugmentor(augmentor)
                .build();

        Outils.conversation(assistant);
    }
}
