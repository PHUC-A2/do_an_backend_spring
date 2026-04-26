package com.example.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.entity.PitchHourlyPrice;
import com.example.backend.domain.request.pitch.ReqCreatePitchDTO;
import com.example.backend.domain.request.pitch.ReqPitchHourlyPriceDTO;
import com.example.backend.domain.request.pitch.ReqUpdatePitchDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.pitch.ResCreatePitchDTO;
import com.example.backend.domain.response.pitch.ResPitchDTO;
import com.example.backend.domain.response.pitch.ResPitchHourlyPriceDTO;
import com.example.backend.domain.response.pitch.ResUpdatePitchDTO;
import com.example.backend.repository.PitchEquipmentRepository;
import com.example.backend.repository.PitchRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.tenant.TenantContext;
import com.example.backend.util.constant.review.ReviewStatusEnum;
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.util.error.BadRequestException;

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
        pitch.setTenantId(TenantContext.requireCurrentTenantId());
        syncHourlyPriceTenant(pitch);
        Pitch savedPitch = this.pitchRepository.save(pitch);

        return this.convertToResCreatePitchDTO(savedPitch, PitchRatingSummary.empty());
    }

    public ResultPaginationDTO getAllPitchesForCurrentTenant(Specification<Pitch> spec, @NonNull Pageable pageable) {
        long tid = TenantContext.requireCurrentTenantId();
        Specification<Pitch> tenantSpec = (root, q, cb) -> cb.equal(root.get("tenantId"), tid);
        Specification<Pitch> combined = spec == null ? tenantSpec : spec.and(tenantSpec);
        return getAllPitches(combined, pageable);
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
        assertCurrentTenant(pitch);

        // Cập nhật thông tin cơ bản của sân
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

        // Cập nhật toàn bộ khung giờ giá theo request (nếu có). Nếu request không gửi `hourlyPrices`
        // thì giữ nguyên để tránh xóa dữ liệu cũ ngoài ý muốn.
        if (req.getHourlyPrices() != null) {
            applyHourlyPricesToPitch(pitch, req.getHourlyPrices());
        }
        syncHourlyPriceTenant(pitch);

        Pitch updatedPitch = this.pitchRepository.save(pitch);

        return this.convertToResUpdatePitchDTO(updatedPitch, resolveRatingSummary(updatedPitch.getId()));
    }

    public void deletePitch(@NonNull Long id) throws IdInvalidException {

        Pitch pitch = this.getPitchById(id);
        assertCurrentTenant(pitch);
        pitchEquipmentRepository.deleteByPitchId(id);
        this.pitchRepository.deleteById(id);
    }

    // req create -> entity
    @NonNull
    public Pitch convertToReqCreatePitch(@NonNull ReqCreatePitchDTO req) {

        Pitch pitch = new Pitch();

        // Lưu thông tin cơ bản
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

        // Lưu lịch giá theo khung giờ (nếu có)
        applyHourlyPricesToPitch(pitch, req.getHourlyPrices());

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

        // Map lịch giá theo khung giờ để frontend có thể preview theo thời gian người dùng chọn
        res.setHourlyPrices(convertToResHourlyPrices(pitch.getHourlyPrices()));
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

        // Map lịch giá theo khung giờ để frontend hiển thị đúng theo cấu hình
        res.setHourlyPrices(convertToResHourlyPrices(pitch.getHourlyPrices()));
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

        // Map lịch giá theo khung giờ cho client hiển thị & tính preview giá
        res.setHourlyPrices(convertToResHourlyPrices(pitch.getHourlyPrices()));
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

    // ====================== Hourly price mapping ======================

    private List<PitchHourlyPrice> convertToReqHourlyPrices(List<ReqPitchHourlyPriceDTO> hourlyPrices, Pitch pitch) {
        // Nếu không truyền khung giờ giá thì để rỗng (giá cố định dùng `pricePerHour`)
        if (hourlyPrices == null || hourlyPrices.isEmpty()) {
            return new ArrayList<>();
        }

        // Validate trước khi chuyển sang entity để tránh dữ liệu chồng lấn
        validateHourlyPrices(hourlyPrices);

        List<PitchHourlyPrice> result = new ArrayList<>();
        for (ReqPitchHourlyPriceDTO dto : hourlyPrices) {
            PitchHourlyPrice e = new PitchHourlyPrice();
            e.setPitch(pitch);
            e.setStartTime(dto.getStartTime());
            e.setEndTime(dto.getEndTime());
            e.setPricePerHour(dto.getPricePerHour());
            result.add(e);
        }
        return result;
    }

    /**
     * Với orphanRemoval=true, tuyệt đối không replace reference của collection.
     * Luôn mutate trên cùng instance list để Hibernate quản lý orphan đúng cách.
     */
    private void applyHourlyPricesToPitch(Pitch pitch, List<ReqPitchHourlyPriceDTO> hourlyPrices) {
        if (pitch == null) {
            return;
        }

        List<PitchHourlyPrice> target = pitch.getHourlyPrices();
        if (target == null) {
            target = new ArrayList<>();
            pitch.setHourlyPrices(target);
        }

        List<PitchHourlyPrice> newItems = convertToReqHourlyPrices(hourlyPrices, pitch);
        target.clear();
        target.addAll(newItems);
    }

    private List<ResPitchHourlyPriceDTO> convertToResHourlyPrices(List<PitchHourlyPrice> hourlyPrices) {
        if (hourlyPrices == null || hourlyPrices.isEmpty()) {
            return List.of();
        }

        List<ResPitchHourlyPriceDTO> res = new ArrayList<>();
        for (PitchHourlyPrice e : hourlyPrices) {
            res.add(new ResPitchHourlyPriceDTO(e.getStartTime(), e.getEndTime(), e.getPricePerHour()));
        }
        // Sắp xếp theo giờ bắt đầu để hiển thị ổn định
        res.sort(Comparator.comparing(ResPitchHourlyPriceDTO::getStartTime));
        return res;
    }

    private void validateHourlyPrices(List<ReqPitchHourlyPriceDTO> hourlyPrices) {
        if (hourlyPrices == null || hourlyPrices.isEmpty()) {
            return;
        }

        // Chuẩn hóa khung giờ thành các interval không vượt qua mốc 00:00 (tách nếu qua nửa đêm)
        class Interval {
            final int startMinute;
            final int endMinute;

            Interval(int startMinute, int endMinute) {
                this.startMinute = startMinute;
                this.endMinute = endMinute;
            }
        }

        List<Interval> intervals = new ArrayList<>();
        for (ReqPitchHourlyPriceDTO dto : hourlyPrices) {
            if (dto.getStartTime() == null || dto.getEndTime() == null) {
                throw new BadRequestException("Khung giờ giá không được để trống giờ bắt đầu/kết thúc");
            }

            int startMin = toMinute(dto.getStartTime());
            int endMin = toMinute(dto.getEndTime());

            // start == end không tạo thành khoảng thời gian hợp lệ
            if (startMin == endMin) {
                throw new BadRequestException("Khung giờ giá không hợp lệ: giờ bắt đầu và giờ kết thúc không được trùng nhau");
            }

            if (startMin < endMin) {
                intervals.add(new Interval(startMin, endMin));
            } else {
                // Tách khung qua nửa đêm thành 2 phần: [start, 24h) và [0, end)
                intervals.add(new Interval(startMin, 24 * 60));
                intervals.add(new Interval(0, endMin));
            }
        }

        // Sort theo start và kiểm tra chồng lấn (end-exclusive -> overlap nếu next.start < prev.end)
        intervals.sort(Comparator.comparingInt(i -> i.startMinute));
        for (int i = 1; i < intervals.size(); i++) {
            Interval prev = intervals.get(i - 1);
            Interval cur = intervals.get(i);
            if (cur.startMinute < prev.endMinute) {
                throw new BadRequestException("Khung giờ giá bị chồng lấn nhau. Vui lòng chỉnh lại để không trùng/đè.");
            }
        }
    }

    private int toMinute(java.time.LocalTime t) {
        return t.getHour() * 60 + t.getMinute();
    }

    private void assertCurrentTenant(Pitch pitch) {
        long tid = TenantContext.requireCurrentTenantId();
        if (pitch.getTenantId() == null || !pitch.getTenantId().equals(tid)) {
            throw new BadRequestException("Sân không thuộc tenant hiện tại");
        }
    }

    private void syncHourlyPriceTenant(Pitch pitch) {
        if (pitch.getHourlyPrices() == null) {
            return;
        }
        Long tid = pitch.getTenantId();
        for (PitchHourlyPrice h : pitch.getHourlyPrices()) {
            h.setTenantId(tid);
        }
    }
}
