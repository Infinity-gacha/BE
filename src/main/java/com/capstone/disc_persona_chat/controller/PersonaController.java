package com.capstone.disc_persona_chat.controller;

import com.capstone.disc_persona_chat.dto.PersonaDto;
import com.capstone.disc_persona_chat.service.PersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas") // 페르소나 관련 작업의 기본 경로
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;

    /**
     * POST /api/personas : 새 페르소나를 생성
     */
    @PostMapping
    public ResponseEntity<PersonaDto.Response> createPersona(@RequestBody PersonaDto.Request request) {
        PersonaDto.Response createdPersona = personaService.createPersona(request);
        return new ResponseEntity<>(createdPersona, HttpStatus.CREATED);
    }

    /**
     * GET /api/personas : 모든 페르소나 목록을 검색하거나 이름으로 검색
     * GET /api/personas?name=""
     * 이름 파라미터가 제공되면 이름 검색을 수행하고, 그렇지 않으면 모든 페르소나를 반환
     * @param name 
     * @return 페르소나 응답 DTO 목록
     */
    @GetMapping
    public ResponseEntity<List<PersonaDto.Response>> getPersonas(@RequestParam(required = false) String name) {
        List<PersonaDto.Response> personas;
        if (name != null && !name.trim().isEmpty()) {
            // 이름 검색 파라미터가 있으면 이름으로 검색
            personas = personaService.searchPersonasByName(name);
        } else {
            // 이름 검색 파라미터가 없으면 모든 페르소나 조회
            personas = personaService.getAllPersonas();
        }
        return ResponseEntity.ok(personas);
    }

    /**
     * GET /api/personas/{id} : ID로 특정 페르소나를 검색
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonaDto.Response> getPersonaById(@PathVariable Long id) {
        // PersonaNotFoundException은 @ControllerAdvice 또는 아래 핸들러에서 처리
        PersonaDto.Response persona = personaService.getPersonaById(id);
        return ResponseEntity.ok(persona);
    }

    /**
     * PUT /api/personas/{id} : 기존 페르소나를 업데이트
     */
    @PutMapping("/{id}")
    public ResponseEntity<PersonaDto.Response> updatePersona(@PathVariable Long id, @RequestBody PersonaDto.Request request) {
        PersonaDto.Response updatedPersona = personaService.updatePersona(id, request);
        return ResponseEntity.ok(updatedPersona);
    }

    /**
     * DELETE /api/personas/{id} : 페르소나를 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePersona(@PathVariable Long id) {
        personaService.deletePersona(id);
        return ResponseEntity.noContent().build(); // 성공 시 204 No Content 반환
    }

}

