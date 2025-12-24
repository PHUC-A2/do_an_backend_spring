package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.request.pitch.ReqCreatePitchDTO;
import com.example.backend.domain.request.pitch.ReqUpdatePitchDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.pitch.ResCreatePitchDTO;
import com.example.backend.domain.response.pitch.ResPitchDTO;
import com.example.backend.domain.response.pitch.ResUpdatePitchDTO;
import com.example.backend.repository.PitchRepository;
import com.example.backend.util.error.IdInvalidException;

@Service
public class PitchService {

    private final PitchRepository pitchRepository;

    public PitchService(PitchRepository pitchRepository) {
        this.pitchRepository = pitchRepository;
    }

    public ResCreatePitchDTO createPitch(ReqCreatePitchDTO req) {

        Pitch pitch = this.convertToReqCreatePitch(req);
        Pitch savedPitch = this.pitchRepository.save(pitch);

        return this.convertToResCreatePitchDTO(savedPitch);
    }

    public ResultPaginationDTO getAllPitches(Specification<Pitch> spec, Pageable pageable) {

        Page<Pitch> pagePitch = this.pitchRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pagePitch.getTotalPages());
        mt.setTotal(pagePitch.getTotalElements());

        rs.setMeta(mt);

        List<ResPitchDTO> resList = new ArrayList<>();
        for (Pitch pitch : pagePitch.getContent()) {
            resList.add(this.convertToResPitchDTO(pitch));
        }

        rs.setResult(resList);
        return rs;
    }

    public Pitch getPitchById(Long id) throws IdInvalidException {

        Optional<Pitch> optionalPitch = this.pitchRepository.findById(id);
        if (optionalPitch.isPresent()) {
            return optionalPitch.get();
        }
        throw new IdInvalidException("Không tìm thấy Pitch với ID = " + id);
    }

    public ResUpdatePitchDTO updatePitch(Long id, ReqUpdatePitchDTO req)
            throws IdInvalidException {

        Pitch pitch = this.getPitchById(id);

        pitch.setName(req.getName());
        pitch.setPitchType(req.getPitchType());
        pitch.setPricePerHour(req.getPricePerHour());
        pitch.setPitchUrl(req.getPitchUrl());
        pitch.setOpenTime(req.getOpenTime());
        pitch.setCloseTime(req.getCloseTime());
        pitch.setOpen24h(req.isOpen24h());
        pitch.setStatus(req.getStatus());
        pitch.setAddress(req.getAddress());

        Pitch updatedPitch = this.pitchRepository.save(pitch);

        return this.convertToResUpdatePitchDTO(updatedPitch);
    }

    public void deletePitch(Long id) throws IdInvalidException {

        Pitch pitch = this.getPitchById(id);
        this.pitchRepository.deleteById(pitch.getId());
    }


    // req create -> entity
    public Pitch convertToReqCreatePitch(ReqCreatePitchDTO req) {

        Pitch pitch = new Pitch();

        pitch.setName(req.getName());
        pitch.setPitchType(req.getPitchType());
        pitch.setPricePerHour(req.getPricePerHour());
        pitch.setPitchUrl(req.getPitchUrl());
        pitch.setOpenTime(req.getOpenTime());
        pitch.setCloseTime(req.getCloseTime());
        pitch.setOpen24h(req.isOpen24h());
        pitch.setStatus(req.getStatus());
        pitch.setAddress(req.getAddress());

        return pitch;
    }

    // entity -> res create
    public ResCreatePitchDTO convertToResCreatePitchDTO(Pitch pitch) {

        ResCreatePitchDTO res = new ResCreatePitchDTO();

        res.setId(pitch.getId());
        res.setName(pitch.getName());
        res.setPitchType(pitch.getPitchType());
        res.setPricePerHour(pitch.getPricePerHour());
        res.setPitchUrl(pitch.getPitchUrl());
        res.setOpenTime(pitch.getOpenTime());
        res.setCloseTime(pitch.getCloseTime());
        res.setOpen24h(pitch.isOpen24h());
        res.setStatus(pitch.getStatus());
        res.setAddress(pitch.getAddress());
        res.setCreatedAt(pitch.getCreatedAt());
        res.setCreatedBy(pitch.getCreatedBy());

        return res;
    }

    // entity -> res update
    public ResUpdatePitchDTO convertToResUpdatePitchDTO(Pitch pitch) {

        ResUpdatePitchDTO res = new ResUpdatePitchDTO();

        res.setId(pitch.getId());
        res.setName(pitch.getName());
        res.setPitchType(pitch.getPitchType());
        res.setPricePerHour(pitch.getPricePerHour());
        res.setPitchUrl(pitch.getPitchUrl());
        res.setOpenTime(pitch.getOpenTime());
        res.setCloseTime(pitch.getCloseTime());
        res.setOpen24h(pitch.isOpen24h());
        res.setStatus(pitch.getStatus());
        res.setAddress(pitch.getAddress());
        res.setUpdatedAt(pitch.getUpdatedAt());
        res.setUpdatedBy(pitch.getUpdatedBy());

        return res;
    }

    // entity -> res get
    public ResPitchDTO convertToResPitchDTO(Pitch pitch) {

        ResPitchDTO res = new ResPitchDTO();

        res.setId(pitch.getId());
        res.setName(pitch.getName());
        res.setPitchType(pitch.getPitchType());
        res.setPricePerHour(pitch.getPricePerHour());
        res.setPitchUrl(pitch.getPitchUrl());
        res.setOpenTime(pitch.getOpenTime());
        res.setCloseTime(pitch.getCloseTime());
        res.setOpen24h(pitch.isOpen24h());
        res.setStatus(pitch.getStatus());
        res.setAddress(pitch.getAddress());
        res.setCreatedAt(pitch.getCreatedAt());
        res.setCreatedBy(pitch.getCreatedBy());
        res.setUpdatedAt(pitch.getUpdatedAt());
        res.setUpdatedBy(pitch.getUpdatedBy());

        return res;
    }
}
