package com.example.backend.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.response.dashboard.ResAdminSystemOverviewDTO;
import com.example.backend.repository.AiApiKeyRepository;
import com.example.backend.repository.AiChatSessionRepository;
import com.example.backend.repository.BookingEquipmentRepository;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.EquipmentBorrowLogRepository;
import com.example.backend.repository.EquipmentRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.PermissionRepository;
import com.example.backend.repository.PitchEquipmentRepository;
import com.example.backend.repository.PitchRepository;
import com.example.backend.repository.ReviewMessageRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.equipment.EquipmentStatusEnum;
import com.example.backend.util.constant.payment.PaymentStatusEnum;
import com.example.backend.util.constant.pitch.PitchStatusEnum;
import com.example.backend.util.constant.review.ReviewStatusEnum;
import com.example.backend.util.constant.user.UserStatusEnum;

import lombok.RequiredArgsConstructor;

/**
 * Gom số liệu thống kê toàn hệ thống phục vụ dashboard admin.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PitchRepository pitchRepository;
    private final EquipmentRepository equipmentRepository;
    private final PitchEquipmentRepository pitchEquipmentRepository;
    private final BookingEquipmentRepository bookingEquipmentRepository;
    private final EquipmentBorrowLogRepository equipmentBorrowLogRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMessageRepository reviewMessageRepository;
    private final NotificationRepository notificationRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AiApiKeyRepository aiApiKeyRepository;
    private final AiChatSessionRepository aiChatSessionRepository;

    public ResAdminSystemOverviewDTO getSystemOverview() {
        BigDecimal pendingAmt = paymentRepository.sumAmountByStatus(PaymentStatusEnum.PENDING);
        if (pendingAmt == null) {
            pendingAmt = BigDecimal.ZERO;
        }

        return ResAdminSystemOverviewDTO.builder()
                .generatedAt(Instant.now().toEpochMilli())
                .usersTotal(userRepository.count())
                .usersActive(userRepository.countByStatus(UserStatusEnum.ACTIVE))
                .usersInactive(userRepository.countByStatus(UserStatusEnum.INACTIVE))
                .usersPendingVerification(userRepository.countByStatus(UserStatusEnum.PENDING_VERIFICATION))
                .usersBanned(userRepository.countByStatus(UserStatusEnum.BANNED))
                .usersDeleted(userRepository.countByStatus(UserStatusEnum.DELETED))
                .bookingsTotalVisible(bookingRepository.countByDeletedByUserFalse())
                .bookingsPending(bookingRepository.countByDeletedByUserFalseAndStatus(BookingStatusEnum.PENDING))
                .bookingsActive(bookingRepository.countByDeletedByUserFalseAndStatus(BookingStatusEnum.ACTIVE))
                .bookingsPaidStatus(bookingRepository.countByDeletedByUserFalseAndStatus(BookingStatusEnum.PAID))
                .bookingsCancelled(bookingRepository.countByDeletedByUserFalseAndStatus(BookingStatusEnum.CANCELLED))
                .paymentsTotal(paymentRepository.count())
                .paymentsPendingCount(paymentRepository.countByStatus(PaymentStatusEnum.PENDING))
                .paymentsPaidCount(paymentRepository.countByStatus(PaymentStatusEnum.PAID))
                .paymentsCancelledCount(paymentRepository.countByStatus(PaymentStatusEnum.CANCELLED))
                .paymentsPendingAmount(pendingAmt)
                .pitchesTotal(pitchRepository.count())
                .pitchesActive(pitchRepository.countByStatus(PitchStatusEnum.ACTIVE))
                .pitchesMaintenance(pitchRepository.countByStatus(PitchStatusEnum.MAINTENANCE))
                .equipmentsTotal(equipmentRepository.count())
                .equipmentsActive(equipmentRepository.countByStatus(EquipmentStatusEnum.ACTIVE))
                .equipmentsMaintenance(equipmentRepository.countByStatus(EquipmentStatusEnum.MAINTENANCE))
                .equipmentsInactive(equipmentRepository.countByStatus(EquipmentStatusEnum.INACTIVE))
                .equipmentsBroken(equipmentRepository.countByStatus(EquipmentStatusEnum.BROKEN))
                .equipmentsLost(equipmentRepository.countByStatus(EquipmentStatusEnum.LOST))
                .pitchEquipmentLinks(pitchEquipmentRepository.count())
                .bookingEquipmentsTotal(bookingEquipmentRepository.count())
                .bookingEquipmentsBorrowed(bookingEquipmentRepository.countByStatus(BookingEquipmentStatusEnum.BORROWED))
                .bookingEquipmentsReturned(bookingEquipmentRepository.countByStatus(BookingEquipmentStatusEnum.RETURNED))
                .bookingEquipmentsLost(bookingEquipmentRepository.countByStatus(BookingEquipmentStatusEnum.LOST))
                .bookingEquipmentsDamaged(bookingEquipmentRepository.countByStatus(BookingEquipmentStatusEnum.DAMAGED))
                .bookingEquipmentsAwaitingAdminConfirm(bookingEquipmentRepository.countByReturnAdminConfirmedFalse())
                .equipmentBorrowLogsTotal(equipmentBorrowLogRepository.count())
                .reviewsTotal(reviewRepository.count())
                .reviewsPending(reviewRepository.countByStatus(ReviewStatusEnum.PENDING))
                .reviewsApproved(reviewRepository.countByStatus(ReviewStatusEnum.APPROVED))
                .reviewsHidden(reviewRepository.countByStatus(ReviewStatusEnum.HIDDEN))
                .reviewMessagesTotal(reviewMessageRepository.count())
                .notificationsTotal(notificationRepository.count())
                .notificationsUnread(notificationRepository.countByIsReadFalse())
                .rolesTotal(roleRepository.count())
                .permissionsTotal(permissionRepository.count())
                .aiApiKeysTotal(aiApiKeyRepository.count())
                .aiApiKeysActive(aiApiKeyRepository.countByActiveTrue())
                .aiChatSessionsTotal(aiChatSessionRepository.count())
                .build();
    }
}
