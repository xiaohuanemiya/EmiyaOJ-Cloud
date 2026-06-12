package com.emiyaoj.problem.service;

import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.problem.config.MinioProperties;
import com.emiyaoj.problem.domain.pojo.ProblemPicture;
import com.emiyaoj.problem.dto.ProblemPictureVO;
import com.emiyaoj.problem.mapper.ProblemPictureMapper;
import com.emiyaoj.problem.service.impl.ProblemImageServiceImpl;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import okhttp3.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProblemImageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ProblemPictureMapper problemPictureMapper;

    @Mock
    private ProblemImageUrlResolver problemImageUrlResolver;

    private MinioProperties minioProperties;
    private ProblemImageServiceImpl problemImageService;

    @BeforeEach
    void setUp() {
        minioProperties = new MinioProperties();
        minioProperties.setBucket("problem-images");
        minioProperties.setMaxFileSize(10L);
        problemImageService = new ProblemImageServiceImpl(
                minioClient, minioProperties, problemPictureMapper, problemImageUrlResolver);
    }

    @Test
    void uploadRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        BaseException ex = assertThrows(BaseException.class, () -> problemImageService.upload(file, 100L));

        assertEquals(400, ex.getCode());
        verify(problemPictureMapper, never()).insert(any(ProblemPicture.class));
    }

    @Test
    void uploadRejectsUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "abc".getBytes());

        BaseException ex = assertThrows(BaseException.class, () -> problemImageService.upload(file, 100L));

        assertEquals(400, ex.getCode());
        verify(problemPictureMapper, never()).insert(any(ProblemPicture.class));
    }

    @Test
    void uploadRejectsOversizedFile() {
        MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", "01234567890".getBytes());

        BaseException ex = assertThrows(BaseException.class, () -> problemImageService.upload(file, 100L));

        assertEquals(400, ex.getCode());
        verify(problemPictureMapper, never()).insert(any(ProblemPicture.class));
    }

    @Test
    void uploadStoresImageAndReturnsPublicUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", "abc".getBytes());
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(problemPictureMapper.insert(any(ProblemPicture.class))).thenAnswer(invocation -> {
            ProblemPicture picture = invocation.getArgument(0);
            picture.setId(1L);
            return 1;
        });
        when(problemImageUrlResolver.buildPublicUrl(anyString()))
                .thenReturn("http://127.0.0.1:9000/problem-images/problem/100/202605/a.png");

        ProblemPictureVO vo = problemImageService.upload(file, 100L);

        assertNotNull(vo);
        assertEquals(1L, vo.getId());
        assertEquals("image/png", vo.getContentType());
        assertEquals("http://127.0.0.1:9000/problem-images/problem/100/202605/a.png", vo.getUrl());
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(problemPictureMapper).insert(any(ProblemPicture.class));
    }

    @Test
    void deleteRejectsDifferentUploader() {
        ProblemPicture picture = new ProblemPicture();
        picture.setId(1L);
        picture.setUserId(200L);
        picture.setDeleted(0);
        when(problemPictureMapper.selectById(1L)).thenReturn(picture);

        BaseException ex = assertThrows(BaseException.class, () -> problemImageService.delete(1L, 100L));

        assertEquals(403, ex.getCode());
        verify(problemPictureMapper, never()).updateById(any(ProblemPicture.class));
    }

    @Test
    void downloadReturnsStoredContentType() throws Exception {
        ProblemPicture picture = new ProblemPicture();
        picture.setId(1L);
        picture.setUserId(100L);
        picture.setObjectName("problem/100/a.png");
        picture.setContentType("image/png");
        picture.setSize(3L);
        picture.setOriginalFilename("a.png");
        picture.setDeleted(0);
        when(problemPictureMapper.selectById(1L)).thenReturn(picture);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(new GetObjectResponse(
                Headers.of(), "problem-images", "", picture.getObjectName(), new ByteArrayInputStream("abc".getBytes())));

        ResponseEntity<InputStreamResource> response = problemImageService.download(1L);

        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertEquals(3L, response.getHeaders().getContentLength());
        assertNotNull(response.getBody());
    }
}
