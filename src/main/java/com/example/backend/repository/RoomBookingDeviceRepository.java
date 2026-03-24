package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.example.backend.domain.entity.RoomBookingDevice;

/**
 * Repository cho dòng mượn/trả theo booking phòng (rooms).
 *
 * <p>
 * Clone theo repository {@code BookingEquipmentRepository} để FE có thể reuse luồng list + stats.
 * </p>
 */
public interface RoomBookingDeviceRepository
        extends JpaRepository<RoomBookingDevice, Long>, JpaSpecificationExecutor<RoomBookingDevice> {

    /** Lấy danh sách dòng mượn/trả theo booking phòng. */
    List<RoomBookingDevice> findByAssetUsage_Id(Long assetUsageId);

    /**
     * Aggregate: tổng số lượt mượn theo thiết bị (Device).
     *
     * <p>
     * booking sân dùng tên cột device/equipment trong bảng equipments; rooms dùng devices.
     * </p>
     */
    @Query(value = "SELECT d.id, d.device_name, COUNT(rbd.id) " +
            "FROM room_booking_devices rbd " +
            "INNER JOIN devices d ON d.id = rbd.device_id " +
            "GROUP BY d.id, d.device_name " +
            "ORDER BY COUNT(rbd.id) DESC", nativeQuery = true)
    List<Object[]> aggregateBorrowCountByDevice();

    /**
     * Aggregate: tổng số lượt mượn theo phòng/tài sản (Asset).
     *
     * <p>
     * booking sân dùng bookings/pitches; rooms dùng asset_usages/assets.
     * </p>
     */
    @Query(value = "SELECT a.id, a.asset_name, COUNT(rbd.id) " +
            "FROM room_booking_devices rbd " +
            "INNER JOIN asset_usages au ON au.id = rbd.asset_usage_id " +
            "INNER JOIN assets a ON a.id = au.asset_id " +
            "GROUP BY a.id, a.asset_name " +
            "ORDER BY COUNT(rbd.id) DESC", nativeQuery = true)
    List<Object[]> aggregateBorrowCountByRoom();
}

