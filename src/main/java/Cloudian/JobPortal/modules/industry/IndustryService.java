package Cloudian.JobPortal.modules.industry;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.models.JobIndustry;
import Cloudian.JobPortal.models.JobPost;
import Cloudian.JobPortal.modules.industry.dto.IndustryResponse;
import Cloudian.JobPortal.modules.industry.dto.UpdateIndustry;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.jobindustry.JobIndustryRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class IndustryService {
    @Autowired
    IndustryRepository industryRepository;
    @Autowired
    JobPostRepository jobPostRepository;
    @Autowired
    JobIndustryRepository jobIndustryRepository;
    @Autowired
    AuditService auditService;
    private IndustryResponse toResponse(Industry industry) {
        return IndustryResponse.builder()
                .id(industry.getId())
                .name(industry.getName())
                .jobCount(jobIndustryRepository.findByIndustryId(industry.getId()).size())
                .createdAt(industry.getCreatedAt())
                .build();
    }

    public Page<IndustryResponse> getAllIndustry(String name , int offset , int limit)
    {
        if (limit < 1 || limit > 100) throw new BadRequestException("Invalid limit"); 
        if (offset < 0) throw new BadRequestException("Invalid offset"); 
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page , limit);
        Page<Industry> result;   //Mot collections kieu page de lay ra cac page trong database
        if (name != null && !name.isBlank())
            result = industryRepository.findByNameContainingIgnoreCaseAndDeleteAtIsNull(name , pageable);
        else
            result = industryRepository.findByDeleteAtIsNull(pageable);

        return result.map(this::toResponse);
    }
    public IndustryResponse getIndustryById(Long id)
    {
        Industry industry = industryRepository.findById(id).orElseThrow(() -> new BadRequestException("industry not found"));
        return toResponse(industry);
    }
    @Transactional
    public IndustryResponse createIndustry(String name, Long userId)
    {
        Industry industry = Industry.builder().name(name).build();
        industryRepository.save(industry);
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("name", industry.getName());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.CREATE)
                .userId(userId)
                .recordId(industry.getId())
                .entityName(EntityName.Industry)
                .data(auditData)
                .build());
        return toResponse(industry);
    }
    @Transactional
    public IndustryResponse updateIndustry(Long id , UpdateIndustry data, Long userId)
    {
        Industry industry = industryRepository.findById(id).orElseThrow(() -> new BadRequestException("industry not found"));
        if (data.getName() != null && !data.getName().isBlank())
        {
            industry.setName(data.getName());
        }
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("name", industry.getName());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(userId)
                .recordId(industry.getId())
                .entityName(EntityName.Industry)
                .data(auditData)
                .build());
        return toResponse(industry);
    }
    @Transactional
    public Boolean deleteIndustry(Long id, Long userId)
    {
        Industry industry = industryRepository.findById(id).orElseThrow(() -> new BadRequestException("industry not found"));
        if (industry.getDeleteAt() != null)
            throw new BadRequestException("Industry not found");
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("name", industry.getName());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.DELETE)
                .userId(userId)
                .recordId(industry.getId())
                .entityName(EntityName.Industry)
                .data(auditData)
                .build());
        industry.setDeleteAt(LocalDateTime.now());
        List<JobIndustry> jobIndustryList = jobIndustryRepository.findByIndustryId(industry.getId());
        for (int i = 0; i < jobIndustryList.size(); ++i)
            jobIndustryList.get(i).setDeleteAt(LocalDateTime.now());
        return true;
    }
}
