package com.emiyaoj.problem.service;

import com.emiyaoj.problem.dto.ProblemPictureVO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IProblemImageService {

    ProblemPictureVO upload(MultipartFile file, Long userId);

    ResponseEntity<InputStreamResource> download(Long pictureId);

    boolean delete(Long pictureId, Long userId);
}
