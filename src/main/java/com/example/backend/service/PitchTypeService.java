package com.example.backend.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.PitchType;
import com.example.backend.domain.request.pitch.ReqPitchTypeDTO;
import com.example.backend.domain.response.pitch.ResPitchTypeDTO;
import com.example.backend.repository.PitchRepository;
import com.example.backend.repository.PitchTypeRepository;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

@Service
public class PitchTypeService {

    private final PitchTypeRepository pitchTypeRepository;
    private final PitchRepository pitchRepository;

    public PitchTypeService(PitchTypeRepository pitchTypeRepository, PitchRepository pitchRepository) {
        this.pitchTypeRepository = pitchTypeRepository;
        this.pitchRepository = pitchRepository;
    }

    public List<ResPitchTypeDTO> getAllPitchTypes() {
        return pitchTypeRepository.findAll().stream()
                .map(this::toResPitchTypeDTO)
                .toList();
    }

    public PitchType getPitchTypeById(Long id) throws IdInvalidException {
        return pitchTypeRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy loại sân với ID = " + id));
    }

    public ResPitchTypeDTO createPitchType(ReqPitchTypeDTO req) {
        String name = normalizeName(req.getName());
        if (pitchTypeRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Tên loại sân đã tồn tại");
        }

        String code = normalizeCode(req.getCode());
        if (code != null && pitchTypeRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Mã loại sân đã tồn tại");
        }

        PitchType pitchType = new PitchType();
        pitchType.setName(name);
        pitchType.setCode(code);
        return toResPitchTypeDTO(pitchTypeRepository.save(pitchType));
    }

    public ResPitchTypeDTO updatePitchType(Long id, ReqPitchTypeDTO req) throws IdInvalidException {
        PitchType pitchType = getPitchTypeById(id);

        String name = normalizeName(req.getName());
        if (pitchTypeRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BadRequestException("Tên loại sân đã tồn tại");
        }

        String code = normalizeCode(req.getCode());
        if (code != null && pitchTypeRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BadRequestException("Mã loại sân đã tồn tại");
        }

        pitchType.setName(name);
        pitchType.setCode(code);
        return toResPitchTypeDTO(pitchTypeRepository.save(pitchType));
    }

    public void deletePitchType(Long id) throws IdInvalidException {
        PitchType pitchType = getPitchTypeById(id);
        long usedCount = pitchRepository.countByPitchTypeId(id);
        if (usedCount > 0) {
            throw new BadRequestException("Không thể xóa loại sân đang được sử dụng");
        }
        pitchTypeRepository.delete(pitchType);
    }

    private ResPitchTypeDTO toResPitchTypeDTO(PitchType pitchType) {
        return new ResPitchTypeDTO(
                pitchType.getId(),
                pitchType.getName(),
                pitchType.getCode(),
                pitchType.getCreatedAt(),
                pitchType.getUpdatedAt());
    }

    private String normalizeName(String value) {
        String name = value == null ? "" : value.trim();
        if (name.isBlank()) {
            throw new BadRequestException("Tên loại sân không được để trống");
        }
        return name;
    }

    private String normalizeCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
