package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.BoardComment;
import com.assetmanagement.backend.entity.BoardPost;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.UserMapper;
import com.assetmanagement.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final UserMapper userMapper;

    @GetMapping("/posts")
    public ResponseEntity<List<BoardPost>> getAllPosts(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(boardService.getAllPosts(search));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<BoardPost> getPost(@PathVariable Long id) {
        BoardPost post = boardService.getPost(id);
        if (post == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts")
    public ResponseEntity<Void> createPost(@AuthenticationPrincipal String email, @RequestBody BoardPost post) {
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userMapper.findByEmail(email);
        if (user == null) {
            System.err.println("User not found for email: " + email);
            return ResponseEntity.status(404).build();
        }
        post.setAuthorId(user.getId());
        boardService.createPost(post);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Void> updatePost(@AuthenticationPrincipal String email, @PathVariable Long id, @RequestBody BoardPost post) {
        User user = userMapper.findByEmail(email);
        BoardPost existing = boardService.getPost(id);
        
        if (existing == null) return ResponseEntity.notFound().build();
        if (!existing.getAuthorId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).build();
        }

        post.setId(id);
        boardService.updatePost(post);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@AuthenticationPrincipal String email, @PathVariable Long id) {
        User user = userMapper.findByEmail(email);
        BoardPost existing = boardService.getPost(id);
        
        if (existing == null) return ResponseEntity.notFound().build();
        if (!existing.getAuthorId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).build();
        }

        boardService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    // Comments
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<BoardComment>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(boardService.getComments(postId));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Void> addComment(@AuthenticationPrincipal String email, @PathVariable Long postId, @RequestBody BoardComment comment) {
        User user = userMapper.findByEmail(email);
        comment.setPostId(postId);
        comment.setAuthorId(user.getId());
        boardService.addComment(comment);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal String email, @PathVariable Long id) {
        User user = userMapper.findByEmail(email);
        BoardComment existing = boardService.getComment(id);
        
        if (existing == null) return ResponseEntity.notFound().build();
        if (!existing.getAuthorId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).build();
        }

        boardService.deleteComment(id);
        return ResponseEntity.ok().build();
    }
}
