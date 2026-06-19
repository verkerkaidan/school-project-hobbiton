package com.postservice.postservice.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.gym.Gym;
import com.postservice.postservice.models.other.Other;
import com.postservice.postservice.models.reading.Reading;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class PostDAOTest {


    @Mock
    private PostRepository postRepository;

    @Mock
    private GymRepository gymRepository;

    @Mock
    private ReadingRepository readingRepository;

    @Mock
    private OtherRepository otherRepository;

    @InjectMocks
    private PostDAO postDAO;

    private final Pageable pageable = PageRequest.of(0, 20);

    @Test
    @DisplayName("Get a post by id")
    void getPostById_ShouldReturnPost() {
        // Arrange
        UUID postId = UUID.randomUUID();
        Post post = mock(Post.class);

        when(postRepository.findById(postId))
                .thenReturn(Optional.of(post));

        // Act
        Post result = postDAO.getPostById(postId);

        // Assert
        assertThat(result).isEqualTo(post);

        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("Get a post by id that does not exist")
    void getPostById_ShouldThrowException_WhenPostDoesNotExist() {
        // Arrange
        UUID postId = UUID.randomUUID();

        when(postRepository.findById(postId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postDAO.getPostById(postId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post not found");

        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("Save a post")
    void save_ShouldSavePost() {
        // Arrange
        Post post = mock(Post.class);

        // Act
        postDAO.save(post);

        // Assert
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("Delete a post by id")
    void deletePostById_ShouldDeletePost() {
        // Arrange
        UUID postId = UUID.randomUUID();

        // Act
        postDAO.deletePostById(postId);

        // Assert
        verify(postRepository).deleteById(postId);
    }

    @Test
    @DisplayName("Get all posts for a user")
    void getPostByUserId_ShouldReturnPosts() {
        // Arrange
        UUID userId = UUID.randomUUID();

        Set<Post> posts = Set.of(
                mock(Post.class),
                mock(Post.class)
        );

        when(postRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(posts);

        // Act
        Set<Post> result = postDAO.getPostByUserId(userId);

        // Assert
        assertThat(result)
                .hasSize(2)
                .isEqualTo(posts);

        verify(postRepository)
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("Get all gym posts")
    void getPostsByHobbyProperty_ShouldReturnGymPosts() {
        // Arrange
        List<Gym> gymPosts = List.of(new Gym(), new Gym());

        when(gymRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(gymPosts);

        // Act
        List<? extends Post> result =
                postDAO.getPostsByHobbyProperty("GYM", pageable);

        // Assert
        assertThat(result)
                .hasSize(2)
                .isEqualTo(gymPosts);

        verify(gymRepository)
                .findAllByOrderByCreatedAtDesc(pageable);

        verifyNoInteractions(readingRepository, otherRepository);
    }

    @Test
    @DisplayName("Get all reading posts")
    void getPostsByHobbyProperty_ShouldReturnReadingPosts() {
        // Arrange
        List<Reading> readingPosts = List.of(new Reading(), new Reading());

        when(readingRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(readingPosts);

        // Act
        List<? extends Post> result =
                postDAO.getPostsByHobbyProperty("READING", pageable);

        // Assert
        assertThat(result)
                .hasSize(2)
                .isEqualTo(readingPosts);

        verify(readingRepository)
                .findAllByOrderByCreatedAtDesc(pageable);

        verifyNoInteractions(gymRepository, otherRepository);
    }

    @Test
    @DisplayName("Get all other posts")
    void getPostsByHobbyProperty_ShouldReturnOtherPosts() {
        // Arrange
        List<Other> otherPosts = List.of(new Other(), new Other());

        when(otherRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(otherPosts);

        // Act
        List<? extends Post> result =
                postDAO.getPostsByHobbyProperty("OTHER", pageable);

        // Assert
        assertThat(result)
                .hasSize(2)
                .isEqualTo(otherPosts);

        verify(otherRepository)
                .findAllByOrderByCreatedAtDesc(pageable);

        verifyNoInteractions(gymRepository, readingRepository);
    }

    @Test
    @DisplayName("Get posts with an invalid hobby property")
    void getPostsByHobbyProperty_ShouldThrowException_WhenPropertyDoesNotExist() {
        // Act & Assert
        assertThatThrownBy(() ->
                postDAO.getPostsByHobbyProperty("INVALID", pageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post Type not found");

        verifyNoInteractions(
                gymRepository,
                readingRepository,
                otherRepository
        );
    }

    @Test
    @DisplayName("Delete all posts from a user")
    void deletePostsByUserId_ShouldDeletePosts() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        postDAO.deletePostsByUserId(userId);

        // Assert
        verify(postRepository)
                .deleteByUserId(userId);
    }
}
