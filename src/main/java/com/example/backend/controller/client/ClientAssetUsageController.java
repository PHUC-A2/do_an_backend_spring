package com.example.backend.controller.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.request.assetusage.ReqCreateClientAssetUsageDTO;
import com.example.backend.domain.request.assetusage.ReqCreateClientCheckoutDTO;
import com.example.backend.domain.request.assetusage.ReqCreateClientDeviceIssueDTO;
import com.example.backend.domain.request.assetusage.ReqCreateClientReturnDTO;
import com.example.backend.domain.request.assetusage.ReqUpdateClientAssetUsageDTO;
import com.example.backend.domain.response.assetusage.ResAssetUsageDetailDTO;
import com.example.backend.domain.response.assetusage.ResCreateAssetUsageDTO;
import com.example.backend.domain.response.assetusage.ResUpdateAssetUsageDTO;
import com.example.backend.domain.response.checkout.ResCheckoutDetailDTO;
import com.example.backend.domain.response.common.ResultPaginationDTO;
import com.example.backend.domain.response.devicereturn.ResDeviceReturnDetailDTO;
import com.example.backend.domain.response.checkout.ResCreateCheckoutDTO;
import com.example.backend.domain.response.devicereturn.ResCreateDeviceReturnDTO;
import com.example.backend.domain.response.deviceissue.ResCreateDeviceIssueDTO;
import com.example.backend.service.AssetUsageService;
import com.example.backend.service.CheckoutService;
import com.example.backend.service.DeviceIssueService;
import com.example.backend.service.DeviceReturnService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.BadRequestException;
import com.example.backend.util.error.IdInvalidException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/client")
public class ClientAssetUsageController {

    private final AssetUsageService assetUsageService;
    private final CheckoutService checkoutService;
    private final DeviceReturnService deviceReturnService;
    private final DeviceIssueService deviceIssueService;

    public ClientAssetUsageController(
            AssetUsageService assetUsageService,
            CheckoutService checkoutService,
            DeviceReturnService deviceReturnService,
            DeviceIssueService deviceIssueService) {
        this.assetUsageService = assetUsageService;
        this.checkoutService = checkoutService;
        this.deviceReturnService = deviceReturnService;
        this.deviceIssueService = deviceIssueService;
    }

    @PostMapping("/room-bookings")
    @ApiMessage("Đặt phòng tin học")
    public ResponseEntity<ResCreateAssetUsageDTO> createRoomBooking(
            @Valid @RequestBody @NonNull ReqCreateClientAssetUsageDTO req) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        ResCreateAssetUsageDTO res = assetUsageService.createClientAssetUsage(req, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/room-bookings")
    @ApiMessage("Danh sách đặt phòng của chính user")
    public ResponseEntity<ResultPaginationDTO> getMyRoomBookings(@NonNull Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        return ResponseEntity.ok(assetUsageService.getAllAssetUsagesOfCurrentUser(email, pageable));
    }

    @GetMapping("/room-bookings/{id}")
    @ApiMessage("Chi tiết đặt phòng của chính user")
    public ResponseEntity<ResAssetUsageDetailDTO> getMyRoomBookingById(@PathVariable("id") @NonNull Long id)
            throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        return ResponseEntity.ok(assetUsageService.getAssetUsageDetailByIdForCurrentUser(id, email));
    }

    @PutMapping("/room-bookings/{id}")
    @ApiMessage("Cập nhật đặt phòng của chính user")
    public ResponseEntity<ResUpdateAssetUsageDTO> updateMyRoomBooking(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody @NonNull ReqUpdateClientAssetUsageDTO req) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        return ResponseEntity.ok(assetUsageService.updateClientAssetUsage(id, req, email));
    }

    @PatchMapping("/room-bookings/{id}/cancel")
    @ApiMessage("Hủy đặt phòng của chính user")
    public ResponseEntity<Void> cancelMyRoomBooking(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        assetUsageService.cancelClientAssetUsage(id, email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/room-bookings/{id}")
    @ApiMessage("Xóa booking phòng của chính user (soft delete)")
    public ResponseEntity<Void> deleteMyRoomBooking(@PathVariable("id") @NonNull Long id) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        assetUsageService.deleteAssetUsageForCurrentUser(id, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/room-bookings/{id}/checkout")
    @ApiMessage("Biên bản nhận tài sản theo đặt phòng")
    public ResponseEntity<ResCheckoutDetailDTO> getCheckoutByRoomBooking(@PathVariable("id") @NonNull Long id) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        try {
            // Nếu chưa có biên bản nhận, trả 200 + data=null để client (polling) không bị 400.
            return ResponseEntity.ok(checkoutService.getCheckoutDetailByAssetUsageIdForCurrentUser(id, email));
        } catch (IdInvalidException ex) {
            return ResponseEntity.ok(null);
        }
    }

    @GetMapping("/room-bookings/{id}/return")
    @ApiMessage("Biên bản trả tài sản theo đặt phòng")
    public ResponseEntity<ResDeviceReturnDetailDTO> getReturnByRoomBooking(@PathVariable("id") @NonNull Long id) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        try {
            // Nếu chưa có biên bản trả, trả 200 + data=null để client (polling) không bị 400.
            return ResponseEntity.ok(deviceReturnService.getDeviceReturnDetailByAssetUsageIdForCurrentUser(id, email));
        } catch (IdInvalidException ex) {
            return ResponseEntity.ok(null);
        }
    }

    @PostMapping("/room-bookings/{id}/checkout")
    @ApiMessage("Client tạo biên bản nhận tài sản theo đặt phòng")
    public ResponseEntity<ResCreateCheckoutDTO> createCheckoutForRoomBooking(
            @PathVariable("id") @NonNull Long id,
            @RequestBody(required = false) ReqCreateClientCheckoutDTO req) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        var body = req != null ? req : new ReqCreateClientCheckoutDTO();
        ResCreateCheckoutDTO res = checkoutService.createCheckoutForCurrentUser(
                id,
                body.getReceiveTime(),
                body.getConditionNote(),
                email);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/room-bookings/{id}/return")
    @ApiMessage("Client tạo biên bản trả tài sản theo đặt phòng")
    public ResponseEntity<ResCreateDeviceReturnDTO> createReturnForRoomBooking(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqCreateClientReturnDTO req) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        ResCreateDeviceReturnDTO res = deviceReturnService.createDeviceReturnForCurrentUser(id, req, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/room-bookings/{id}/issues")
    @ApiMessage("Client báo sự cố thiết bị theo đặt phòng")
    public ResponseEntity<ResCreateDeviceIssueDTO> createIssueForRoomBooking(
            @PathVariable("id") @NonNull Long id,
            @Valid @RequestBody ReqCreateClientDeviceIssueDTO req) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Không tìm thấy user login"));
        ResCreateDeviceIssueDTO res = deviceIssueService.createDeviceIssueForCurrentUser(
                id,
                req.getDeviceId(),
                req.getDescription(),
                email);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
