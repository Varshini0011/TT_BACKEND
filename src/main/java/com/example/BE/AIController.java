package com.example.BE;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class AIController {

    @Value("${huggingface.api.key:NOT_SET}")
    private String hfApiKey;

    @PostConstruct
    public void checkKey() {
        if ("NOT_SET".equals(hfApiKey) || hfApiKey.isBlank()) {
            System.out.println("=== HF KEY: NOT SET ===");
        } else {
            System.out.println("=== HF KEY: LOADED OK (" +
                    hfApiKey.substring(0, 8) + "...) ===");
        }
    }

    @PostMapping("/analyze")
    public Map<String, String> analyzeImage(
            @RequestParam("image") MultipartFile file) {

        Map<String, String> result = new HashMap<>();
        result.put("issueType", "");
        result.put("description", "");
        result.put("aiError", "");

        if ("NOT_SET".equals(hfApiKey) || hfApiKey.isBlank()) {
            result.put("aiError", "HuggingFace API key not configured.");
            return result;
        }

        if (file == null || file.isEmpty()) {
            result.put("aiError", "No image received.");
            return result;
        }

        try {
            System.out.println("IMAGE SIZE: " + file.getSize());

            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String mimeType = file.getContentType();
            String dataUrl = "data:" + mimeType + ";base64," + base64;

            String url = "https://router.huggingface.co/v1/chat/completions";
            // Build message content with image + text
            Map<String, Object> imageUrlMap = new HashMap<>();
            imageUrlMap.put("url", dataUrl);
            
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            imageContent.put("image_url", imageUrlMap);
            System.out.println("Debugging statement1: Image content created.");
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text",
                "Look at this college campus image and identify the infrastructure issue. "
                + "Respond ONLY in this format: issueType | description. "
                + "Example: Broken Fan | The ceiling fan blades appear damaged.");

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", Arrays.asList(imageContent, textContent));
            System.out.println("Debugging statement2: Image content created.");
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "meta-llama/Llama-3.1-8B-Instruct");
            requestBody.put("messages", Collections.singletonList(message));
            requestBody.put("max_tokens", 200);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + hfApiKey);
            System.out.println("Debugging statement3: Image content created.");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map body = response.getBody();
            System.out.println("HF RESPONSE: " + body);
            System.out.println("Debugging statement4: Image content created.");
            List choices = (List) body.get("choices");
            Map choice = (Map) choices.get(0);
            Map msg = (Map) choice.get("message");
            String aiText = msg.get("content").toString().trim();
            System.out.println("Debugging statement5: Image content created.");
            System.out.println("AI TEXT: " + aiText);

            if (!aiText.contains("|")) {
                result.put("issueType", "Infrastructure Issue");
                result.put("description", aiText);
                return result;
            }

            String[] split = aiText.split("\\|", 2);
            result.put("issueType", split[0].trim());
            result.put("description", split[1].trim());
            System.out.println("Debugging statement6: Image content created.");
        } catch (Exception e) {
            System.out.println("HF ERROR: " + e.getMessage());
            result.put("aiError", "AI analysis failed. Please fill manually.");
        }

        return result;
    }
}