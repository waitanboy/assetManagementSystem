package com.assetmanagement.backend.service;

import com.assetmanagement.backend.dto.NoticeRequest;
import com.assetmanagement.backend.entity.Notice;
import com.assetmanagement.backend.entity.User;
import com.assetmanagement.backend.mapper.NoticeMapper;
import com.assetmanagement.backend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;
    private final UserMapper userMapper;

    public List<Notice> getAllNotices() {
        return noticeMapper.findAll();
    }

    public Notice getNoticeById(Long id) {
        return noticeMapper.findById(id);
    }

    @Transactional
    public Notice createNotice(NoticeRequest request) {
        Long authorId = getCurrentUserId();
        if (authorId == null) {
            throw new RuntimeException("Unauthorized");
        }

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(authorId)
                .build();
        
        noticeMapper.insert(notice);
        return noticeMapper.findById(notice.getId()); // To fetch with join details
    }

    @Transactional
    public Notice updateNotice(Long id, NoticeRequest request) {
        Notice existing = noticeMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("Notice not found");
        }
        
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        noticeMapper.update(existing);

        return noticeMapper.findById(id);
    }

    @Transactional
    public void deleteNotice(Long id) {
        noticeMapper.delete(id);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String email = auth.getPrincipal().toString();
            User user = userMapper.findByEmail(email);
            return user != null ? user.getId() : null;
        }
        return null;
    }
}
