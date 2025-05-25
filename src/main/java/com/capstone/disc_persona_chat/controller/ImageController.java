package com.capstone.disc_persona_chat.controller;

import com.capstone.disc_persona_chat.dto.ProfileImageDto;
import com.capstone.disc_persona_chat.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;

    /**
     * 프로필 이미지 URL을 조회합니다.
     *
     * @param imageName 이미지 파일명
     * @return 프로필 이미지 URL을 포함한 응답
     */
    @GetMapping("/image/{imageName}")
    public ResponseEntity<ProfileImageDto> getProfileImage(@PathVariable String imageName) {
        String imageUrl = s3Service.getProfileImageUrl(imageName);
        return ResponseEntity.ok(new ProfileImageDto(imageUrl));
    }
}
