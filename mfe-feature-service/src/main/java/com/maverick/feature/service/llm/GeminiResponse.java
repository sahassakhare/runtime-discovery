package com.maverick.feature.service.llm;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class GeminiResponse {
    private List<Candidate> candidates;

    @Data
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
    }

    @Data
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @NoArgsConstructor
    public static class Part {
        private String text;
    }
}
