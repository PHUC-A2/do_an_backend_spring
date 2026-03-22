package com.example.backend.service.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.v2.RoomDeviceCatalog;
import com.example.backend.domain.request.v2.ReqCreateDeviceCatalogDTO;
import com.example.backend.domain.request.v2.ReqUpdateDeviceCatalogDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.v2.ResDeviceCatalogDTO;
import com.example.backend.repository.v2.RoomDeviceCatalogRepository;
import com.example.backend.util.error.IdInvalidException;

@Service
public class RoomDeviceCatalogService {

    private final RoomDeviceCatalogRepository roomDeviceCatalogRepository;

    public RoomDeviceCatalogService(RoomDeviceCatalogRepository roomDeviceCatalogRepository) {
        this.roomDeviceCatalogRepository = roomDeviceCatalogRepository;
    }

    @Transactional
    public ResDeviceCatalogDTO create(@NonNull ReqCreateDeviceCatalogDTO req) {
        RoomDeviceCatalog e = new RoomDeviceCatalog();
        applyCreate(req, e);
        RoomDeviceCatalog saved = roomDeviceCatalogRepository.save(e);
        return toDto(saved);
    }

    public ResultPaginationDTO getAll(Specification<RoomDeviceCatalog> spec, @NonNull Pageable pageable) {
        Page<RoomDeviceCatalog> page = roomDeviceCatalogRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(page.getTotalPages());
        mt.setTotal(page.getTotalElements());
        rs.setMeta(mt);

        List<ResDeviceCatalogDTO> list = new ArrayList<>();
        for (RoomDeviceCatalog row : page.getContent()) {
            list.add(toDto(row));
        }
        rs.setResult(list);
        return rs;
    }

    public ResDeviceCatalogDTO getDtoById(@NonNull Long id) throws IdInvalidException {
        return toDto(getById(id));
    }

    public RoomDeviceCatalog getById(@NonNull Long id) throws IdInvalidException {
        Optional<RoomDeviceCatalog> opt = roomDeviceCatalogRepository.findById(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        throw new IdInvalidException("Không tìm thấy danh mục thiết bị với ID = " + id);
    }

    @Transactional
    public ResDeviceCatalogDTO update(@NonNull Long id, @NonNull ReqUpdateDeviceCatalogDTO req)
            throws IdInvalidException {
        RoomDeviceCatalog e = getById(id);
        applyUpdate(req, e);
        RoomDeviceCatalog saved = roomDeviceCatalogRepository.save(e);
        return toDto(saved);
    }

    @Transactional
    public void deleteById(@NonNull Long id) throws IdInvalidException {
        if (!roomDeviceCatalogRepository.existsById(id)) {
            throw new IdInvalidException("Không tìm thấy danh mục thiết bị với ID = " + id);
        }
        roomDeviceCatalogRepository.deleteById(id);
    }

    private static void applyCreate(ReqCreateDeviceCatalogDTO req, RoomDeviceCatalog e) {
        e.setDeviceName(req.getDeviceName().trim());
        e.setDeviceType(req.getDeviceType());
        e.setMobilityType(req.getMobilityType());
        e.setDescription(trimToNull(req.getDescription()));
        e.setImageUrl(trimToNull(req.getImageUrl()));
        e.setManufacturer(trimToNull(req.getManufacturer()));
        e.setModel(trimToNull(req.getModel()));
        e.setStatus(req.getStatus());
    }

    private static void applyUpdate(ReqUpdateDeviceCatalogDTO req, RoomDeviceCatalog e) {
        e.setDeviceName(req.getDeviceName().trim());
        e.setDeviceType(req.getDeviceType());
        e.setMobilityType(req.getMobilityType());
        e.setDescription(trimToNull(req.getDescription()));
        e.setImageUrl(trimToNull(req.getImageUrl()));
        e.setManufacturer(trimToNull(req.getManufacturer()));
        e.setModel(trimToNull(req.getModel()));
        e.setStatus(req.getStatus());
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static ResDeviceCatalogDTO toDto(RoomDeviceCatalog e) {
        return new ResDeviceCatalogDTO(
                e.getId(),
                e.getDeviceName(),
                e.getDeviceType(),
                e.getMobilityType(),
                e.getDescription(),
                e.getImageUrl(),
                e.getManufacturer(),
                e.getModel(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getCreatedBy(),
                e.getUpdatedBy());
    }
}
