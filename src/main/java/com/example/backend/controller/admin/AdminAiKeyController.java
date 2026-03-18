package com.example.backend.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.backend.domain.request.ai.ReqAiKeyDTO;
import com.example.backend.domain.response.ai.ResAiKeyDTO;
import com.example.backend.service.AiApiKeyService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/ai/keys")
@RequiredArgsConstructor
public class AdminAiKeyController {

    private final AiApiKeyService aiApiKeyService;

    @GetMapping
    @ApiMessage("Lấy danh sách AI keys")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('AI_VIEW_LIST')")
    public ResponseEntity<List<ResAiKeyDTO>> listAll() {
        return ResponseEntity.ok(aiApiKeyService.listAll());
    }

    @PostMapping
    @ApiMessage("Thêm AI key")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('AI_CREATE')")
    public ResponseEntity<ResAiKeyDTO> add(@Valid @RequestBody ReqAiKeyDTO req) {
        return ResponseEntity.ok(aiApiKeyService.addKey(req));
    }

    @PatchMapping("/{id}/toggle")
    @ApiMessage("Bật/tắt AI key")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('AI_UPDATE')")
    public ResponseEntity<ResAiKeyDTO> toggle(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.ok(aiApiKeyService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa AI key")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('AI_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IdInvalidException {
        aiApiKeyService.deleteKey(id);
        return ResponseEntity.ok().build();
    }
}
