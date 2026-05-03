package com.emiyaoj.blog.service;

import com.emiyaoj.blog.vo.BlogPictureVO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IBlogImageService {

    BlogPictureVO upload(MultipartFile file, Long userId);

    ResponseEntity<InputStreamResource> download(Long pictureId);

    boolean delete(Long pictureId, Long userId);
}
