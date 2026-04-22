package com.kr.bill.inventorymangementsystem.service;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for providing dynamic, AI-powered translations.
 * <p>
 * This service uses the configured Spring AI {@link ChatClient} to translate text
 * into a specified target language. It seamlessly switches between a local Ollama
 * model for development and a free Hugging Face API for production.
 * </p>
 */
@Service
public class TranslationService {

    private final ChatClient chatClient;

    /**
     * A reusable prompt template for translation requests.
     * It instructs the AI model to act as a translator and only return the translated text.
     */
    private final PromptTemplate promptTemplate = new PromptTemplate(
            """
            You are a professional translator. Translate the following text into {language}.
            Do not add any commentary, notes, or explanations. Only return the translated text.
            Text to translate:
            ---
            {text}
            """
    );

    // With Spring AI 0.8.1, the ChatClient bean is configured automatically
    // and can be injected directly.
    public TranslationService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Translates the given text into the specified target language.
     *
     * @param text The text to translate.
     * @param targetLanguage The desired output language (e.g., "Tamil", "Spanish", "French").
     * @return The translated text, as returned by the AI model.
     */
    public String translate(String text, String targetLanguage) {
        if (text == null || text.isBlank() || targetLanguage == null || targetLanguage.isBlank()) {
            return text; // Return original text if input is invalid
        }

        Prompt prompt = promptTemplate.create(Map.of(
                "text", text,
                "language", targetLanguage
        ));

        // The API for ChatClient is slightly different in version 0.8.1
        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
}
