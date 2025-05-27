package com.capstone.disc_persona_chat.service;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * S3에 저장된 프로필 이미지의 URL을 생성합니다.
     *
     * @param imagePath 이미지 경로 (예: "profiles/D/Male/D_Male_20.jpg")
     * @return 이미지의 URL
     */
    public String getProfileImageUrl(String imagePath) {
        // 이미지가 존재하는지 확인
        if (!amazonS3.doesObjectExist(bucketName, imagePath)) {
            log.warn("프로필 이미지가 존재하지 않습니다: {}", imagePath);
            // 기본 이미지 URL 반환
            return getDefaultProfileImageUrl();
        }
        
        // S3 객체의 URL 반환
        return amazonS3.getUrl(bucketName, imagePath).toString();
    }
    
    /**
     * 기본 프로필 이미지의 URL을 반환합니다.
     *
     * @return 기본 프로필 이미지 URL
     */
    private String getDefaultProfileImageUrl() {
        String defaultImagePath = "profiles/default/default.jpg";
        return amazonS3.getUrl(bucketName, defaultImagePath).toString();
    }
}
