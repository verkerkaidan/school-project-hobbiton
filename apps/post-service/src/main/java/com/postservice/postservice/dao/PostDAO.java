package com.postservice.postservice.dao;

import com.postservice.postservice.models.Post;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class PostDAO {

    private final PostRepository postRepository;

    public PostDAO(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post getPostById(UUID postId) {
        Optional<Post> optionalPost = this.postRepository.findById(postId);

        if (optionalPost.isEmpty()){
            throw new RuntimeException("Post not found");
        } else {
            return optionalPost.get();
        }
    }

    public void save(Post post) {
        this.postRepository.save(post);
    }

    public Set<Post> getPostByUserId(UUID userId) {
        return this.postRepository.findByUserId(userId);
    }

    public void deletePostById(UUID postId) {
        this.postRepository.deleteById(postId);
    }
}
