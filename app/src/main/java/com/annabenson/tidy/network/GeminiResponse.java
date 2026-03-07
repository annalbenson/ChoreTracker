package com.annabenson.tidy.network;

import java.util.List;

public class GeminiResponse {

    public List<Candidate> candidates;

    public String getText() {
        if (candidates == null || candidates.isEmpty()) return null;
        Candidate c = candidates.get(0);
        if (c.content == null || c.content.parts == null || c.content.parts.isEmpty()) return null;
        return c.content.parts.get(0).text;
    }

    public static class Candidate {
        public Content content;
    }

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;
    }
}
