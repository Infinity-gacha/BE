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
     * @param imageName 이미지 파일명 (예: "profile1.jpg")
     * @return 이미지의 URL
     */
    public String getProfileImageUrl(String imageName) {
        // 프로필 이미지는 profiles 폴더에 저장되어 있다고 가정
        String key = imageName;
        
        // 이미지가 존재하는지 확인 (선택적)
        if (!amazonS3.doesObjectExist(bucketName, key)) {
            log.warn("프로필 이미지가 존재하지 않습니다: {}", key);
            // 기본 이미지 URL 반환 또는 예외 처리
            return getDefaultProfileImageUrl();
        }
        
        // S3 객체의 URL 반환
        return amazonS3.getUrl(bucketName, key).toString();
    }
    
    /**
     * 기본 프로필 이미지의 URL을 반환합니다.
     *
     * @return 기본 프로필 이미지 URL
     */
    private String getDefaultProfileImageUrl() {
        return amazonS3.getUrl(bucketName, "default.jpg").toString();
    }
}
