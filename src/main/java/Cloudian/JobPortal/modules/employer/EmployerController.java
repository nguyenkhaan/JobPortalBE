package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.employer.dto.CreateEmployerProfileRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileUpdateRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerStatisticResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerSubscriptionResponse;
import Cloudian.JobPortal.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("employer")
@Tag(name = "Nhà tuyển dụng", description = "APIs quản lý hồ sơ nhà tuyển dụng và gói dịch vụ")
public class EmployerController {
    @Autowired
    EmployerService employerService;

    @Operation(summary = "Tạo hồ sơ nhà tuyển dụng", description = """
        Tạo hồ sơ nhà tuyển dụng mới cho người dùng đã xác thực.
        
        ## Quy tắc kinh doanh
        
        - Người dùng không được đã có hồ sơ nhà tuyển dụng
        - Người dùng không được là người tìm việc
        - Hồ sơ mới có trạng thái chờ phê duyệt (PENDING)
        - Gói Free được tự động gán
        
        ## Yêu cầu sử dụng multipart/form-data để hỗ trợ tải lên logo, banner và giấy phép kinh doanh.
        """)
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo hồ sơ thành công", content = @Content(schema = @Schema(implementation = EmployerProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ (đã có hồ sơ hoặc là người tìm việc)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployerProfileResponse> createEmployerProfile(
            @ModelAttribute @Valid CreateEmployerProfileRequest data,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("User not found");
        EmployerProfileResponse emp = employerService.createEmployer(data, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(emp);
    }

    @Operation(summary = "Lấy hồ sơ nhà tuyển dụng", description = "Lấy hồ sơ nhà tuyển dụng của người dùng đã xác thực.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy hồ sơ thành công", content = @Content(schema = @Schema(implementation = EmployerProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Chưa tạo hồ sơ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @GetMapping
    public ResponseEntity<EmployerProfileResponse> getEmployerProfile(
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerProfileResponse response = employerService.getEmployerProfile(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Lấy gói dịch vụ nhà tuyển dụng", description = "Lấy thông tin chi tiết gói dịch vụ của người dùng đã xác thực.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy gói dịch vụ thành công", content = @Content(schema = @Schema(implementation = EmployerSubscriptionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Chưa tạo hồ sơ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @GetMapping("/subscription")
    public ResponseEntity<EmployerSubscriptionResponse> getEmployerSubscription(
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerSubscriptionResponse response = employerService.getEmployerSubscription(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Cập nhật hồ sơ nhà tuyển dụng", description = """
        Cập nhật hồ sơ nhà tuyển dụng của người dùng đã xác thực.
        
        Hỗ trợ cập nhật thông tin hồ sơ và/hoặc tải lên logo, banner hoặc giấy phép kinh doanh mới.
        """)
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật hồ sơ thành công", content = @Content(schema = @Schema(implementation = EmployerProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không tìm thấy hồ sơ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    @PatchMapping
    public ResponseEntity<EmployerProfileResponse> updateEmployerProfile(
            @Valid @ModelAttribute EmployerProfileUpdateRequest data,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerProfileResponse response = employerService.updateEmployerProfile(user.getId(), data);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Lấy thống kê bài đăng", description = "Trả về tổng số bài đăng và tổng số ứng viên của nhà tuyển dụng.")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<EmployerStatisticResponse>> getEmployerStatistics(
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("User not found");
        EmployerStatisticResponse data = employerService.getEmployerStatistics(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}