package com.example.backend.controller.client;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.request.booking.ReqCreateBookingDTO;
import com.example.backend.domain.request.booking.ReqUpdateBookingDTO;
import com.example.backend.domain.response.booking.ResBookingDTO;
import com.example.backend.domain.response.booking.ResCreateBookingDTO;
import com.example.backend.domain.response.booking.ResUpdateBookingDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.service.BookingService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/client")
public class ClientBookingController {

    private final BookingService bookingService;

    public ClientBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // CREATE booking
    @PostMapping("/bookings")
    @ApiMessage("Đặt lịch sân bóng")
    public ResponseEntity<ResCreateBookingDTO> createBooking(
            @Valid @RequestBody ReqCreateBookingDTO req) throws BadRequestException, IdInvalidException {

        ResCreateBookingDTO res = bookingService.createBooking(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // GET all bookings của chính user
    @GetMapping("/bookings")
    @ApiMessage("Lấy danh sách tất cả booking của chính user (có phân trang)")
    public ResponseEntity<ResultPaginationDTO> getAllBookings(Pageable pageable) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        ResultPaginationDTO result = bookingService.getAllBookingsOfUser(email, pageable);
        return ResponseEntity.ok(result);
    }

    // GET booking theo ID (chỉ của chính user)
    @GetMapping("/bookings/{id}")
    @ApiMessage("Lấy thông tin booking theo ID của chính user")
    public ResponseEntity<ResBookingDTO> getBookingById(@PathVariable Long id) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        Booking booking = bookingService.getBookingByIdForUser(id, email);
        ResBookingDTO res = bookingService.convertToResBookingDTO(booking);
        return ResponseEntity.ok(res);
    }

    // UPDATE booking (chỉ của chính user)
    @PutMapping("/bookings/{id}")
    @ApiMessage("Cập nhật thông tin booking của chính user")
    public ResponseEntity<ResUpdateBookingDTO> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdateBookingDTO req) throws IdInvalidException {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        ResUpdateBookingDTO res = bookingService.updateBookingForUser(id, req, email);
        return ResponseEntity.ok(res);
    }

    // DELETE booking (chỉ của chính user)
    @DeleteMapping("/bookings/{id}")
    @ApiMessage("Xóa booking của chính user")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));

        bookingService.deleteBookingForUser(id, email);
        return ResponseEntity.ok().build();
    }
}
