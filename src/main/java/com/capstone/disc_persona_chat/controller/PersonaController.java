package com.capstone.disc_persona_chat.controller;

import com.capstone.disc_persona_chat.config.security.SecurityUtils;
import com.capstone.disc_persona_chat.dto.PersonaDto;
import com.capstone.disc_persona_chat.service.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas") // 페르소나 관련 작업의 기본 경로
public class PersonaController {

    private final PersonaService personaService;

    @Autowired
    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    /**
     * POST /api/personas : 현재 로그인한 사용자를 위한 새 페르소나를 생성
     */
    @PostMapping
    public ResponseEntity<PersonaDto.Response> createPersona(@RequestBody PersonaDto.Request request) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        PersonaDto.Response createdPersona = personaService.createPersona(currentUserId, request);
        return new ResponseEntity<>(createdPersona, HttpStatus.CREATED);
    }

    /**
     * GET /api/personas : 현재 로그인한 사용자의 페르소나 목록을 검색하거나 이름으로 검색
     * GET /api/personas?name=""
     * 이름 파라미터가 제공되면 이름 검색을 수행하고, 그렇지 않으면 모든 페르소나를 반환
     * @param name 검색할 페르소나 이름 (선택적)
     * @return 페르소나 응답 DTO 목록
     */
    @GetMapping
    public ResponseEntity<List<PersonaDto.Response>> getPersonas(@RequestParam(required = false) String name) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        List<PersonaDto.Response> personas;
        if (name != null && !name.trim().isEmpty()) {
            // 이름 검색 파라미터가 있으면 현재 사용자의 페르소나 중 이름으로 검색
            personas = personaService.searchPersonasByNameAndUserId(name, currentUserId);
        } else {
            // 이름 검색 파라미터가 없으면 현재 사용자의 모든 페르소나 조회
            personas = personaService.getAllPersonasByUserId(currentUserId);
        }
        return ResponseEntity.ok(personas);
    }

    /**
     * GET /api/personas/{id} : ID로 특정 페르소나를 검색 (현재 사용자의 페르소나만 접근 가능)
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonaDto.Response> getPersonaById(@PathVariable Long id) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 페르소나 조회 시 현재 사용자의 ID도 함께 전달하여 권한 검증
        PersonaDto.Response persona = personaService.getPersonaByIdAndUserId(id, currentUserId);
        return ResponseEntity.ok(persona);
    }

    /**
     * PUT /api/personas/{id} : 기존 페르소나를 업데이트 (현재 사용자의 페르소나만 수정 가능)
     */
    @PutMapping("/{id}")
    public ResponseEntity<PersonaDto.Response> updatePersona(@PathVariable Long id, @RequestBody PersonaDto.Request request) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 페르소나 업데이트 시 현재 사용자의 ID도 함께 전달하여 권한 검증
        PersonaDto.Response updatedPersona = personaService.updatePersonaWithUserCheck(id, request, currentUserId);
        return ResponseEntity.ok(updatedPersona);
    }

    /**
     * DELETE /api/personas/{id} : 페르소나를 삭제 (현재 사용자의 페르소나만 삭제 가능)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersona(@PathVariable Long id) {
        // 현재 인증된 사용자의 ID를 가져와서 사용
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 페르소나 삭제 시 현재 사용자의 ID도 함께 전달하여 권한 검증
        personaService.deletePersonaWithUserCheck(id, currentUserId);
        return ResponseEntity.noContent().build(); // 성공 시 204 No Content 반환
    }
}
