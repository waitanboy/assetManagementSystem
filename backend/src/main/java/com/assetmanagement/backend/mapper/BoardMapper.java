package com.assetmanagement.backend.mapper;

import com.assetmanagement.backend.entity.BoardComment;
import com.assetmanagement.backend.entity.BoardPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    // Post
    List<BoardPost> findAllPosts(@Param("search") String search);
    BoardPost findPostById(Long id);
    void insertPost(BoardPost post);
    void updatePost(BoardPost post);
    void deletePost(Long id);
    void incrementViewCount(Long id);

    // Comment
    List<BoardComment> findCommentsByPostId(Long postId);
    void insertComment(BoardComment comment);
    void deleteComment(Long id);
    BoardComment findCommentById(Long id);
}
