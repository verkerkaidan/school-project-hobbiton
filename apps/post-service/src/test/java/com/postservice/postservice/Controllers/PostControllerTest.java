package com.postservice.postservice.Controllers;

import com.postservice.postservice.controllers.PostController;
import com.postservice.postservice.dao.PostDAO;
import com.postservice.postservice.dto.LinkedHobbyItemDTO;
import com.postservice.postservice.dto.OtherDTO;
import com.postservice.postservice.dto.PostDTO;
import com.postservice.postservice.models.LinkedHobbyItem;
import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.other.Other;
import com.postservice.postservice.services.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @InjectMocks
    private PostController postController;

    @Mock
    private PostDAO postDAO;

    @Mock
    private PostService postService;


    @Test
    @DisplayName(
            "Create a post with a linkedHobbyItem"
    )
    void save_ShouldCreateNewPost() {
        // Arrange
        OtherDTO dto = new OtherDTO();
        dto.id = null;
        dto.title = "New Post";
        dto.description = "Description";
        dto.userId = UUID.randomUUID();

        LinkedHobbyItemDTO linkedItem = new LinkedHobbyItemDTO();
        linkedItem.displayName = "Football";
        linkedItem.imageUrl = "image.jpg";
        linkedItem.externalId = "123";
        linkedItem.hobbyType = "GYM";

        dto.linkedItems = Set.of(linkedItem);
        dto.mediaInstanceSet = Set.of(UUID.randomUUID());

        Post post = new Other(dto);

        when(postService.saveAsSetMediaInstanceFromSetUUID(
                anySet(),
                eq(dto.mediaInstanceSet),
                any(Post.class)))
                .thenReturn(new HashSet<>());

        when(postDAO.getPostById(post.getId())).thenReturn(post);

        // Mock toEntity()
        PostDTO spyDto = Mockito.spy(dto);
        doReturn(post).when(spyDto).toEntity();

        // Act
        ResponseEntity response = postController.save(spyDto);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(postDAO).save(post);

        assertThat(post.getTitle()).isEqualTo("New Post");
        assertThat(post.getDescription()).isEqualTo("Description");
        assertThat(post.getLinkedItems().size()).isEqualTo(1);
        assertThat(post.getTitle()).isEqualTo("New Post");


        LinkedHobbyItem createdItem =
                post.getLinkedItems().iterator().next();

        assertThat(createdItem.getDisplayName()).isEqualTo("Football");
        assertThat(createdItem.getPost()).isEqualTo(post);
    }

    @Test
    @DisplayName("Update post with non-existing id should throw exception")
    void save_ShouldThrowException_WhenPostDoesNotExist() {
        // Arrange
        OtherDTO dto = new OtherDTO();
        dto.id = UUID.randomUUID();
        dto.title = "Updated title";
        dto.description = "Updated description";
        dto.userId = UUID.randomUUID();

        when(postDAO.getPostById(dto.id)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> postController.save(dto))
                .isInstanceOf(NullPointerException.class);

        verify(postDAO).getPostById(dto.id);
        verify(postDAO, never()).save(any());
    }

    @Test
    @DisplayName("Create post without linked hobby items")
    void save_ShouldCreatePost_WhenLinkedItemsAreNull() {
        // Arrange
        OtherDTO dto = new OtherDTO();
        dto.title = "New Post";
        dto.description = "Description";
        dto.userId = UUID.randomUUID();
        dto.linkedItems = null;
        dto.mediaInstanceSet = Set.of();

        Post post = new Other(dto);

        when(postService.saveAsSetMediaInstanceFromSetUUID(
                anySet(),
                anySet(),
                any(Post.class)))
                .thenReturn(new HashSet<>());

        when(postDAO.getPostById(post.getId())).thenReturn(post);

        PostDTO spyDto = Mockito.spy(dto);
        doReturn(post).when(spyDto).toEntity();

        // Act
        ResponseEntity<Post> response = postController.save(spyDto);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(post.getLinkedItems()).isEmpty();
    }

    @Test
    @DisplayName("Create post should propagate media service exception")
    void save_ShouldThrowException_WhenMediaServiceFails() {
        // Arrange
        OtherDTO dto = new OtherDTO();
        dto.title = "New Post";
        dto.description = "Description";
        dto.userId = UUID.randomUUID();

        Post post = new Other(dto);

        PostDTO spyDto = Mockito.spy(dto);
        doReturn(post).when(spyDto).toEntity();

        when(postService.saveAsSetMediaInstanceFromSetUUID(
                anySet(),
                any(),
                any(Post.class)))
                .thenThrow(new RuntimeException("Media service failure"));

        // Act & Assert
        assertThatThrownBy(() -> postController.save(spyDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Media service failure");

        verify(postDAO, never()).save(any());
    }
}