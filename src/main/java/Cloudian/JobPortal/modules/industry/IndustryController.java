package Cloudian.JobPortal.modules.industry;

import Cloudian.JobPortal.models.Industry;
import Cloudian.JobPortal.modules.industry.dto.CreateIndustryDto;
import Cloudian.JobPortal.modules.industry.dto.IndustryResponse;
import Cloudian.JobPortal.modules.industry.dto.UpdateIndustry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

//Note: !! Nho bo sung them role requiredRole("admin") truoc khi ra production
@RestController
@RequestMapping("industry")
public class IndustryController {
    @Autowired
    IndustryService industryService;
    @GetMapping()   //?name=?limit=?offset=
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllIndustry(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    )
    {
        List<IndustryResponse> response = industryService.getAllIndustry(name , offset , limit);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getIndustryById(
            @PathVariable Long id
    )
    {
        IndustryResponse response = industryService.getIndustryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createIndustry(
            @RequestBody()CreateIndustryDto data
            )
    {
        IndustryResponse industry = industryService.createIndustry(data.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(industry);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateIndustry(
            @PathVariable Long id,
            @RequestBody()UpdateIndustry data
            )
    {
        IndustryResponse response = industryService.updateIndustry(id , data);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIndustry(
        @PathVariable Long id
    )
    {
        Boolean result = industryService.deleteIndustry(id);
        HashMap<String , Object> mp = new HashMap<>();
        mp.put("status" , result);
        if (result)
            mp.put("message" , "Delete industry successfully");
        else mp.put("message" , "Delete industry unsuccessfully");
        return ResponseEntity.status(HttpStatus.OK).body(mp);
    }
}
