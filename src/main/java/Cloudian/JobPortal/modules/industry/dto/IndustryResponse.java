package Cloudian.JobPortal.modules.industry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryResponse {
    Long id;
    String name;
    Integer jobCount;
    LocalDateTime createdAt;
}
