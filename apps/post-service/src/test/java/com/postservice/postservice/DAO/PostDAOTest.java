package com.postservice.postservice.DAO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.postservice.postservice.dao.GymRepository;
import com.postservice.postservice.dao.PostDAO;
import com.postservice.postservice.dao.ReadingRepository;
import com.postservice.postservice.models.Post;
import com.postservice.postservice.models.gym.Gym;
import com.postservice.postservice.models.reading.Reading;
import java.util.Collections;
import java.util.List;
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
    private GymRepository gymRepository;

    @Mock
    private ReadingRepository readingRepository;

    @InjectMocks
    private PostDAO postDAO;

    Pageable pageable = PageRequest.of(0, 20);

    @Test
    @DisplayName(
        "When property type is 'GYM', all gym posts should be returned from the gym repository"
    )
    void getPostsByHobbyProperty_gymType_returnsGymPosts() {
        List<Gym> gymPosts = List.of(new Gym(), new Gym());

        when(gymRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(
            gymPosts
        ); // ✅ updated

        List<? extends Post> result = postDAO.getPostsByHobbyProperty(
            "GYM",
            pageable
        );

        assertThat(result).isEqualTo(gymPosts).hasSize(2);
        verify(gymRepository, times(1)).findAllByOrderByCreatedAtDesc(pageable); // ✅ updated
        verifyNoInteractions(readingRepository);
    }

    @Test
    @DisplayName(
        "When property type is 'READING', all reading posts should be returned from the reading repository"
    )
    void getPostsByHobbyProperty_readingType_returnsReadingPosts() {
        List<Reading> readingPosts = List.of(new Reading(), new Reading());

        when(
            readingRepository.findAllByOrderByCreatedAtDesc(pageable)
        ).thenReturn(readingPosts); // ✅ updated

        List<? extends Post> result = postDAO.getPostsByHobbyProperty(
            "READING",
            pageable
        );

        assertThat(result).isEqualTo(readingPosts).hasSize(2);
        verify(readingRepository, times(1)).findAllByOrderByCreatedAtDesc(
            pageable
        ); // ✅ updated
        verifyNoInteractions(gymRepository);
    }

    @Test
    @DisplayName(
        "When property type is unrecognized, a RuntimeException should be thrown with message 'Post Type not found'"
    )
    void getPostsByHobbyProperty_unknownType_throwsRuntimeException() {
        assertThatThrownBy(() ->
            postDAO.getPostsByHobbyProperty("UNKNOWN", pageable)
        )
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Post Type not found");

        verifyNoInteractions(gymRepository, readingRepository);
    }

    @Test
    @DisplayName(
        "When property type is 'GYM' and no gym posts exist, an empty list should be returned"
    )
    void getPostsByHobbyProperty_gymType_noResults_returnsEmptyList() {
        when(gymRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(
            Collections.emptyList()
        ); // ✅ updated

        List<? extends Post> result = postDAO.getPostsByHobbyProperty(
            "GYM",
            pageable
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName(
        "When property type is 'READING' and no reading posts exist, an empty list should be returned"
    )
    void getPostsByHobbyProperty_readingType_noResults_returnsEmptyList() {
        when(
            readingRepository.findAllByOrderByCreatedAtDesc(pageable)
        ).thenReturn(Collections.emptyList()); // ✅ updated

        List<? extends Post> result = postDAO.getPostsByHobbyProperty(
            "READING",
            pageable
        );

        assertThat(result).isEmpty();
    }
}
