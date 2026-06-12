package com.emiyaoj.problem.service.impl;

import com.emiyaoj.common.exception.BaseException;
import com.emiyaoj.problem.config.MinioProperties;
import com.emiyaoj.problem.domain.pojo.ProblemPicture;
import com.emiyaoj.problem.dto.ProblemPictureVO;
import com.emiyaoj.problem.mapper.ProblemPictureMapper;
import com.emiyaoj.problem.service.IProblemImageService;
import com.emiyaoj.problem.service.ProblemImageUrlResolver;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemImageServiceImpl implements IProblemImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            MediaType.IMAGE_GIF_VALUE
    );

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ProblemPictureMapper problemPictureMapper;
    private final ProblemImageUrlResolver problemImageUrlResolver;

    @Override
    public ProblemPictureVO upload(MultipartFile file, Long userId) {
        validate(file);
        ensureBucket();

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String extension = getExtension(originalFilename, file.getContentType());
        String objectName = "problem/%d/%s/%s.%s".formatted(
                userId, LocalDateTime.now().format(MONTH_FORMATTER), UUID.randomUUID(), extension);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .contentType(file.getContentType())
                    .stream(file.getInputStream(), file.getSize(), -1L)
                    .build());
        } catch (Exception e) {
            log.error("upload problem image to MinIO failed, endpoint={}, bucket={}, objectName={}",
                    minioProperties.getEndpoint(), minioProperties.getBucket(), objectName, e);
            throw new BaseException(500, "图片上传失败，请检查对象存储服务是否可用");
        }

        ProblemPicture picture = new ProblemPicture(
                null,
                userId,
                null,
                objectName,
                objectName,
                file.getContentType(),
                file.getSize(),
                originalFilename,
                LocalDateTime.now(),
                0
        );
        problemPictureMapper.insert(picture);
        return convertPictureToVO(picture);
    }

    @Override
    public ResponseEntity<InputStreamResource> download(Long pictureId) {
        ProblemPicture picture = selectActivePicture(pictureId);
        try {
            GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(picture.getObjectName())
                    .build());
            String filename = picture.getOriginalFilename() == null ? "image" : picture.getOriginalFilename();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(picture.getContentType()))
                    .contentLength(picture.getSize())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(URLEncoder.encode(filename, StandardCharsets.UTF_8))
                                    .build()
                                    .toString())
                    .body(new InputStreamResource(object));
        } catch (Exception e) {
            log.error("download problem image from MinIO failed, pictureId={}", pictureId, e);
            throw new BaseException(500, "图片下载失败，请检查对象存储服务是否可用");
        }
    }

    @Override
    public boolean delete(Long pictureId, Long userId) {
        ProblemPicture picture = selectActivePicture(pictureId);
        if (!picture.getUserId().equals(userId)) {
            throw new BaseException(403, "只能删除自己上传的图片");
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(picture.getObjectName())
                    .build());
        } catch (Exception e) {
            log.warn("remove problem image from MinIO failed, objectName={}", picture.getObjectName(), e);
        }
        picture.setDeleted(1);
        return problemPictureMapper.updateById(picture) == 1;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(400, "图片不能为空");
        }
        if (file.getSize() > minioProperties.getMaxFileSize()) {
            throw new BaseException(400, "图片大小不能超过10MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BaseException(400, "仅支持 jpg/jpeg/png/webp/gif 图片");
        }
    }

    private ProblemPicture selectActivePicture(Long pictureId) {
        ProblemPicture picture = problemPictureMapper.selectById(pictureId);
        if (picture == null || Integer.valueOf(1).equals(picture.getDeleted())) {
            throw new BaseException(404, "图片不存在");
        }
        return picture;
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
            }
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .config(publicReadPolicy())
                    .build());
        } catch (Exception e) {
            log.error("ensure MinIO bucket failed, endpoint={}, bucket={}",
                    minioProperties.getEndpoint(), minioProperties.getBucket(), e);
            throw new BaseException(500, "对象存储服务不可用，请检查 MinIO 连接配置");
        }
    }

    private String publicReadPolicy() {
        String bucket = minioProperties.getBucket();
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucket);
    }

    private String getExtension(String filename, String contentType) {
        String ext = StringUtils.getFilenameExtension(filename);
        if (StringUtils.hasText(ext)) {
            return ext.toLowerCase(Locale.ROOT);
        }
        return switch (contentType) {
            case MediaType.IMAGE_JPEG_VALUE -> "jpg";
            case MediaType.IMAGE_PNG_VALUE -> "png";
            case MediaType.IMAGE_GIF_VALUE -> "gif";
            case "image/webp" -> "webp";
            default -> "img";
        };
    }

    private ProblemPictureVO convertPictureToVO(ProblemPicture picture) {
        ProblemPictureVO vo = new ProblemPictureVO();
        BeanUtils.copyProperties(picture, vo);
        vo.setUrl(problemImageUrlResolver.buildPublicUrl(picture.getObjectName()));
        return vo;
    }
}
