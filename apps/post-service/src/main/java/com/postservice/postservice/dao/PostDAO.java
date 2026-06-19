package com.postservice.postservice.dao;

import com.postservice.postservice.models.Post;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class PostDAO {

    private final PostRepository postRepository;
    private final GymRepository gymRepository;
    private final ReadingRepository readingRepository;
    private final OtherRepository otherRepository;

    public PostDAO(PostRepository postRepository, GymRepository gymRepository, ReadingRepository readingRepository, OtherRepository otherRepository) {
        this.postRepository = postRepository;
        this.gymRepository = gymRepository;
        this.readingRepository = readingRepository;
        this.otherRepository = otherRepository;
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
        return this.postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deletePostById(UUID postId) {
        this.postRepository.deleteById(postId);
    }

    public List<? extends Post> getPostsByHobbyProperty(String propertyType, Pageable pageable) {
        return switch (propertyType) {
            case "GYM" -> this.gymRepository.findAllByOrderByCreatedAtDesc(pageable);
            case "READING" -> this.readingRepository.findAllByOrderByCreatedAtDesc(pageable);
            case "OTHER" -> this.otherRepository.findAllByOrderByCreatedAtDesc(pageable);
            default -> throw new RuntimeException("Post Type not found");
        };
    }

    /**
     * Transactional is used, because all items in child classes should also be deleted
     * @param userId userId that is to be deleted
     */
    @Transactional
    public void deletePostsByUserId(UUID userId) {
        this.postRepository.deleteByUserId(userId);
    }

    public List<? extends Post> getPosts(Pageable pageable) {
        return this.postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<? extends Post> getPostsExcluding(Collection<UUID> excludeIds, Pageable pageable) {
        return this.postRepository.findAllExcludingUsersOrderByCreatedAtDesc(excludeIds, pageable);
    }
}
