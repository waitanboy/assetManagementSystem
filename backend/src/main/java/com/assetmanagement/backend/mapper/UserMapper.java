package com.assetmanagement.backend.mapper;

import com.assetmanagement.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {
    User findByEmail(String email);
    User findById(Long id);
    List<User> findAll();
    List<User> findApproved(); // status = 'APPROVED' 사용자만 반환 (대여자 선택용)
    void insert(User user);
    void update(User user);
    void delete(Long id);
}
