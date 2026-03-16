package com.postservice.postservice.DAO;

import com.postservice.postservice.dao.GymRepository;
import com.postservice.postservice.dao.PostDAO;
import com.postservice.postservice.dao.PostRepository;
import com.postservice.postservice.dao.ReadingRepository;
import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.gym.Gym;
import com.postservice.postservice.models.reading.Reading;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostDAOTest {

    @Mock
    private GymRepository gymRepository;

    @Mock
    private ReadingRepository readingRepository;

    @InjectMocks
    private PostDAO postDAO;

    @Test
    @DisplayName("When property type is 'GYM', all gym posts should be returned from the gym repository")
    void getPostsByHobbyProperty_gymType_returnsGymPosts() {
        List<Gym> gymPosts = List.of(new Gym(), new Gym());
        when(gymRepository.findAll()).thenReturn(gymPosts);

        List<? extends Post> result = postDAO.getPostsByHobbyProperty("GYM");

        assertThat(result).isEqualTo(gymPosts).hasSize(2);
        verify(gymRepository, times(1)).findAll();
        verifyNoInteractions(readingRepository);
    }

    @Test
    @DisplayName("When property type is 'READING', all reading posts should be returned from the reading repository")
    void getPostsByHobbyProperty_readingType_returnsReadingPosts() {
        List<Reading> readingPosts = List.of(new Reading(), new Reading());
        when(readingRepository.findAll()).thenReturn(readingPosts);

        List<? extends Post> result = postDAO.getPostsByHobbyProperty("READING");

        assertThat(result).isEqualTo(readingPosts).hasSize(2);
        verify(readingRepository, times(1)).findAll();
        verifyNoInteractions(gymRepository);
    }

    @Test
    @DisplayName("When property type is unrecognized, a RuntimeException should be thrown with message 'Post Type not found'")
    void getPostsByHobbyProperty_unknownType_throwsRuntimeException() {
        assertThatThrownBy(() -> postDAO.getPostsByHobbyProperty("UNKNOWN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Post Type not found");

        verifyNoInteractions(gymRepository, readingRepository);
    }

    @Test
    @DisplayName("When property type is 'GYM' and no gyms exist, an empty list should be returned")
    void getPostsByHobbyProperty_gymType_noResults_returnsEmptyList() {
        when(gymRepository.findAll()).thenReturn(Collections.emptyList());

        List<? extends Post> result = postDAO.getPostsByHobbyProperty("GYM");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("When property type is 'READING' and no readings exist, an empty list should be returned")
    void getPostsByHobbyProperty_readingType_noResults_returnsEmptyList() {
        when(readingRepository.findAll()).thenReturn(Collections.emptyList());

        List<? extends Post> result = postDAO.getPostsByHobbyProperty("READING");

        assertThat(result).isEmpty();
    }
}