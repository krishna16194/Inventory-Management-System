package com.kr.bill.inventorymangementsystem.controller;

import com.kr.bill.inventorymangementsystem.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller providing an API for dynamic, AI-powered translations.
 *
 * <p>This controller exposes an endpoint to translate text using the underlying
 * {@link TranslationService}, which is powered by Spring AI.</p>
 */
@RestController
@RequestMapping("/api/v1/translate")
@Tag(name = "Translation API", description = "Endpoints for AI-powered dynamic translations.")
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    /**
     * DTO for the translation request body.
     */
    @Data
    static class TranslationRequest {
        private String text;
        private String targetLanguage;
    }

    /**
     * DTO for the translation response body.
     */
    @Data
    static class TranslationResponse {
        private final String translatedText;
    }

    @Operation(
            summary = "Translate text",
            description = "Translates a given piece of text into a target language using the configured AI model.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Translation successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body")
            }
    )
    @PostMapping
    public ResponseEntity<TranslationResponse> translate(@RequestBody TranslationRequest request) {
        if (request.getText() == null || request.getTargetLanguage() == null) {
            return ResponseEntity.badRequest().build();
        }
        String translatedText = translationService.translate(request.getText(), request.getTargetLanguage());
        return ResponseEntity.ok(new TranslationResponse(translatedText));
    }
}
