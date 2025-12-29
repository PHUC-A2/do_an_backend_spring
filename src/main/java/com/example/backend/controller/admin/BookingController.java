package com.example.backend.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.request.booking.ReqCreateBookingDTO;
import com.example.backend.domain.request.booking.ReqUpdateBookingDTO;
import com.example.backend.domain.response.booking.ResBookingDTO;
import com.example.backend.domain.response.booking.ResCreateBookingDTO;
import com.example.backend.domain.response.booking.ResUpdateBookingDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.service.BookingService;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Create booking
    @PostMapping("/bookings")
    @ApiMessage("Đặt lịch")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_CREATE')")
    public ResponseEntity<ResCreateBookingDTO> createBooking(
            @Valid @RequestBody ReqCreateBookingDTO req) throws BadRequestException, IdInvalidException {

        // Nếu userId != null => kiểm tra quyền admin
        // if (req.getUserId() != null && !SecurityUtil.isCurrentUserAdmin()) {
        // throw new BadRequestException("Không có quyền đặt cho user khác");
        // }

        ResCreateBookingDTO res = this.bookingService.createBooking(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Get all bookings (pagination + filter)
    @GetMapping("/bookings")
    @ApiMessage("Lấy danh sách đặt lịch sân")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_VIEW_LIST')")
    public ResponseEntity<ResultPaginationDTO> getAllBookings(
            @Filter Specification<Booking> spec,
            Pageable pageable) {

        return ResponseEntity.ok(this.bookingService.getAllBookings(spec, pageable));
    }

    // Get booking by ID
    @GetMapping("/bookings/{id}")
    @ApiMessage("Lấy thông tin đặt lịch theo ID")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_VIEW_DETAIL')")
    public ResponseEntity<ResBookingDTO> getBookingById(@PathVariable Long id) throws IdInvalidException {
        Booking booking = this.bookingService.getBookingById(id);
        ResBookingDTO res = this.bookingService.convertToResBookingDTO(booking);
        return ResponseEntity.ok(res);
    }

    // Update booking
    @PutMapping("/bookings/{id}")
    @ApiMessage("Cập nhật thông tin đặt lịch")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_UPDATE')")
    public ResponseEntity<ResUpdateBookingDTO> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdateBookingDTO req) throws IdInvalidException {

        // Kiểm tra admin
        // if (req.getUserId() != null && !SecurityUtil.isCurrentUserAdmin()) {
        //     throw new BadRequestException("Không có quyền đặt cho user khác");
        // }

        ResUpdateBookingDTO res = this.bookingService.updateBooking(id, req);
        return ResponseEntity.ok(res);
    }

    // Delete booking
    @DeleteMapping("/bookings/{id}")
    @ApiMessage("Xóa đặt lịch")
    @PreAuthorize("hasAuthority('ALL') or hasAuthority('BOOKING_DELETE')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) throws IdInvalidException {
        this.bookingService.deleteBooking(id);
        return ResponseEntity.ok().build();
    }
}
