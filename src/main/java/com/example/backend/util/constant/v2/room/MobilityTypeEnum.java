package com.example.backend.util.constant.v2.room;

/**
 * Thiết bị gắn cố định trong phòng hay có thể di chuyển mượn (bảng {@code room_device_catalog_v2}).
 */
public enum MobilityTypeEnum {

    /** Gắn cố định tại phòng, không mang ra ngoài. */
    FIXED,

    /** Có thể mượn/trả, di chuyển giữa các phòng. */
    MOVABLE
}
