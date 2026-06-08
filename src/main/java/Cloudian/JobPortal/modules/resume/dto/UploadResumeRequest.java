package Cloudian.JobPortal.modules.resume.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class UploadResumeRequest {
    private String fileName; 
    MultipartFile file; 
}
