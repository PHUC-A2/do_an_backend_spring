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
import com.example.backend.tenant.TenantContext;
import com.example.backend.util.SecurityRbac;
import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.equipment.EquipmentStatusEnum;
import com.example.backend.util.constant.payment.PaymentStatusEnum;
import com.example.backend.util.constant.pitch.PitchStatusEnum;
import com.example.backend.util.constant.review.ReviewStatusEnum;
import com.example.backend.util.constant.user.UserStatusEnum;

import lombok.RequiredArgsConstructor;

/**
 * Thống kê dashboard admin theo tenant hiện tại (user/role/permission vẫn toàn cục).
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
        long tid = TenantContext.requireCurrentTenantId();
        long rolesTotal;
        if (SecurityRbac.hasAllAuthority()) {
            rolesTotal = roleRepository.count();
        } else {
            long inShop = roleRepository.countByTenant_Id(tid);
            long withView = roleRepository.existsByNameAndTenantIsNull("VIEW") ? 1L : 0L;
            rolesTotal = inShop + withView;
        }

        BigDecimal pendingAmt = paymentRepository.sumAmountByStatusAndTenantId(tid, PaymentStatusEnum.PENDING);
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
                .bookingsTotalVisible(bookingRepository.countByDeletedByUserFalseAndTenantId(tid))
                .bookingsPending(bookingRepository.countByDeletedByUserFalseAndStatusAndTenantId(BookingStatusEnum.PENDING, tid))
                .bookingsActive(bookingRepository.countByDeletedByUserFalseAndStatusAndTenantId(BookingStatusEnum.ACTIVE, tid))
                .bookingsPaidStatus(bookingRepository.countByDeletedByUserFalseAndStatusAndTenantId(BookingStatusEnum.PAID, tid))
                .bookingsCancelled(bookingRepository.countByDeletedByUserFalseAndStatusAndTenantId(BookingStatusEnum.CANCELLED, tid))
                .paymentsTotal(paymentRepository.countByTenantId(tid))
                .paymentsPendingCount(paymentRepository.countByStatusAndTenantId(PaymentStatusEnum.PENDING, tid))
                .paymentsPaidCount(paymentRepository.countByStatusAndTenantId(PaymentStatusEnum.PAID, tid))
                .paymentsCancelledCount(paymentRepository.countByStatusAndTenantId(PaymentStatusEnum.CANCELLED, tid))
                .paymentsPendingAmount(pendingAmt)
                .pitchesTotal(pitchRepository.countByTenantId(tid))
                .pitchesActive(pitchRepository.countByStatusAndTenantId(PitchStatusEnum.ACTIVE, tid))
                .pitchesMaintenance(pitchRepository.countByStatusAndTenantId(PitchStatusEnum.MAINTENANCE, tid))
                .equipmentsTotal(equipmentRepository.countByTenantId(tid))
                .equipmentsActive(equipmentRepository.countByStatusAndTenantId(EquipmentStatusEnum.ACTIVE, tid))
                .equipmentsMaintenance(equipmentRepository.countByStatusAndTenantId(EquipmentStatusEnum.MAINTENANCE, tid))
                .equipmentsInactive(equipmentRepository.countByStatusAndTenantId(EquipmentStatusEnum.INACTIVE, tid))
                .equipmentsBroken(equipmentRepository.countByStatusAndTenantId(EquipmentStatusEnum.BROKEN, tid))
                .equipmentsLost(equipmentRepository.countByStatusAndTenantId(EquipmentStatusEnum.LOST, tid))
                .pitchEquipmentLinks(pitchEquipmentRepository.countByTenantId(tid))
                .bookingEquipmentsTotal(bookingEquipmentRepository.countByTenantId(tid))
                .bookingEquipmentsBorrowed(bookingEquipmentRepository.countByStatusAndTenantId(BookingEquipmentStatusEnum.BORROWED, tid))
                .bookingEquipmentsReturned(bookingEquipmentRepository.countByStatusAndTenantId(BookingEquipmentStatusEnum.RETURNED, tid))
                .bookingEquipmentsLost(bookingEquipmentRepository.countByStatusAndTenantId(BookingEquipmentStatusEnum.LOST, tid))
                .bookingEquipmentsDamaged(bookingEquipmentRepository.countByStatusAndTenantId(BookingEquipmentStatusEnum.DAMAGED, tid))
                .bookingEquipmentsAwaitingAdminConfirm(bookingEquipmentRepository.countByReturnAdminConfirmedFalseAndTenantId(tid))
                .equipmentBorrowLogsTotal(equipmentBorrowLogRepository.countByTenantId(tid))
                .reviewsTotal(reviewRepository.countByTenantId(tid))
                .reviewsPending(reviewRepository.countByStatusAndTenantId(ReviewStatusEnum.PENDING, tid))
                .reviewsApproved(reviewRepository.countByStatusAndTenantId(ReviewStatusEnum.APPROVED, tid))
                .reviewsHidden(reviewRepository.countByStatusAndTenantId(ReviewStatusEnum.HIDDEN, tid))
                .reviewMessagesTotal(reviewMessageRepository.count())
                .notificationsTotal(notificationRepository.countByTenantId(tid))
                .notificationsUnread(notificationRepository.countByIsReadFalseAndTenantId(tid))
                .rolesTotal(rolesTotal)
                .permissionsTotal(permissionRepository.count())
                .aiApiKeysTotal(aiApiKeyRepository.countByTenantId(tid))
                .aiApiKeysActive(aiApiKeyRepository.countByActiveTrueAndTenantId(tid))
                .aiChatSessionsTotal(aiChatSessionRepository.countByTenantId(tid))
                .build();
    }
}
