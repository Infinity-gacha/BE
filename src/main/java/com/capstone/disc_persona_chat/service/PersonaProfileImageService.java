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

        // 연령대 결정
        String ageGroup = getAgeGroup(age);
        
        // 성별 문자열 (None인 경우 Male로 처리)
        String genderStr = (gender == Gender.None) ? "Male" : gender.name();
        
        // 기본 이미지 경로 생성
        String discTypeStr = discType.name();
        String imagePath = String.format("%s/%s/%s/%s_%s_%s.jpg", 
                PROFILE_BASE_PATH, discTypeStr, genderStr, discTypeStr, genderStr, ageGroup);
        
        // 이미지 존재 여부 확인
        if (amazonS3.doesObjectExist(bucketName, imagePath)) {
            log.info("페르소나 프로필 이미지를 찾았습니다: {}", imagePath);
            return s3Service.getProfileImageUrl(imagePath);
        }
        
        // 대체 이미지 시도 (같은 DISC 유형, 같은 성별, 다른 연령대)
        String[] alternativeAgeGroups = {"20", "30", "40", "10"};
        for (String altAgeGroup : alternativeAgeGroups) {
            if (!altAgeGroup.equals(ageGroup)) {
                String altImagePath = String.format("%s/%s/%s/%s_%s_%s.jpg", 
                        PROFILE_BASE_PATH, discTypeStr, genderStr, discTypeStr, genderStr, altAgeGroup);
                
                if (amazonS3.doesObjectExist(bucketName, altImagePath)) {
                    log.info("대체 연령대 프로필 이미지를 찾았습니다: {}", altImagePath);
                    return s3Service.getProfileImageUrl(altImagePath);
                }
            }
        }
        
        // 대체 이미지 시도 (같은 DISC 유형, 다른 성별)
        String alternativeGender = genderStr.equals("Male") ? "Female" : "Male";
        String altGenderImagePath = String.format("%s/%s/%s/%s_%s_%s.jpg", 
                PROFILE_BASE_PATH, discTypeStr, alternativeGender, discTypeStr, alternativeGender, ageGroup);
        
        if (amazonS3.doesObjectExist(bucketName, altGenderImagePath)) {
            log.info("대체 성별 프로필 이미지를 찾았습니다: {}", altGenderImagePath);
            return s3Service.getProfileImageUrl(altGenderImagePath);
        }
        
        // 기본 이미지 반환
        log.warn("적합한 프로필 이미지를 찾을 수 없습니다. 기본 이미지를 사용합니다.");
        return s3Service.getProfileImageUrl(DEFAULT_IMAGE_PATH);
    }
    
    /**
     * 나이를 연령대 그룹으로 변환합니다.
     *
     * @param age 나이
     * @return 연령대 그룹 (10, 20, 30, 40)
     */
    private String getAgeGroup(Integer age) {
        if (age == null) {
            return "20"; // 기본값
        }
        
        if (age < 20) {
            return "10";
        } else if (age < 30) {
            return "20";
        } else if (age < 40) {
            return "30";
        } else {
            return "40";
        }
    }
}
