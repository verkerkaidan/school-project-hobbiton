package com.postservice.postservice.dao;

import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.reading.Reading;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class PostDAO {

    private final PostRepository postRepository;
    private final GymRepository gymRepository;
    private final ReadingRepository readingRepository;

    public PostDAO(PostRepository postRepository, GymRepository gymRepository, ReadingRepository readingRepository) {
        this.postRepository = postRepository;
        this.gymRepository = gymRepository;
        this.readingRepository = readingRepository;
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

    public List<? extends Post> getPostsByHobbyProperty(String propertyType, Pageable pageable) {
        switch(propertyType){
            case "GYM": return this.gymRepository.findAllByOrderByCreatedAtDesc(pageable);
            case "READING": return this.readingRepository.findAllByOrderByCreatedAtDesc(pageable);
            default: throw new RuntimeException("Post Type not found");
        }
    }
}
