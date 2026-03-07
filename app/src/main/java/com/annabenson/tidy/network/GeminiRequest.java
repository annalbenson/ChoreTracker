package com.annabenson.tidy.network;

import java.util.List;

public class GeminiRequest {

    public final List<Content> contents;
    public final SystemInstruction systemInstruction;

    public GeminiRequest(String systemPrompt, List<Content> contents) {
        this.systemInstruction = new SystemInstruction(systemPrompt);
        this.contents = contents;
    }

    public static class SystemInstruction {
        public final List<Part> parts;
        public SystemInstruction(String text) {
            this.parts = java.util.Collections.singletonList(new Part(text));
        }
    }

    public static class Content {
        public final String role;
        public final List<Part> parts;
        public Content(String role, List<Part> parts) {
            this.role = role;
            this.parts = parts;
        }
    }

    public static class Part {
        public final String text;
        public Part(String text) { this.text = text; }
    }
}
