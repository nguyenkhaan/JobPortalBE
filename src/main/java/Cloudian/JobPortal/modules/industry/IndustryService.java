package Cloudian.JobPortal.modules.industry;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.models.Industry;
import Cloudian.JobPortal.modules.industry.dto.IndustryResponse;
import Cloudian.JobPortal.modules.industry.dto.UpdateIndustry;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class IndustryService {
    @Autowired
    IndustryRepository industryRepository;
    public List<IndustryResponse> getAllIndustry(String name , int offset , int limit)
    {
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page , limit);
        Page<Industry> result;   //Mot collections kieu page de lay ra cac page trong database
        if (name != null && !name.isBlank())
            result = industryRepository.findByNameContainingIgnoreCaseAndDeleteAtIsNull(name , pageable);
        else
            result = industryRepository.findByDeleteAtIsNull(pageable);

        return result.getContent().stream().map(it -> IndustryResponse.builder().id(it.getId()).name(it.getName()).build()).toList();
    }
    public IndustryResponse getIndustryById(Long id)
    {
        Industry industry = industryRepository.findById(id).orElseThrow(() -> new BadRequestException("industry not found"));
        return IndustryResponse.builder().name(industry.getName()).id(industry.getId()).build();
    }
    @Transactional
    public IndustryResponse createIndustry(String name)
    {
        Industry industry = Industry.builder().name(name).build();
        industryRepository.save(industry);
        return IndustryResponse.builder().id(industry.getId()).name(industry.getName()).build();
    }
    @Transactional
    public IndustryResponse updateIndustry(Long id , UpdateIndustry data)
    {
        Industry industry = industryRepository.findById(id).orElseThrow(() -> new BadRequestException("industry not found"));
        if (data.getName() != null && !data.getName().isBlank())
        {
            industry.setName(data.getName());
        }
        return IndustryResponse.builder().name(industry.getName()).id(industry.getId()).build();
    }
    @Transactional
    public Boolean deleteIndustry(Long id)
    {
        Industry industry = industryRepository.findById(id).orElseThrow(() -> new BadRequestException("industry not found"));
        industry.setDeleteAt(LocalDateTime.now());

    }
}
