package com.assetmanagement.backend.mapper;

import com.assetmanagement.backend.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {
    List<Notice> findAll();
    Notice findById(Long id);
    int insert(Notice notice);
    int update(Notice notice);
    int delete(Long id);
}
