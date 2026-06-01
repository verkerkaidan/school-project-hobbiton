package com.apigateway.apigateway.clients;

import com.apigateway.apigateway.enums.HobbyType;
import com.apigateway.apigateway.models.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "post-service", url = "${post.service.url}")
public interface PostClient {
    @GetMapping("/posts/hobby/{hobbyType}")
    List<Post> getPostsByHobbyType(@PathVariable HobbyType hobbyType);

    @GetMapping("/posts")
    List<Post> getAllPosts();

    @GetMapping("/posts/users/{user_id}")
    List<Post> getUserPosts(@PathVariable("user_id") java.util.UUID userId);

    @GetMapping("/posts/{id}")
    Post getPostById(@PathVariable UUID id);

    // Admin: delete a single post by ID
    @DeleteMapping("/posts/{postId}")
    void deletePost(@PathVariable("postId") UUID postId);

    // Admin: delete all posts belonging to a user
    @DeleteMapping("/posts/users/{userId}")
    void deleteAllUserPosts(@PathVariable("userId") UUID userId);
}
