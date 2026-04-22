package com.kr.bill.inventorymangementsystem.config;

import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class AiConfig {

    /**
     * In the "render" profile (production), we want to use the HuggingFace ChatClient.
     * We mark it as @Primary so it gets injected into the TranslationService.
     */
    @Bean
    @Primary
    @Profile("render")
    public ChatClient productionChatClient(@Qualifier("huggingfaceChatClient") ChatClient chatClient) {
        return chatClient;
    }

    /**
     * In the default profile (local development), we want to use the Ollama ChatClient.
     * We mark it as @Primary so it gets injected into the TranslationService.
     */
    @Bean
    @Primary
    @Profile("!render")
    public ChatClient localChatClient(@Qualifier("ollamaChatClient") ChatClient chatClient) {
        return chatClient;
    }
}
