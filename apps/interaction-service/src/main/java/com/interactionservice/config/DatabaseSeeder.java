package com.interactionservice.config;

import com.interactionservice.dto.CommentDTO;
import com.interactionservice.model.Comment;
import com.interactionservice.repository.CommentRepository;
import com.interactionservice.repository.LikeRepository;
import com.interactionservice.service.CommentService;
import com.interactionservice.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {
    @Autowired
    private LikeService likeService;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;

    @Override
    public void run(String... args) {
        UUID postId1 = UUID.fromString("2fc98726-48da-4da2-8c2e-bd53d602431f");
        UUID postId2 = UUID.fromString("7b7e2b2c-6d81-4088-b95e-10355f5e7c4a");
        UUID postId3 = UUID.fromString("cea3be72-b654-4c8e-9da1-1ac7e7e1018b");
        UUID user1 = UUID.fromString("3c50a324-7e99-4d20-8206-8dc70c453e26");
        UUID user2 = UUID.fromString("f94e1ec2-7361-4d94-8c68-36231081383d");

        if (likeRepository.count() == 0) {
            likeService.toggleLike(user1, postId1);
            likeService.toggleLike(user1, postId2);
            likeService.toggleLike(user2, postId3);
        }
        if (commentRepository.count() == 0) {
            CommentDTO comment1 = new CommentDTO(user1, postId1, "Nice pic");
            CommentDTO comment2 = new CommentDTO(user2, postId3, "Nice pic as well");
            Comment commentId_1 = commentService.createComment(comment1);
            commentService.createComment(comment2);
            CommentDTO comment3 = new CommentDTO(user2, commentId_1.getId(), "Why man?");
            commentService.createComment(comment3);
        }

    }
}
