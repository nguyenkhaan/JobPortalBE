package Cloudian.JobPortal.modules.user;


import Cloudian.JobPortal.models.ActionType;
import Cloudian.JobPortal.models.EntityName;
import Cloudian.JobPortal.models.Role;
import Cloudian.JobPortal.models.User;
import Cloudian.JobPortal.models.UserRole;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;
    @Autowired
    private EmployerRepository employerRepository;
    @Autowired
    private JobSeekerRepository jobSeekerRepository;

    private UserResponse toResponse(User user)
    {
        List<Role> roles = user.getUserRoleList().stream()
                .map(UserRole::getRole)
                .toList();

        String displayName = user.getEmail();
        if (roles.contains(Role.EMPLOYER)) {
            displayName = employerRepository.findByOwnerId(user.getId())
                    .map(it -> it.getCompanyName())
                    .orElse(user.getEmail());
        } else if (roles.contains(Role.SEEKER)) {
            displayName = jobSeekerRepository.findByUserId(user.getId())
                    .map(it -> it.getFullName())
                    .orElse(user.getEmail());
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(displayName)
                .createdAt(user.getCreatedAt())
                .active(user.getActive())
                .banned(user.getBanned())
                .roles(roles)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int limit, int offset, String search, Role role, Boolean active)
    {
        if (limit <= 0 || limit > 100) {
            throw new BadRequestException("Limit must be between 1 and 100");
        }
        if (offset < 0) {
            throw new BadRequestException("Offset cannot be less than 0");
        }

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);

        Specification<User> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String value = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("email").as(String.class)), value));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (role != null) {
                var roleJoin = root.join("userRoleList");
                predicates.add(cb.equal(roleJoin.get("role"), role));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return userRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public User findUserByEmail(String email)
    {
        //Make something to this
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        /*
        Install this to use UsernameNotFoundException if you haven't yet
        	implementation 'org.springframework.boot:spring-boot-starter-security'
	        implementation 'org.springframework.security:spring-security-core:6.2.8'
         */

    }
    // admin ban user
    @Transactional
    public UserResponse toggleUserActive(Long adminId, Long targetUserId){
        if(adminId.equals(targetUserId)) {
            throw new BadRequestException("Admin cannot self-lock account");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean newBannedStatus = !targetUser.getBanned();
        targetUser.setBanned(newBannedStatus);
        userRepository.save(targetUser);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("targetEmail", targetUser.getEmail());
        auditData.put("action", newBannedStatus ? "LOCKED" : "UNLOCKED");

        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(adminId)
                .recordId(targetUser.getId())
                .entityName(EntityName.User)
                .data(auditData)
                .build());

        return toResponse(targetUser);
    }

    @Transactional
    public UserResponse deactivateUser(Long adminId, Long targetUserId) {
        if(adminId.equals(targetUserId)) {
            throw new BadRequestException("Admin cannot deactivate their own account");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        targetUser.setActive(false);
        targetUser.setBanned(true);
        userRepository.save(targetUser);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("targetEmail", targetUser.getEmail());
        auditData.put("action", "DEACTIVATED");
        auditData.put("deactivatedAt", LocalDateTime.now().toString());

        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.DELETE)
                .userId(adminId)
                .recordId(targetUser.getId())
                .entityName(EntityName.User)
                .data(auditData)
                .build());

        return toResponse(targetUser);
    }
}
