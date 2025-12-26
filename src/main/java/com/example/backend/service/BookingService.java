package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

        User user;

        // 0. Nếu userId != null => admin đặt cho user khác
        if (req.getUserId() != null) {
            // Kiểm tra quyền admin
            // if (!SecurityUtil.isCurrentUserAdmin()) {
            //     throw new BadRequestException("Không có quyền đặt cho user khác");
            // }
            user = userService.getUserById(req.getUserId());
        } else {

            // 1. Lấy user từ token
            // user từ token (client đặt)
            String email = SecurityUtil.getCurrentUserLogin().orElse("");
            user = userService.handleGetUserByUsername(email);
        }

        // 2. Check user status
        if (user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new BadRequestException("Tài khoản không đủ điều kiện đặt lịch");
        }

        // 3. Lấy pitch
        Pitch pitch = pitchService.getPitchById(req.getPitchId());

        // 4. Validate thời gian
        this.validateTime(req.getStartDateTime(), req.getEndDateTime());

        // 5. Check trùng lịch
        this.checkOverlapping(pitch.getId(), req);

        // 6. Resolve contactPhone
        String contactPhone = this.resolveContactPhone(user, req.getContactPhone());

        // 7. Lưu booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setPitch(pitch);
        booking.setStartDateTime(req.getStartDateTime());
        booking.setEndDateTime(req.getEndDateTime());
        booking.setShirtOption(
                req.getShirtOption() != null ? req.getShirtOption() : ShirtOptionEnum.WITHOUT_PITCH_SHIRT);
        booking.setContactPhone(contactPhone);

        this.bookingRepository.save(booking);

        // 8. Trả response
        return this.convertToResCreateBookingDTO(booking);
    }

    // get all
    public ResultPaginationDTO getAllBookings(Specification<Booking> spec, Pageable pageable) {
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
    public Booking getBookingById(Long id) throws IdInvalidException {

        Optional<Booking> optionalBooking = this.bookingRepository.findById(id);
        if (optionalBooking.isPresent()) {
            return optionalBooking.get();
        }
        throw new IdInvalidException("Không tìm thấy Booking với ID = " + id);
    }

    @Transactional
    public ResUpdateBookingDTO updateBooking(Long id, ReqUpdateBookingDTO req) throws IdInvalidException {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Booking không tồn tại"));

        // 1. Nếu admin muốn đổi user
        if (req.getUserId() != null && !req.getUserId().equals(booking.getUser().getId())) {
            User newUser = userService.getUserById(req.getUserId());
            if (newUser.getStatus() != UserStatusEnum.ACTIVE) {
                throw new BadRequestException("Tài khoản mới không đủ điều kiện đặt lịch");
            }
            booking.setUser(newUser);
        }

        // 2. Nếu admin muốn đổi pitch
        if (req.getPitchId() != null && !req.getPitchId().equals(booking.getPitch().getId())) {
            Pitch newPitch = pitchService.getPitchById(req.getPitchId());
            booking.setPitch(newPitch);
        }

        // 3. Validate thời gian
        this.validateTime(req.getStartDateTime(), req.getEndDateTime());

        // 4. Check trùng giờ (dành cho pitch hiện tại)
        boolean exists = this.bookingRepository
                .existsByPitchIdAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNot(
                        booking.getPitch().getId(),
                        req.getEndDateTime(),
                        req.getStartDateTime(),
                        booking.getId());

        if (exists)
            throw new BadRequestException("Khung giờ này đã được đặt");

        // 5. Update các trường còn lại
        booking.setStartDateTime(req.getStartDateTime());
        booking.setEndDateTime(req.getEndDateTime());
        booking.setShirtOption(
                req.getShirtOption() != null ? req.getShirtOption() : ShirtOptionEnum.WITHOUT_PITCH_SHIRT);
        booking.setContactPhone(resolveContactPhone(booking.getUser(), req.getContactPhone()));

        // 6. Lưu và trả response
        bookingRepository.save(booking);
        return convertToResUpdateBookingDTO(booking);
    }

    // delete
    public void deleteBooking(Long id) throws IdInvalidException {

        Booking booking = this.getBookingById(id);
        this.bookingRepository.deleteById(booking.getId());
    }

    // ==============HELPER===========

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
        res.setCreatedAt(booking.getCreatedAt());
        res.setUpdatedAt(booking.getUpdatedAt());
        res.setCreatedBy(booking.getCreatedBy());
        res.setUpdatedBy(booking.getUpdatedBy());
        return res;
    }

    // ==========Client=========
    // Lấy booking chỉ của user
    public Booking getBookingByIdForUser(Long id, String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        Booking booking = getBookingById(id);
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền truy cập booking này");
        }
        return booking;
    }

    // Update booking của user
    @Transactional
    public ResUpdateBookingDTO updateBookingForUser(Long id, ReqUpdateBookingDTO req, String email)
            throws IdInvalidException {

        // Chỉ để kiểm tra quyền truy cập
        this.getBookingByIdForUser(id, email);

        // Thực hiện update
        return this.updateBooking(id, req);
    }

    // Delete booking của user
    @Transactional
    public void deleteBookingForUser(Long id, String email) throws IdInvalidException {
        this.getBookingByIdForUser(id, email);
        this.deleteBooking(id);
    }

    // Get all bookings của user
    public ResultPaginationDTO getAllBookingsOfUser(String email, Pageable pageable) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        Specification<Booking> spec = (root, query, cb) -> cb.equal(root.get("user").get("id"), user.getId());
        return getAllBookings(spec, pageable);
    }

}
