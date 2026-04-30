package Cloudian.JobPortal.modules.industry;


import Cloudian.JobPortal.models.Industry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndustryRepository extends JpaRepository<Industry , Long> {
    public Page<Industry> findByNameContainingIgnoreCaseAndDeleteAtIsNull(String name , Pageable page);
    public Page<Industry> findByDeleteAtIsNull(Pageable pageable);
    public Industry findByNameContainingIgnoreCaseAndDeleteAtIsNull(String name);  //trung ten ham nhung khac danh sach tham so wtf
}
