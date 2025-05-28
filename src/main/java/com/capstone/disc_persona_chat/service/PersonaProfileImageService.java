package com.capstone.disc_persona_chat.service;

import com.amazonaws.services.s3.AmazonS3;
import com.capstone.disc_persona_chat.Enums.DiscType;
import com.capstone.disc_persona_chat.Enums.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 페르소나의 DISC 유형, 성별, 연령대에 따라 S3에서 프로필 이미지를 관리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonaProfileImageService {

    private final AmazonS3 amazonS3;
    private final S3Service s3Service;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // 프로필 이미지 기본 경로
    private static final String PROFILE_BASE_PATH = "profiles";
    private static final String DEFAULT_IMAGE_PATH = PROFILE_BASE_PATH + "/default/default.jpg";

    /**
     * 페르소나의 DISC 유형, 성별, 연령대에 따라 적절한 프로필 이미지 URL을 반환합니다.
     *
     * @param discType DISC 유형 (D, I, S, C)
     * @param gender 성별 (Male, Female, None)
     * @param age 나이
     * @return 프로필 이미지 URL
     */
    public String getPersonaProfileImageUrl(DiscType discType, Gender gender, Integer age) {
        if (discType == null || gender == null) {
            log.warn("DISC 유형 또는 성별이 null입니다. 기본 이미지를 사용합니다.");
            return s3Service.getProfileImageUrl(DEFAULT_IMAGE_PATH);
        }
        
        // 성별 문자열 (None인 경우 Male로 처리)
        String genderStr = (gender == Gender.None) ? "Male" : gender.name();
        
        // 기본 이미지 경로 생성
        String discTypeStr = discType.name();
        String imagePath = String.format("%s/%s/%s/%s_%s.png", 
                PROFILE_BASE_PATH, discTypeStr, genderStr, discTypeStr, genderStr);
        
        // 이미지 존재 여부 확인
        if (amazonS3.doesObjectExist(bucketName, imagePath)) {
            log.info("페르소나 프로필 이미지를 찾았습니다: {}", imagePath);
            return s3Service.getProfileImageUrl(imagePath);
        }
        
        // 기본 이미지 반환
        log.warn("적합한 프로필 이미지를 찾을 수 없습니다. 기본 이미지를 사용합니다.");
        return s3Service.getProfileImageUrl(DEFAULT_IMAGE_PATH);
    }
}
