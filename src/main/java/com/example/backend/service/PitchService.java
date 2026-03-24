package com.example.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.request.pitch.ReqCreatePitchDTO;
import com.example.backend.domain.request.pitch.ReqUpdatePitchDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.pitch.ResCreatePitchDTO;
import com.example.backend.domain.response.pitch.ResPitchDTO;
import com.example.backend.domain.response.pitch.ResUpdatePitchDTO;
import com.example.backend.repository.PitchEquipmentRepository;
import com.example.backend.repository.PitchRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.util.constant.review.ReviewStatusEnum;
import com.example.backend.util.error.IdInvalidException;

@Service
public class PitchService {

    private final PitchRepository pitchRepository;
    private final PitchEquipmentRepository pitchEquipmentRepository;
    private final ReviewRepository reviewRepository;

    public PitchService(
            PitchRepository pitchRepository,
            PitchEquipmentRepository pitchEquipmentRepository,
            ReviewRepository reviewRepository) {
        this.pitchRepository = pitchRepository;
        this.pitchEquipmentRepository = pitchEquipmentRepository;
        this.reviewRepository = reviewRepository;
    }

    public ResCreatePitchDTO createPitch(@NonNull ReqCreatePitchDTO req) {

        Pitch pitch = this.convertToReqCreatePitch(req);
        Pitch savedPitch = this.pitchRepository.save(pitch);

        return this.convertToResCreatePitchDTO(savedPitch, PitchRatingSummary.empty());
    }

    public ResultPaginationDTO getAllPitches(Specification<Pitch> spec, @NonNull Pageable pageable) {

        Page<Pitch> pagePitch = this.pitchRepository.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pagePitch.getTotalPages());
        mt.setTotal(pagePitch.getTotalElements());

        rs.setMeta(mt);

        List<ResPitchDTO> resList = new ArrayList<>();
        Map<Long, PitchRatingSummary> summaryMap = buildPitchRatingSummaryMap(pagePitch.getContent());
        for (Pitch pitch : pagePitch.getContent()) {
            PitchRatingSummary summary = summaryMap.getOrDefault(pitch.getId(), PitchRatingSummary.empty());
            resList.add(this.convertToResPitchDTO(pitch, summary));
        }

