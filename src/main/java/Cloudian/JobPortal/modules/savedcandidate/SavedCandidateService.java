package Cloudian.JobPortal.modules.savedcandidate;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.JobSeekerProfile;
import Cloudian.JobPortal.models.SavedCandidate;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository;
import Cloudian.JobPortal.modules.savedcandidate.dto.CreateSavedCandidateDto;
import Cloudian.JobPortal.modules.savedcandidate.dto.SavedCandidateResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedCandidateService {
    private final SavedCandidateRepository savedCandidateRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerRepository jobSeekerRepository;

    @Transactional
    public SavedCandidateResponse saveCandidate(Long userId, CreateSavedCandidateDto dto) {
        EmployerProfile employer = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new NotFoundException("Employer profile not found"));

        JobSeekerProfile jobSeeker = jobSeekerRepository.findById(dto.getJobSeekerId())
                .orElseThrow(() -> new NotFoundException("Job seeker not found"));

        if (savedCandidateRepository.existsByEmployerIdAndJobSeekerId(employer.getId(), dto.getJobSeekerId())) {
            throw new BadRequestException("Candidate is already saved");
        }

        SavedCandidate savedCandidate = SavedCandidate.builder()
                .employer(employer)
                .jobSeeker(jobSeeker)
                .build();

        savedCandidateRepository.save(savedCandidate);
        return SavedCandidateResponse.from(savedCandidate);
    }

    @Transactional
    public List<SavedCandidateResponse> getSavedCandidates(Long userId) {
        EmployerProfile employer = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new NotFoundException("Employer profile not found"));
        
        return savedCandidateRepository.findByEmployerId(employer.getId()).stream()
                .map(SavedCandidateResponse::from)
                .toList();
    }

    @Transactional
    public void removeSavedCandidate(Long userId, Long jobSeekerId) {
        EmployerProfile employer = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new NotFoundException("Employer profile not found"));
        
        SavedCandidate savedCandidate = savedCandidateRepository.findByEmployerIdAndJobSeekerId(employer.getId(), jobSeekerId)
                .orElseThrow(() -> new NotFoundException("Saved candidate not found"));

        savedCandidate.setDeleteAt(LocalDateTime.now());
        savedCandidateRepository.save(savedCandidate);
    }
}
