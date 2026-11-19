package com.jarmouniaya.tp4;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Méthodes utilitaires partagées par les tests (évite la duplication de code).
 *
 * Variables d'environnement à définir :
 *   GEMINI_KEY  : clé API Gemini (obligatoire)
 *   GEMINI_MODEL: nom du modèle (optionnel, défaut gemini-3.8-flash)
 *   TAVILY_KEY  : clé Tavily (uniquement pour Test4)
 */
public class Outils {

    public static String cle(String nomVariable) {
        String cle = System.getenv(nomVariable);
        if (cle == null || cle.isBlank()) {
            throw new IllegalStateException("Variable d'environnement manquante : " + nomVariable);
        }
        return cle;
    }

    /** Crée le ChatModel Gemini. Avec logging=true, requêtes/réponses sont journalisées. */
    public static ChatModel creerModele(boolean logging) {
        String nomModele = System.getenv().getOrDefault("GEMINI_MODEL", "gemini-3.8-flash");
        return GoogleAiGeminiChatModel.builder()
                .apiKey(cle("GEMINI_KEY"))
                .modelName(nomModele)
                .temperature(0.3)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(5)
                .logRequestsAndResponses(logging)
                .build();
    }

    /** Configuration du logger (java.util.logging) comme dans l'énoncé. */
    public static void configureLogger() {
        Logger packageLogger = Logger.getLogger("dev.langchain4j");
        packageLogger.setLevel(Level.FINE);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINE);
        packageLogger.addHandler(handler);
    }

    public static EmbeddingModel creerModeleEmbedding() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * Phase 1 du RAG : charge un PDF depuis src/main/resources, le découpe,
     * calcule les embeddings et les range dans un nouveau magasin en mémoire.
     */
    public static EmbeddingStore<TextSegment> ingerer(String nomFichier, EmbeddingModel embeddingModel) {
        // Pourquoi src/main/resources ? Maven copie ce répertoire dans le classpath
        // (target/classes), donc le fichier est retrouvé au même endroit après packaging.
        Document document = ClassPathDocumentLoader.loadDocument(nomFichier, new ApacheTikaDocumentParser());

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 30);
        List<TextSegment> segments = splitter.split(document);

        // embedAll retourne une Response<List<Embedding>> ; content() donne la liste
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        store.addAll(embeddings, segments);
        System.out.println("[Phase 1] " + nomFichier + " : " + segments.size() + " segments enregistrés");
        return store;
    }

    /** Retriever : 2 meilleurs résultats, score minimal 0,5. */
    public static ContentRetriever creerRetriever(EmbeddingStore<TextSegment> store, EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.5)
                .build();
    }

    /** Boucle de conversation : plusieurs questions sans recompiler. "fin" pour quitter. */
    public static void conversation(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("==================================================");
                System.out.print("Posez votre question (ou 'fin') : ");
                String question = scanner.nextLine();
                if (question.isBlank()) {
                    continue;
                }
                if ("fin".equalsIgnoreCase(question.trim())) {
                    break;
                }
                System.out.println("==================================================");
                String reponse = assistant.chat(question);
                System.out.println("Assistant : " + reponse);
            }
        }
    }
}