        rs.setResult(resList);
        return rs;
    }

    public Pitch getPitchById(@NonNull Long id) throws IdInvalidException {

        Optional<Pitch> optionalPitch = this.pitchRepository.findById(id);
        if (optionalPitch.isPresent()) {
            return optionalPitch.get();
        }
        throw new IdInvalidException("Không tìm thấy Pitch với ID = " + id);
    }

    public ResUpdatePitchDTO updatePitch(@NonNull Long id, ReqUpdatePitchDTO req)
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
        pitch.setLatitude(req.getLatitude());
        pitch.setLongitude(req.getLongitude());
        pitch.setLength(req.getLength());
        pitch.setWidth(req.getWidth());
        pitch.setHeight(req.getHeight());
        pitch.setImageUrl(req.getImageUrl());

        Pitch updatedPitch = this.pitchRepository.save(pitch);

        return this.convertToResUpdatePitchDTO(updatedPitch, resolveRatingSummary(updatedPitch.getId()));
    }

    public void deletePitch(@NonNull Long id) throws IdInvalidException {

        // Pitch pitch = this.getPitchById(id);
        this.getPitchById(id);
        pitchEquipmentRepository.deleteByPitchId(id);
        this.pitchRepository.deleteById(id);
    }

    // req create -> entity
    @NonNull
    public Pitch convertToReqCreatePitch(@NonNull ReqCreatePitchDTO req) {

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
        pitch.setLatitude(req.getLatitude());
        pitch.setLongitude(req.getLongitude());
        pitch.setLength(req.getLength());
        pitch.setWidth(req.getWidth());
        pitch.setHeight(req.getHeight());
        pitch.setImageUrl(req.getImageUrl());

        return pitch;
    }

    // entity -> res create
    public ResCreatePitchDTO convertToResCreatePitchDTO(Pitch pitch, PitchRatingSummary summary) {

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
        res.setLatitude(pitch.getLatitude());
        res.setLongitude(pitch.getLongitude());
        res.setLength(pitch.getLength());
        res.setWidth(pitch.getWidth());
        res.setHeight(pitch.getHeight());
        res.setImageUrl(pitch.getImageUrl());
        res.setAverageRating(summary.averageRating());
        res.setReviewCount(summary.reviewCount());

        res.setCreatedAt(pitch.getCreatedAt());
        res.setCreatedBy(pitch.getCreatedBy());

        return res;
    }

    // entity -> res update
    public ResUpdatePitchDTO convertToResUpdatePitchDTO(Pitch pitch, PitchRatingSummary summary) {

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
        res.setLatitude(pitch.getLatitude());
        res.setLongitude(pitch.getLongitude());
        res.setLength(pitch.getLength());
        res.setWidth(pitch.getWidth());
        res.setHeight(pitch.getHeight());
        res.setImageUrl(pitch.getImageUrl());
        res.setAverageRating(summary.averageRating());
        res.setReviewCount(summary.reviewCount());

        res.setUpdatedAt(pitch.getUpdatedAt());
        res.setUpdatedBy(pitch.getUpdatedBy());

        return res;
    }

    // entity -> res get
    public ResPitchDTO convertToResPitchDTO(Pitch pitch) {
        return convertToResPitchDTO(pitch, resolveRatingSummary(pitch.getId()));
    }

    // entity -> res get (có sẵn summary để tránh query lặp)
    public ResPitchDTO convertToResPitchDTO(Pitch pitch, PitchRatingSummary summary) {

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
        res.setLatitude(pitch.getLatitude());
        res.setLongitude(pitch.getLongitude());
        res.setLength(pitch.getLength());
        res.setWidth(pitch.getWidth());
        res.setHeight(pitch.getHeight());
        res.setImageUrl(pitch.getImageUrl());
        res.setAverageRating(summary.averageRating());
        res.setReviewCount(summary.reviewCount());

        res.setCreatedAt(pitch.getCreatedAt());
        res.setCreatedBy(pitch.getCreatedBy());
        res.setUpdatedAt(pitch.getUpdatedAt());
        res.setUpdatedBy(pitch.getUpdatedBy());

        return res;
    }

    private Map<Long, PitchRatingSummary> buildPitchRatingSummaryMap(List<Pitch> pitches) {
        Map<Long, PitchRatingSummary> result = new HashMap<>();
        if (pitches == null || pitches.isEmpty()) {
            return result;
        }

        List<Long> pitchIds = pitches.stream().map(Pitch::getId).toList();
        List<Object[]> rows = reviewRepository.findPitchRatingSummaryByPitchIds(pitchIds, ReviewStatusEnum.APPROVED);
        for (Object[] row : rows) {
            Long pitchId = (Long) row[0];
            Double average = row[1] == null ? 0d : ((Number) row[1]).doubleValue();
            Long count = row[2] == null ? 0L : ((Number) row[2]).longValue();
            result.put(pitchId, new PitchRatingSummary(roundToOneDecimal(average), count));
        }
        return result;
    }

    private PitchRatingSummary resolveRatingSummary(Long pitchId) {
        if (pitchId == null) {
            return PitchRatingSummary.empty();
        }
        List<Object[]> rows = reviewRepository.findPitchRatingSummaryByPitchIds(List.of(pitchId), ReviewStatusEnum.APPROVED);
        if (rows.isEmpty()) {
            return PitchRatingSummary.empty();
        }
        Object[] row = rows.get(0);
        Double average = row[1] == null ? 0d : ((Number) row[1]).doubleValue();
        Long count = row[2] == null ? 0L : ((Number) row[2]).longValue();
        return new PitchRatingSummary(roundToOneDecimal(average), count);
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record PitchRatingSummary(Double averageRating, Long reviewCount) {
        private static PitchRatingSummary empty() {
            return new PitchRatingSummary(0d, 0L);
        }
    }
}
