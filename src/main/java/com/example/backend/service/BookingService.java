package com.example.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.BookingEquipment;
import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.entity.PitchHourlyPrice;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.booking.ReqCreateBookingDTO;
import com.example.backend.domain.request.booking.ReqUpdateBookingDTO;
import com.example.backend.domain.response.booking.ResBookingDTO;
import com.example.backend.domain.response.booking.ResCreateBookingDTO;
import com.example.backend.domain.response.booking.ResUpdateBookingDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.repository.BookingEquipmentRepository;
import com.example.backend.repository.BookingRepository;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.user.UserStatusEnum;
import com.example.backend.util.constant.notification.NotificationTypeEnum;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final UserService userService;
    private final PitchService pitchService;
    private final NotificationService notificationService;

    private static final List<BookingStatusEnum> OCCUPYING_STATUSES = List.of(
            BookingStatusEnum.ACTIVE,
            BookingStatusEnum.PAID);

    public BookingService(BookingRepository bookingRepository, BookingEquipmentRepository bookingEquipmentRepository,
            UserService userService,
            PitchService pitchService,
            NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.bookingEquipmentRepository = bookingEquipmentRepository;
        this.userService = userService;
        this.pitchService = pitchService;
        this.notificationService = notificationService;
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

        // 8. Lấy giá mặc định của sân và Validate (giá fallback khi không nằm trong khung giờ giá)
        BigDecimal basePricePerHour = pitch.getPricePerHour();
        if (basePricePerHour == null || basePricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Giá sân không hợp lệ");
        }

        // 9. Tính tổng tiền theo khung giờ giá (nếu có) — vẫn giữ logic tỉ lệ theo phút
        BigDecimal totalPrice = calculateTotalPrice(pitch, req.getStartDateTime(), req.getEndDateTime(), durationMinutes);

        // 10. Resolve contactPhone
        String contactPhone = this.resolveContactPhone(user, req.getContactPhone());

        boolean requiresAdminApproval = req.getUserId() == null;

        // 11. Lưu booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setPitch(pitch);
        booking.setStartDateTime(req.getStartDateTime());
        booking.setEndDateTime(req.getEndDateTime());
        booking.setContactPhone(contactPhone);
        booking.setDurationMinutes(durationMinutes);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(requiresAdminApproval ? BookingStatusEnum.PENDING : BookingStatusEnum.ACTIVE);

        this.bookingRepository.save(booking);

        String pitchName = pitch.getName() != null ? pitch.getName() : "sân";
        String bookingTime = booking.getStartDateTime().toString().replace("T", " ").substring(0, 16);

        if (requiresAdminApproval) {
            String userMsg = String.format(
                    "Yêu cầu đặt sân đã được gửi! Booking #%d – %s lúc %s đang chờ admin xác nhận.",
                    booking.getId(), pitchName, bookingTime);
            notificationService.createAndPush(user, NotificationTypeEnum.BOOKING_PENDING_CONFIRMATION, userMsg, booking.getId());

            String requesterName = user.getFullName() != null && !user.getFullName().isBlank()
                    ? user.getFullName()
                    : user.getName();
            String adminMsg = String.format(
                    "Có yêu cầu đặt sân mới cần xác nhận. Booking #%d – %s đặt sân %s lúc %s.",
                    booking.getId(), requesterName, pitchName, bookingTime);
            // Không gửi bản "admin" cho chính người đặt nếu họ là admin — đã có createAndPush phía user ở trên
            notificationService.notifyAdmins(NotificationTypeEnum.BOOKING_PENDING_CONFIRMATION, adminMsg, user.getId(), booking.getId());
        } else {
            String notifMsg = String.format("Đặt sân thành công! Booking #%d – %s lúc %s",
                    booking.getId(), pitchName, bookingTime);
            notificationService.createAndPush(user, NotificationTypeEnum.BOOKING_CREATED, notifMsg, booking.getId());
        }

        // 13. Trả response
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
                .existsByPitchIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNot(
                        pitch.getId(),
                        OCCUPYING_STATUSES,
                        end,
                        start,
                        booking.getId());

        if (exists) {
            throw new BadRequestException("Khung giờ này đã được đặt");
        }

        // 6. Tính lại duration
        long durationMinutes = calculateDurationMinutes(start, end);

        // 7. Tính lại giá theo khung giờ (nếu có)
        BigDecimal basePricePerHour = pitch.getPricePerHour();
        if (basePricePerHour == null || basePricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Giá sân không hợp lệ");
        }

        BigDecimal totalPrice = calculateTotalPrice(pitch, start, end, durationMinutes);

        // 8. Update fields
        booking.setStartDateTime(start);
        booking.setEndDateTime(end);
        booking.setDurationMinutes(durationMinutes);
        booking.setTotalPrice(totalPrice);
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
                .existsByPitchIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThan(
                        pitchId,
                        OCCUPYING_STATUSES,
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

        ResCreateBookingDTO res = new ResCreateBookingDTO();
        res.setId(booking.getId());
        res.setUserId(booking.getUser().getId());
        res.setUserName(userName);
        res.setPitchId(booking.getPitch().getId());
        res.setPitchName(booking.getPitch().getName());
        res.setStartDateTime(booking.getStartDateTime());
        res.setEndDateTime(booking.getEndDateTime());
        res.setContactPhone(booking.getContactPhone());
        res.setDurationMinutes(booking.getDurationMinutes());
        res.setTotalPrice(booking.getTotalPrice());
        res.setCreatedAt(booking.getCreatedAt());
        res.setCreatedBy(booking.getCreatedBy());
        return res;
    }

    // convert update
    public ResUpdateBookingDTO convertToResUpdateBookingDTO(Booking booking) {
        ResUpdateBookingDTO res = new ResUpdateBookingDTO();

        // Thông tin booking cơ bản
        res.setId(booking.getId());
        res.setStartDateTime(booking.getStartDateTime());
        res.setEndDateTime(booking.getEndDateTime());
        res.setContactPhone(booking.getContactPhone());
        res.setDurationMinutes(booking.getDurationMinutes());
        res.setTotalPrice(booking.getTotalPrice());
        res.setStatus(booking.getStatus());
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
    public Booking getBookingByIdForUserIncludingDeleted(@NonNull Long id, String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        Booking booking = getBookingById(id);

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Không có quyền truy cập booking này");
        }
        return booking;
    }

    // Lấy booking chỉ của user
    public Booking getBookingByIdForUser(@NonNull Long id, String email) throws IdInvalidException {
        Booking booking = getBookingByIdForUserIncludingDeleted(id, email);
        if (booking.getDeletedByUser()) {
            throw new BadRequestException("Booking đã bị xóa khỏi lịch sử");
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

        if (booking.getStatus() == BookingStatusEnum.PAID) {
            throw new BadRequestException("Không thể cập nhật booking đã thanh toán");
        }

        if (booking.getDeletedByUser()) {
            throw new BadRequestException("Booking đã bị xóa khỏi lịch sử");
        }

        this.updateBooking(id, req);

        Booking updatedBooking = getBookingByIdForUser(id, email);
        updatedBooking.setStatus(BookingStatusEnum.PENDING);
        bookingRepository.save(updatedBooking);

        String pitchName = updatedBooking.getPitch() != null && updatedBooking.getPitch().getName() != null
                ? updatedBooking.getPitch().getName()
                : "sân";
        String bookingTime = updatedBooking.getStartDateTime().toString().replace("T", " ").substring(0, 16);
        User bookingUser = updatedBooking.getUser();

        String userMsg = String.format(
                "Booking #%d – %s lúc %s đã được cập nhật và đang chờ admin xác nhận lại.",
                updatedBooking.getId(), pitchName, bookingTime);
        notificationService.createAndPush(
                bookingUser,
                NotificationTypeEnum.BOOKING_PENDING_CONFIRMATION,
                userMsg,
                updatedBooking.getId());

        String requesterName = bookingUser.getFullName() != null && !bookingUser.getFullName().isBlank()
                ? bookingUser.getFullName()
                : bookingUser.getName();
        String adminMsg = String.format(
                "Booking #%d của %s vừa được cập nhật sang sân %s lúc %s và cần xác nhận lại.",
                updatedBooking.getId(), requesterName, pitchName, bookingTime);
        notificationService.notifyAdmins(
                NotificationTypeEnum.BOOKING_PENDING_CONFIRMATION,
                adminMsg,
                bookingUser.getId(),
                updatedBooking.getId());

        return convertToResUpdateBookingDTO(updatedBooking);
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

        ensureBookingEquipmentsReturnedBeforeUserDelete(booking);

        // if (booking.getStatus() != BookingStatusEnum.CANCELLED) {
        // throw new BadRequestException("Vui lòng hủy booking trước khi xóa khỏi lịch
        // sử");
        // }

        booking.setDeletedByUser(true);
        bookingRepository.save(booking);
    }

    private void ensureBookingEquipmentsReturnedBeforeUserDelete(Booking booking) {
        List<BookingEquipment> bookingEquipments = bookingEquipmentRepository.findByBookingId(booking.getId());
        boolean hasBorrowedEquipment = bookingEquipments.stream()
                .anyMatch(line -> !line.isDeletedByClient()
                        && line.getStatus() == BookingEquipmentStatusEnum.BORROWED);

        if (hasBorrowedEquipment) {
            throw new BadRequestException("Vui lòng trả hết thiết bị mượn trước khi xóa khỏi lịch sử");
        }
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

    // ========================= Hourly pricing =========================

    private BigDecimal calculateTotalPrice(
            @NonNull Pitch pitch,
            @NonNull LocalDateTime start,
            @NonNull LocalDateTime end,
            long durationMinutes) {

        // Bảo đảm giá mặc định luôn hợp lệ (đã validate ở caller nhưng phòng thủ thêm)
        BigDecimal basePricePerHour = pitch.getPricePerHour();
        if (basePricePerHour == null || basePricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Giá sân không hợp lệ");
        }

        // Tính tổng tiền theo giá fallback (giữ đúng pattern v1)
        BigDecimal total = calculateTotalPrice(basePricePerHour, durationMinutes);

        List<PitchHourlyPrice> rules = pitch.getHourlyPrices();
        if (rules == null || rules.isEmpty()) {
            return total;
        }

        // Đếm số phút mà booking rơi vào từng khung giá
        long[] minutesByRuleIndex = new long[rules.size()];

        for (long i = 0; i < durationMinutes; i++) {
            LocalDateTime t = start.plusMinutes(i);
            LocalTime tod = t.toLocalTime();
            int matchedIndex = resolveMatchedHourlyPriceIndex(rules, tod);
            if (matchedIndex >= 0) {
                minutesByRuleIndex[matchedIndex]++;
            }
        }

        // Cộng phần chênh lệch: (giá rule - giá base) * số phút rule / 60
        for (int idx = 0; idx < rules.size(); idx++) {
            long ruleMinutes = minutesByRuleIndex[idx];
            if (ruleMinutes <= 0) continue;

            PitchHourlyPrice rule = rules.get(idx);
            BigDecimal rulePricePerHour = rule.getPricePerHour();
            if (rulePricePerHour == null || rulePricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Giá theo khung giờ không hợp lệ");
            }

            BigDecimal deltaPerHour = rulePricePerHour.subtract(basePricePerHour);
            BigDecimal delta = deltaPerHour
                    .multiply(BigDecimal.valueOf(ruleMinutes))
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

            total = total.add(delta);
        }

        return total;
    }

    // Trả về index rule khớp theo thời gian trong ngày (tính end-exclusive), ưu tiên rule theo thứ tự lưu
    private int resolveMatchedHourlyPriceIndex(List<PitchHourlyPrice> rules, LocalTime tod) {
        if (rules == null || rules.isEmpty()) return -1;

        for (int idx = 0; idx < rules.size(); idx++) {
            PitchHourlyPrice rule = rules.get(idx);
            LocalTime startTime = rule.getStartTime();
            LocalTime endTime = rule.getEndTime();
            if (startTime == null || endTime == null) continue;
            if (isTimeInRule(tod, startTime, endTime)) return idx;
        }
        return -1;
    }

    // start <= tod < end (nếu start < end); hoặc tod >= start || tod < end (nếu start > end)
    private boolean isTimeInRule(LocalTime tod, LocalTime startTime, LocalTime endTime) {
        if (tod == null || startTime == null || endTime == null) return false;

        if (startTime.equals(endTime)) {
            return false; // khung 0 phút không hợp lệ
        }

        if (startTime.isBefore(endTime)) {
            // Không qua nửa đêm: [start, end)
            return (tod.equals(startTime) || tod.isAfter(startTime)) && tod.isBefore(endTime);
        }

        // Qua nửa đêm: [start, 24h) U [00:00, end)
        return !tod.isBefore(startTime) || tod.isBefore(endTime);
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

        String pitchName = booking.getPitch() != null ? booking.getPitch().getName() : "sân";
        String msg = String.format("Booking #%d – %s lúc %s đã được hủy.",
                booking.getId(),
                pitchName,
                booking.getStartDateTime().toString().replace("T", " ").substring(0, 16));
        notificationService.createAndPush(booking.getUser(), NotificationTypeEnum.BOOKING_REJECTED, msg, booking.getId());
    }

    @Transactional
    public void approveBooking(@NonNull Long id) throws IdInvalidException {
        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatusEnum.ACTIVE || booking.getStatus() == BookingStatusEnum.PAID) {
            throw new BadRequestException("Booking này đã được xác nhận trước đó");
        }

        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new BadRequestException("Booking đã bị hủy, không thể xác nhận");
        }

        // Khi nới lỏng tạo booking PENDING, đến bước duyệt cần khóa cứng theo ACTIVE/PAID
        // để tránh 2 booking cùng được duyệt trên cùng khung giờ.
        boolean occupiedByApprovedBooking = bookingRepository
                .existsByPitchIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNot(
                        booking.getPitch().getId(),
                        List.of(BookingStatusEnum.ACTIVE, BookingStatusEnum.PAID),
                        booking.getEndDateTime(),
                        booking.getStartDateTime(),
                        booking.getId());
        if (occupiedByApprovedBooking) {
            throw new BadRequestException("Khung giờ này đã có booking được duyệt trước đó");
        }

        booking.setStatus(BookingStatusEnum.ACTIVE);
        bookingRepository.save(booking);

        // Tự động từ chối các booking PENDING trùng khung giờ sau khi đã duyệt booking hiện tại.
        List<Booking> overlappingPending = bookingRepository
                .findByPitchIdAndStatusAndStartDateTimeLessThanAndEndDateTimeGreaterThanAndIdNotOrderByStartDateTimeAsc(
                        booking.getPitch().getId(),
                        BookingStatusEnum.PENDING,
                        booking.getEndDateTime(),
                        booking.getStartDateTime(),
                        booking.getId());
        for (Booking pending : overlappingPending) {
            pending.setStatus(BookingStatusEnum.CANCELLED);
        }
        if (!overlappingPending.isEmpty()) {
            bookingRepository.saveAll(overlappingPending);
        }

        String pitchName = booking.getPitch() != null ? booking.getPitch().getName() : "sân";
        String msg = String.format("Booking #%d – %s lúc %s đã được admin xác nhận.",
                booking.getId(),
                pitchName,
                booking.getStartDateTime().toString().replace("T", " ").substring(0, 16));
        notificationService.createAndPush(booking.getUser(), NotificationTypeEnum.BOOKING_APPROVED, msg, booking.getId());

        // Thông báo rõ lý do cho các user bị từ chối do trùng giờ với booking vừa được duyệt.
        for (Booking pending : overlappingPending) {
            String rejectMsg = String.format(
                    "Booking #%d – %s lúc %s đã bị từ chối vì khung giờ đã được admin duyệt cho booking khác (Booking #%d).",
                    pending.getId(),
                    pitchName,
                    pending.getStartDateTime().toString().replace("T", " ").substring(0, 16),
                    booking.getId());
            notificationService.createAndPush(pending.getUser(), NotificationTypeEnum.BOOKING_REJECTED, rejectMsg, pending.getId());
        }
    }

    @Transactional
    public void rejectBooking(@NonNull Long id) throws IdInvalidException {
        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatusEnum.ACTIVE || booking.getStatus() == BookingStatusEnum.PAID) {
            throw new BadRequestException("Booking này đã được xác nhận, không thể từ chối");
        }

        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new BadRequestException("Booking đã bị hủy trước đó");
        }

        booking.setStatus(BookingStatusEnum.CANCELLED);
        bookingRepository.save(booking);

        String pitchName = booking.getPitch() != null ? booking.getPitch().getName() : "sân";
        String msg = String.format("Booking #%d – %s lúc %s đã bị admin từ chối.",
                booking.getId(),
                pitchName,
                booking.getStartDateTime().toString().replace("T", " ").substring(0, 16));
        notificationService.createAndPush(booking.getUser(), NotificationTypeEnum.BOOKING_REJECTED, msg, booking.getId());
    }

}
