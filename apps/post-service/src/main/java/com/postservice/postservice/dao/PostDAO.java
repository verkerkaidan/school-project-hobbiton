package com.postservice.postservice.dao;

import com.postservice.postservice.models.Post;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class PostDAO {
    private PostRepository postRepository;

    public PostDAO(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post generatePost(Post post) {
        Post savedPost = this.postRepository.save(post);
        return postRepository.findById(savedPost.getId()).orElseThrow();
    }

    public Set<Post> getPostsByUserId (UUID userId) {
        return this.postRepository.findByUserId(userId);
    }

    public Post getPostByPostId(UUID postId) {
        Optional<Post> optionalPost = this.postRepository.findById(postId);

        if(optionalPost.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatusCode.valueOf(404)
            );
        } else {
            return optionalPost.get();
        }
    }
}
