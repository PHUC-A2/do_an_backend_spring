package com.example.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.booking.ReqCreateBookingDTO;
import com.example.backend.domain.request.booking.ReqUpdateBookingDTO;
import com.example.backend.domain.response.booking.ResBookingDTO;
import com.example.backend.domain.response.booking.ResCreateBookingDTO;
import com.example.backend.domain.response.booking.ResUpdateBookingDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.repository.BookingRepository;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.booking.ShirtOptionEnum;
import com.example.backend.util.constant.user.UserStatusEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final PitchService pitchService;

    public BookingService(BookingRepository bookingRepository, UserService userService, PitchService pitchService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.pitchService = pitchService;
    }

    //

    @Transactional
    public ResCreateBookingDTO createBooking(ReqCreateBookingDTO req) throws IdInvalidException {

        // 1. Nếu userId != null => admin đặt cho user khác
        User user;
        if (req.getUserId() != null) {
            // Kiểm tra quyền admin
            // if (!SecurityUtil.isCurrentUserAdmin()) {
            // throw new BadRequestException("Không có quyền đặt cho user khác");
            // }
            user = userService.getUserById(req.getUserId());
        } else {

            // Lấy user từ token
            // user từ token (client đặt)
            String email = SecurityUtil.getCurrentUserLogin().orElse("");
            user = userService.handleGetUserByUsername(email);
        }

        // 2. Check user status
        if (user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new BadRequestException("Tài khoản không đủ điều kiện đặt lịch");
        }

        // 3. Lấy pitch Validate pitchId
        Long pitchId = req.getPitchId();
        if (pitchId == null) {
            throw new IdInvalidException("pitchId không được để trống");
        }

        // 4. Validate thời gian
        this.validateTime(req.getStartDateTime(), req.getEndDateTime());

        // 5. Lấy sân (pitch)
        Pitch pitch = pitchService.getPitchById(pitchId);

        // 6. Check trùng lịch
        this.checkOverlapping(pitch.getId(), req);

        // 7. Tính durationMinutes
        long durationMinutes = calculateDurationMinutes(
                req.getStartDateTime(),
                req.getEndDateTime());

        // 8. Lấy giá sân và Validate
        BigDecimal pricePerHour = pitch.getPricePerHour();
        if (pricePerHour == null || pricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Giá sân không hợp lệ");
        }

        // 9. Tính tổng tiền
        BigDecimal totalPrice = calculateTotalPrice(pricePerHour, durationMinutes);

        // 10. Resolve contactPhone
        String contactPhone = this.resolveContactPhone(user, req.getContactPhone());

        // 11. Lưu booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setPitch(pitch);
        booking.setStartDateTime(req.getStartDateTime());
        booking.setEndDateTime(req.getEndDateTime());
        booking.setShirtOption(
                req.getShirtOption() != null ? req.getShirtOption() : ShirtOptionEnum.WITHOUT_PITCH_SHIRT);
        booking.setContactPhone(contactPhone);
        booking.setDurationMinutes(durationMinutes);
        booking.setTotalPrice(totalPrice);

        this.bookingRepository.save(booking);

        // 12. Trả response
        return this.convertToResCreateBookingDTO(booking);
    }

    // get all
    public ResultPaginationDTO getAllBookings(Specification<Booking> spec, @NonNull Pageable pageable) {
        Page<Booking> pageBooking = bookingRepository.findAll(spec, pageable);

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageBooking.getTotalPages());
        meta.setTotal(pageBooking.getTotalElements());
        result.setMeta(meta);

        List<ResBookingDTO> resList = new ArrayList<>();
        for (Booking b : pageBooking.getContent()) {
            resList.add(this.convertToResBookingDTO(b));
        }
        result.setResult(resList);

        return result;
    }

    // get by id
    public Booking getBookingById(@NonNull Long id) throws IdInvalidException {

        Optional<Booking> optionalBooking = this.bookingRepository.findById(id);
        if (optionalBooking.isPresent()) {
            return optionalBooking.get();
        }
        throw new IdInvalidException("Không tìm thấy Booking với ID = " + id);
    }

    /*
     * @Transactional
     * public ResUpdateBookingDTO updateBooking(@NonNull Long id,
     * ReqUpdateBookingDTO req) throws IdInvalidException {
     * Booking booking = bookingRepository.findById(id)
     * .orElseThrow(() -> new IdInvalidException("Booking không tồn tại"));
     * 
     * // 1. Nếu admin muốn đổi user
     * if (req.getUserId() != null &&
     * !req.getUserId().equals(booking.getUser().getId())) {
     * User newUser = userService.getUserById(req.getUserId());
     * if (newUser.getStatus() != UserStatusEnum.ACTIVE) {
     * throw new BadRequestException("Tài khoản mới không đủ điều kiện đặt lịch");
     * }
     * booking.setUser(newUser);
     * }
     * 
     * // 2. Nếu admin muốn đổi pitch
     * 
     * // if (req.getPitchId() != null &&
     * // !req.getPitchId().equals(booking.getPitch().getId())) {
     * // Pitch newPitch = pitchService.getPitchById(req.getPitchId());
     * // booking.setPitch(newPitch);
     * // }
     * 
     * Long newPitchId = req.getPitchId();
     * if (newPitchId != null && !newPitchId.equals(booking.getPitch().getId())) {
     * Pitch newPitch = pitchService.getPitchById(newPitchId);
     * booking.setPitch(newPitch);
     * }
     * 
     * // 3. Validate thời gian
     * this.validateTime(req.getStartDateTime(), req.getEndDateTime());
     * 
     * // 4. Check trùng giờ (dành cho pitch hiện tại)
     * boolean exists = this.bookingRepository
     * .existsByPitchIdAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNot(
     * booking.getPitch().getId(),
     * req.getEndDateTime(),
     * req.getStartDateTime(),
     * booking.getId());
     * 
     * if (exists)
     * throw new BadRequestException("Khung giờ này đã được đặt");
     * 
     * // 5. Update các trường còn lại
     * booking.setStartDateTime(req.getStartDateTime());
     * booking.setEndDateTime(req.getEndDateTime());
     * booking.setShirtOption(
     * req.getShirtOption() != null ? req.getShirtOption() :
     * ShirtOptionEnum.WITHOUT_PITCH_SHIRT);
     * booking.setContactPhone(resolveContactPhone(booking.getUser(),
     * req.getContactPhone()));
     * 
     * // 6. Lưu và trả response
     * bookingRepository.save(booking);
     * return convertToResUpdateBookingDTO(booking);
     * }
     */

    @Transactional
    public ResUpdateBookingDTO updateBooking(@NonNull Long id, ReqUpdateBookingDTO req)
            throws IdInvalidException {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Booking không tồn tại"));

        // 1. Đổi user (nếu có)
        if (req.getUserId() != null && !req.getUserId().equals(booking.getUser().getId())) {
            User newUser = userService.getUserById(req.getUserId());
            if (newUser.getStatus() != UserStatusEnum.ACTIVE) {
                throw new BadRequestException("Tài khoản mới không đủ điều kiện đặt lịch");
            }
            booking.setUser(newUser);
        }

        // 2. Đổi pitch (nếu có)
        Pitch pitch = booking.getPitch();
        Long newPitchId = req.getPitchId();
        if (newPitchId != null && !newPitchId.equals(pitch.getId())) {
            pitch = pitchService.getPitchById(newPitchId);
            booking.setPitch(pitch);
        }

        // 3. Resolve thời gian
        LocalDateTime start = req.getStartDateTime() != null
                ? req.getStartDateTime()
                : booking.getStartDateTime();

        LocalDateTime end = req.getEndDateTime() != null
                ? req.getEndDateTime()
                : booking.getEndDateTime();

        // 4. Validate thời gian
        this.validateTime(start, end);

        // 5. Check trùng lịch
        boolean exists = bookingRepository
                .existsByPitchIdAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNot(
                        pitch.getId(),
                        end,
                        start,
                        booking.getId());

        if (exists) {
            throw new BadRequestException("Khung giờ này đã được đặt");
        }

        // 6. Tính lại duration
        long durationMinutes = calculateDurationMinutes(start, end);

        // 7. Tính lại giá
        BigDecimal pricePerHour = pitch.getPricePerHour();
        if (pricePerHour == null || pricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Giá sân không hợp lệ");
        }

        BigDecimal totalPrice = calculateTotalPrice(pricePerHour, durationMinutes);

        // 8. Update fields
        booking.setStartDateTime(start);
        booking.setEndDateTime(end);
        booking.setDurationMinutes(durationMinutes);
        booking.setTotalPrice(totalPrice);
        booking.setShirtOption(
                req.getShirtOption() != null
                        ? req.getShirtOption()
                        : booking.getShirtOption());
        booking.setContactPhone(
                resolveContactPhone(booking.getUser(), req.getContactPhone()));
        // 9. Save
        bookingRepository.save(booking);

        return convertToResUpdateBookingDTO(booking);
    }

    // delete
    public void deleteBooking(@NonNull Long id) throws IdInvalidException {

        // Booking booking = this.getBookingById(id);
        this.getBookingById(id);
        this.bookingRepository.deleteById(id);
    }

    // xử lý thời gian

    private void validateTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Không thể đặt lịch trong quá khứ");
        }
    }

    // khiểm tra trùng lịch
    private void checkOverlapping(Long pitchId, ReqCreateBookingDTO req) {
        boolean exists = bookingRepository
                .existsByPitchIdAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        pitchId,
                        req.getEndDateTime(),
                        req.getStartDateTime());

        if (exists) {
            throw new BadRequestException("Khung giờ này đã được đặt");
        }
    }

    // kiểm tra số điện thoại
    private String resolveContactPhone(User user, String phone) {
        if (phone != null && !phone.isBlank())
            return phone;
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank())
            return user.getPhoneNumber();
        throw new BadRequestException("Vui lòng cung cấp số điện thoại liên hệ");
    }

    private ResCreateBookingDTO convertToResCreateBookingDTO(Booking booking) {
        String userName = booking.getUser().getFullName();
        if (userName == null || userName.isBlank()) {
            userName = booking.getUser().getName(); // fallback sang name nếu fullName null
        }

        return new ResCreateBookingDTO(
                booking.getId(),
                booking.getUser().getId(),
                userName,
                booking.getPitch().getId(),
                booking.getPitch().getName(),
                booking.getStartDateTime(),
                booking.getEndDateTime(),
                booking.getShirtOption(),
                booking.getContactPhone(),
                booking.getDurationMinutes(),
                booking.getTotalPrice(),
                booking.getCreatedAt(),
                booking.getCreatedBy());
    }

    // convert update
    public ResUpdateBookingDTO convertToResUpdateBookingDTO(Booking booking) {
        ResUpdateBookingDTO res = new ResUpdateBookingDTO();

        // Thông tin booking cơ bản
        res.setId(booking.getId());
        res.setStartDateTime(booking.getStartDateTime());
        res.setEndDateTime(booking.getEndDateTime());
        res.setShirtOption(booking.getShirtOption());
        res.setContactPhone(booking.getContactPhone());
        res.setDurationMinutes(booking.getDurationMinutes());
        res.setTotalPrice(booking.getTotalPrice());
        // Thông tin user
        res.setUserId(booking.getUser() != null ? booking.getUser().getId() : null);
        String userName = (booking.getUser() != null && booking.getUser().getFullName() != null
                && !booking.getUser().getFullName().isBlank())
                        ? booking.getUser().getFullName()
                        : (booking.getUser() != null ? booking.getUser().getName() : null);
        res.setUserName(userName);

        // Thông tin pitch
        res.setPitchId(booking.getPitch() != null ? booking.getPitch().getId() : null);
        res.setPitchName(booking.getPitch() != null ? booking.getPitch().getName() : null);

        // Thông tin audit
        res.setUpdatedAt(booking.getUpdatedAt());
        res.setUpdatedBy(booking.getUpdatedBy());

        return res;
    }

    // convert get all
    public ResBookingDTO convertToResBookingDTO(Booking booking) {
        ResBookingDTO res = new ResBookingDTO();

        String userName = booking.getUser().getFullName();
        if (userName == null || userName.isBlank()) {
            userName = booking.getUser().getName(); // fallback sang name nếu fullName null
        }

        res.setId(booking.getId());
        res.setUserId(booking.getUser().getId());
        res.setUserName(userName);
        res.setPitchId(booking.getPitch().getId());
        res.setPitchName(booking.getPitch().getName());
        res.setStartDateTime(booking.getStartDateTime());
        res.setEndDateTime(booking.getEndDateTime());
        res.setShirtOption(booking.getShirtOption());
        res.setContactPhone(booking.getContactPhone());
        res.setDurationMinutes(booking.getDurationMinutes());
        res.setTotalPrice(booking.getTotalPrice());
        res.setStatus(booking.getStatus());
        res.setDeletedByUser(booking.getDeletedByUser());
        res.setCreatedAt(booking.getCreatedAt());
        res.setUpdatedAt(booking.getUpdatedAt());
        res.setCreatedBy(booking.getCreatedBy());
        res.setUpdatedBy(booking.getUpdatedBy());
        return res;
    }

    // ==========Client=========
    // Lấy booking chỉ của user
    public Booking getBookingByIdForUser(@NonNull Long id, String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        Booking booking = getBookingById(id);

        if (booking.getDeletedByUser()) {
            throw new BadRequestException("Booking đã bị xóa khỏi lịch sử");
        }

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền truy cập booking này");
        }
        return booking;
    }

    // Update booking của user
    @Transactional
    public ResUpdateBookingDTO updateBookingForUser(
            @NonNull Long id,
            ReqUpdateBookingDTO req,
            String email) throws IdInvalidException {

        Booking booking = getBookingByIdForUser(id, email);

        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new BadRequestException("Không thể cập nhật booking đã bị hủy");
        }

        if (booking.getDeletedByUser()) {
            throw new BadRequestException("Booking đã bị xóa khỏi lịch sử");
        }

        return this.updateBooking(id, req);
    }

    // Delete booking của user
    // @Transactional
    // public void deleteBookingForUser(@NonNull Long id, String email) throws
    // IdInvalidException {
    // this.getBookingByIdForUser(id, email);
    // this.deleteBooking(id);
    // }
    @Transactional
    public void deleteBookingForUser(@NonNull Long id, String email) throws IdInvalidException {
        Booking booking = getBookingByIdForUser(id, email);

        if (booking.getDeletedByUser()) {
            throw new BadRequestException("Booking đã bị xóa khỏi lịch sử");
        }

        // if (booking.getStatus() != BookingStatusEnum.CANCELLED) {
        //     throw new BadRequestException("Vui lòng hủy booking trước khi xóa khỏi lịch sử");
        // }

        booking.setDeletedByUser(true);
        bookingRepository.save(booking);
    }

    // Get all bookings của user
    public ResultPaginationDTO getAllBookingsOfUser(String email, @NonNull Pageable pageable)
            throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        // Specification<Booking> spec = (root, query, cb) ->
        // cb.equal(root.get("user").get("id"), user.getId());
        Specification<Booking> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("user").get("id"), user.getId()),
                cb.isFalse(root.get("deletedByUser")));
        return getAllBookings(spec, pageable);
    }

    private static final long MIN_BOOKING_MINUTES = 30;

    private long calculateDurationMinutes(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();

        if (minutes < MIN_BOOKING_MINUTES) {
            throw new BadRequestException(
                    minutes <= 0
                            ? "Thời lượng đặt sân không hợp lệ"
                            : "Thời lượng đặt sân tối thiểu là " + MIN_BOOKING_MINUTES + " phút");
        }

        return minutes;
    }

    private BigDecimal calculateTotalPrice(
            BigDecimal pricePerHour,
            long durationMinutes) {
        // pricePerHour * durationMinutes / 60
        return pricePerHour
                .multiply(BigDecimal.valueOf(durationMinutes))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void cancelBookingForUser(@NonNull Long id, String email) throws IdInvalidException {

        Booking booking = getBookingByIdForUser(id, email);

        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new BadRequestException("Booking đã bị hủy");
        }

        // Không cho hủy khi đã bắt đầu
        if (booking.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Không thể hủy booking đã hoặc đang diễn ra");
        }

        if (booking.getDeletedByUser()) {
            throw new BadRequestException("Booking đã bị xóa khỏi lịch sử");
        }

        booking.setStatus(BookingStatusEnum.CANCELLED);
        bookingRepository.save(booking);
    }

}
