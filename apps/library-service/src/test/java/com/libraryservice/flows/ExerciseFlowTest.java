package com.libraryservice.flows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.libraryservice.dto.common.SearchRequest;
import com.libraryservice.enums.SearchType;
import com.libraryservice.model.Exercise;
import com.libraryservice.strategy.exercise.ExerciseApiStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExerciseFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExerciseApiStrategy exerciseApiStrategy;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger(ExerciseFlowTest.class.getName());
        logger.setLevel(Level.WARNING);
    }

    @Test
    void search_exercises_fullFlow_success() throws Exception {
        Map<String, Object> rawExercise = Map.of(
                "exerciseId", "EX123",
                "name", "Push Up",
                "bodyParts", List.of("chest", "arms"),
                "targetMuscles", List.of("pectorals"),
                "secondaryMuscles", List.of("triceps"),
                "equipments", List.of("bodyweight"),
                "exerciseType", "strength",
                "overview", "Classic push up",
                "instructions", List.of("Place hands on floor", "Lower body", "Push up"),
                "gifUrl", "http://example.com/pushup.gif"
        );

        when(exerciseApiStrategy.searchExercises("push", 5))
                .thenReturn(List.of(new Exercise(
                        "EX123",
                        "Push Up",
                        List.of("chest", "arms"),
                        List.of("pectorals"),
                        List.of("triceps"),
                        List.of("bodyweight"),
                        "strength",
                        "Classic push up",
                        List.of("Place hands on floor", "Lower body", "Push up"),
                        null,
                        "http://example.com/pushup.gif"
                )));

        SearchRequest request = new SearchRequest(SearchType.EXERCISE, "push", 5);

        mockMvc.perform(post("/api/v1/library/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Push Up"))
                .andExpect(jsonPath("$.data[0].resultType").value("exercise"))
                .andExpect(jsonPath("$.data[0].bodyParts[0]").value("chest"));
    }

    @Test
    void findById_exercise_found() throws Exception {
        when(exerciseApiStrategy.findById("EX123"))
                .thenReturn(new Exercise(
                        "EX123",
                        "Push Up",
                        List.of("chest", "arms"),
                        List.of("pectorals"),
                        List.of("triceps"),
                        List.of("bodyweight"),
                        "strength",
                        "Classic push up",
                        List.of("Place hands on floor", "Lower body", "Push up"),
                        null,
                        "http://example.com/pushup.gif"
                ));

        mockMvc.perform(get("/api/v1/library/EXERCISE/EX123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Push Up"));
    }

    @Test
    void findById_exercise_notFound() throws Exception {
        when(exerciseApiStrategy.findById("EX999")).thenReturn(null);

        mockMvc.perform(get("/api/v1/library/EXERCISE/EX999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}