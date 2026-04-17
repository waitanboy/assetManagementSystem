package com.assetmanagement.backend.service;

import com.assetmanagement.backend.entity.BoardComment;
import com.assetmanagement.backend.entity.BoardPost;
import com.assetmanagement.backend.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardMapper boardMapper;

    @Transactional(readOnly = true)
    public List<BoardPost> getAllPosts(String search) {
        return boardMapper.findAllPosts(search);
    }

    @Transactional
    public BoardPost getPost(Long id) {
        boardMapper.incrementViewCount(id);
        return boardMapper.findPostById(id);
    }

    @Transactional
    public void createPost(BoardPost post) {
        boardMapper.insertPost(post);
    }

    @Transactional
    public void updatePost(BoardPost post) {
        boardMapper.updatePost(post);
    }

    @Transactional
    public void deletePost(Long id) {
        boardMapper.deletePost(id);
    }

    @Transactional(readOnly = true)
    public List<BoardComment> getComments(Long postId) {
        return boardMapper.findCommentsByPostId(postId);
    }

    @Transactional
    public void addComment(BoardComment comment) {
        boardMapper.insertComment(comment);
    }

    @Transactional
    public void deleteComment(Long id) {
        boardMapper.deleteComment(id);
    }

    public BoardComment getComment(Long id) {
        return boardMapper.findCommentById(id);
    }
}
