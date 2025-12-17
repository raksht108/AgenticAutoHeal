package com.demo.agentic;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import okhttp3.*;

import java.util.*;
import java.util.stream.Collectors;

public class ChatGPTFeatureStep {

	private static final String API_KEY =
	           ""; 

    private static final String OPENAI_URL =
            "https://api.openai.com/v1/responses";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final OkHttpClient CLIENT = new OkHttpClient();

    /**
     * FEATURE-STYLE entry point
     */
    public static List<String> getCandidateXPaths(
            String failedXpath,
            String parentElementHtml,
            int limit) {

        System.out.println("ChatGPT feature step invoked");

        try {
            String prompt = buildFeaturePrompt(
                    failedXpath, parentElementHtml);

            String payload = buildRequest(prompt);

            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .post(RequestBody.create(
                            payload,
                            MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = CLIENT.newCall(request).execute();
            String body = response.body().string();

            return parseAndLimit(body, limit);

        } catch (Exception e) {
            throw new RuntimeException(
                    "ChatGPT feature autoheal failed", e);
        }
    }

    private static String buildFeaturePrompt(
            String failedXpath,
            String parentElement) {
/*
        System.out.println("ParentElement: " +
                parentElement.substring(0,
                        Math.min(500, parentElement.length())) + "...");
*/
        return "The following Selenium test failed due to an incorrect XPath:\n"
                + "Failing XPath: " + failedXpath + "\n\n"
                + "HTML snippet:\n" + parentElement + "\n\n"
                + "RULES:\n"
                + "- Provide ONLY alternative XPaths\n"
                + "- Do NOT add numbering or explanations\n"
                + "- Each XPath must be on a new line\n"
                + "- Do NOT include the failing XPath\n"
                + "- ONLY include valid, working XPaths\n\n"
                + "OUTPUT FORMAT:\n"
                + "//xpath1\n//xpath2\n//xpath3";
    }

    private static String buildRequest(String prompt)
            throws Exception {

        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", "gpt-4.1-mini");
        root.put("temperature", 0);

        ArrayNode input = root.putArray("input");
        ObjectNode msg = input.addObject();
        msg.put("role", "user");
        msg.put("content", prompt);

        return MAPPER.writeValueAsString(root);
    }

    /**
     * BULLETPROOF PARSER
     */
    private static List<String> parseAndLimit(
            String json,
            int limit) throws Exception {

        JsonNode root = MAPPER.readTree(json);
        String text = "";

        // Case 1: output_text shortcut
        if (root.has("output_text")) {
            text = root.get("output_text").asText();
        }

        // Case 2: standard Responses API
        else if (root.has("output")
                && root.get("output").isArray()) {

            for (JsonNode out : root.get("output")) {
                JsonNode content = out.get("content");
                if (content != null && content.isArray()) {
                    for (JsonNode c : content) {
                        if (c.has("text")) {
                            text = c.get("text").asText();
                            break;
                        }
                    }
                }
            }
        }

        if (text == null || text.trim().isEmpty()) {
            System.out.println("RAW OpenAI response:\n" + json);
            throw new RuntimeException("Empty ChatGPT response");
        }

        List<String> xpaths =
                Arrays.stream(text.split("\\R"))
                        .map(String::trim)
                        .filter(x -> x.startsWith("//"))
                        .distinct()
                        .limit(limit)
                        .collect(Collectors.toList());

        System.out.println("Candidate XPath count: " + xpaths.size());
        System.out.println("Candidate XPaths: " + xpaths);

        return xpaths;
    }
}
