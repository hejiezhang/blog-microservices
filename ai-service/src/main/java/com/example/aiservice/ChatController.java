package com.example.aiservice;

import com.example.aiservice.client.PostClient;
import com.example.aiservice.dto.PostDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController()
@RequestMapping("/ai")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private PostClient postClient;

    @GetMapping("/chat")
    public String getResponse(String prompt) {
        ChatResponse response = chatClient
                .prompt(prompt)
                .call().chatResponse();

        return response.getResult().getOutput().getText();
    }

    @GetMapping("/generatePost")
    public PostDto generatePost(@RequestParam String topic) {
        log.info("Generating AI post for topic: {}", topic);
        // Step 1: Build prompt
        String prompt = String.format("""
                You are an expert blog writer.
                Write a short but engaging blog post about the topic below.

                Topic: %s

                The post should include:
                - A clear and catchy title
                - A few paragraphs (around 200 words)
                - The tone should be professional yet friendly.

                Respond ONLY with valid JSON, no markdown, no explanations, Respond in this exact JSON format:
                {
                  "title": "Generated title here",
                  "content": "Full post content here"
                }
                """, topic);

        //System.out.println(prompt);

        // Step 2: Call AI model
        ChatResponse response = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();

        // Step 3: Parse the model output (JSON)
        String aiText = response.getResult().getOutput().getText();

        ObjectMapper mapper = new ObjectMapper();
        PostDto generatedPost;
        try {
            // Extract valid JSON block if needed
            Pattern jsonPattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = jsonPattern.matcher(aiText);
            if (matcher.find()) {
                aiText = matcher.group();
            }

            // Normalize smart quotes and whitespace
            aiText = aiText
                    .replaceAll("“", "\"")
                    .replaceAll("”", "\"")
                    .replaceAll("`", "\"")
                    .replaceAll("\n", " ")
                    .trim();

            //System.out.println(aiText);

            generatedPost = mapper.readValue(aiText, PostDto.class);
        } catch (Exception e) {
            // fallback if AI doesn't return perfect JSON
            generatedPost = new PostDto();
            generatedPost.setTitle("AI Generated Post: " + topic);
            generatedPost.setContent(aiText);
        }

        return generatedPost;

        // Step 4 (optional): save to post microservice
        //return postClient.createPost(generatedPost);
    }

    @GetMapping("/generateComment")
    public String generateComment(@RequestParam Long postId) {
        log.info("Generating AI Comment for postId: {}", postId);
        // 1. Get post content
        PostDto post = postClient.getPostById(postId);

        // 2. Build prompt
        String prompt = String.format("""
                You are a helpful assistant who writes insightful blog comments.
                Read the following post and generate one short, thoughtful comment
                that could be posted under it. around 50 words.

                Post Title: %s
                Post Content: %s
                """, post.getTitle(), post.getContent());

        //System.out.println(prompt);

        // 3. Call AI model
        ChatResponse response = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();

        // 4. Return the generated comment
        return response.getResult().getOutput().getText();
    }
}