package com.example.backend.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.Payment;
import com.example.backend.domain.response.revenue.ResRevenueDashboardDTO;
import com.example.backend.domain.response.revenue.RevenueByPitchDTO;
import com.example.backend.domain.response.revenue.RevenuePointDTO;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.PitchRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.payment.PaymentStatusEnum;

@Service
public class RevenueService {

    // Inject repository để lấy dữ liệu từ database
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PitchRepository pitchRepository;

    public RevenueService(PaymentRepository paymentRepository, BookingRepository bookingRepository,
            UserRepository userRepository, PitchRepository pitchRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.pitchRepository = pitchRepository;
    }

    public ResRevenueDashboardDTO getDashboard() {

        long totalUsers = userRepository.count();
        long totalPitches = pitchRepository.count();

        // Lấy tất cả payment có trạng thái PAID
        List<Payment> paidPayments = paymentRepository.findByStatus(PaymentStatusEnum.PAID);

        // Lấy ngày hiện tại (server time)
        LocalDate today = LocalDate.now();

        // Lấy tuần
        WeekFields weekFields = WeekFields.ISO;

        int currentWeek = today.get(weekFields.weekOfWeekBasedYear());
        int currentWeekYear = today.get(weekFields.weekBasedYear());

        // ===== Các biến tổng hợp =====
        BigDecimal totalRevenue = BigDecimal.ZERO; // Tổng doanh thu toàn bộ
        BigDecimal todayRevenue = BigDecimal.ZERO; // Doanh thu hôm nay
        BigDecimal weekRevenue = BigDecimal.ZERO; // Doanh thu tuần nay
        BigDecimal monthRevenue = BigDecimal.ZERO; // Doanh thu tháng hiện tại

        // Map lưu doanh thu theo ngày (dùng cho biểu đồ line chart)
        Map<LocalDate, BigDecimal> revenueByDateMap = new java.util.HashMap<>();

        // Map lưu doanh thu theo sân (key = pitchId)
        Map<Long, RevenueByPitchDTO> revenueByPitchMap = new java.util.HashMap<>();

        // ===== Duyệt toàn bộ payment 1 lần duy nhất (O(n)) =====
        for (Payment payment : paidPayments) {

            BigDecimal amount = payment.getAmount(); // Số tiền thanh toán
            LocalDate paidDate = toLocalDate(payment.getPaidAt()); // Ngày thanh toán

            // ===== 1. Cộng tổng doanh thu =====
            totalRevenue = totalRevenue.add(amount);

            // ===== 2. Nếu thanh toán hôm nay =====
            if (paidDate.equals(today)) {
                todayRevenue = todayRevenue.add(amount);
            }

            // ===== 3. Nếu thanh toán trong tháng hiện tại =====
            if (paidDate.getMonth() == today.getMonth()
                    && paidDate.getYear() == today.getYear()) {
                monthRevenue = monthRevenue.add(amount);
            }

            // ===== 3.5 Nếu thanh toán trong tuần hiện tại =====
            int paymentWeek = paidDate.get(weekFields.weekOfWeekBasedYear());
            int paymentWeekYear = paidDate.get(weekFields.weekBasedYear());

            if (paymentWeek == currentWeek && paymentWeekYear == currentWeekYear) {
                weekRevenue = weekRevenue.add(amount);
            }

            // ===== 4. Gom nhóm doanh thu theo ngày =====
            // Nếu ngày đã tồn tại thì cộng thêm, chưa tồn tại thì tạo mới
            revenueByDateMap.merge(
                    paidDate,
                    amount,
                    BigDecimal::add);

            // ===== 5. Gom nhóm doanh thu theo sân =====
            Long pitchId = payment.getBooking().getPitch().getId();
            String pitchName = payment.getBooking().getPitch().getName();

            revenueByPitchMap.merge(
                    pitchId,
                    // Nếu chưa có thì tạo mới DTO
                    RevenueByPitchDTO.builder()
                            .pitchId(pitchId)
                            .pitchName(pitchName)
                            .revenue(amount)
                            .build(),
                    // Nếu đã tồn tại thì tạo object mới với revenue cộng dồn
                    (existing, incoming) -> RevenueByPitchDTO.builder()
                            .pitchId(existing.getPitchId())
                            .pitchName(existing.getPitchName())
                            .revenue(existing.getRevenue()
                                    .add(incoming.getRevenue()))
                            .build());
        }

        // ===== Chuyển Map doanh thu theo ngày thành List và sort theo ngày tăng dần
        // =====
        List<RevenuePointDTO> revenueByDate = revenueByDateMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> RevenuePointDTO.builder()
                        .label(e.getKey().toString()) // format yyyy-MM-dd
                        .revenue(e.getValue())
                        .build())
                .toList();

        // ===== Chuyển Map doanh thu theo sân thành List =====
        List<RevenueByPitchDTO> revenueByPitch = new java.util.ArrayList<>(revenueByPitchMap.values());

        // ===== Thống kê booking =====
        long totalBookings = bookingRepository.count(); // Tổng booking
        long paidBookings = bookingRepository.countByStatus(BookingStatusEnum.PAID); // Booking đã thanh toán
        long cancelledBookings = bookingRepository.countByStatus(BookingStatusEnum.CANCELLED); // Booking đã hủy

        // ===== Build DTO trả về =====
        return ResRevenueDashboardDTO.builder()
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .weekRevenue(weekRevenue)
                .monthRevenue(monthRevenue)
                .totalBookings(totalBookings)
                .paidBookings(paidBookings)
                .cancelledBookings(cancelledBookings)
                .totalUsers(totalUsers)
                .totalPitches(totalPitches)
                .revenueByDate(revenueByDate)
                .revenueByPitch(revenueByPitch)
                .build();
    }

    /**
     * Convert Instant (UTC) sang LocalDate theo múi giờ hệ thống
     */
    private LocalDate toLocalDate(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
