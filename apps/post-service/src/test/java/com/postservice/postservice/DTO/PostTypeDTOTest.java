package com.postservice.postservice.DTO;

import com.postservice.postservice.controllers.PostController;
import com.postservice.postservice.dto.GymDTO;
import com.postservice.postservice.dto.GymExerciseDTO;
import com.postservice.postservice.dto.OtherDTO;
import com.postservice.postservice.dto.ReadingDTO;
import com.postservice.postservice.models.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PostTypeDTOTest {
    @Test
    @DisplayName("Creating a post with type GYM")
    void create_gym_post() {
        GymDTO gymDTO = new GymDTO();
        gymDTO.exercises.add(new GymExerciseDTO());
        Post post = gymDTO.toEntity();

        assertThat(post.getHobbyType()).isEqualTo("GYM");
    }

    @Test
    @DisplayName("Creating a post with type OTHER")
    void create_other_post() {
        OtherDTO otherDTO = new OtherDTO();
        Post post = otherDTO.toEntity();

        assertThat(post.getHobbyType()).isEqualTo("OTHER");
    }

    @Test
    @DisplayName("Creating a post with type READING")
    void create_reading_post() {
        ReadingDTO readingDTO = new ReadingDTO();
        Post post = readingDTO.toEntity();

        assertThat(post.getHobbyType()).isEqualTo("READING");
    }
}
