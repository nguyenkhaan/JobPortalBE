package Cloudian.JobPortal.modules.user;


import Cloudian.JobPortal.models.ActionType;
import Cloudian.JobPortal.models.EntityName;
import Cloudian.JobPortal.models.User;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<UserResponse> getAllUsers(int limit, int offset)
    {
        if (limit <= 0 || limit > 100) {
            throw new BadRequestException("Limit must be between 1 and 100");
        }
        if (offset < 0) {
            throw new BadRequestException("Offset cannot be less than 0");
        }
        
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        
        return userRepository.findAll(pageable).getContent().stream().map(
                it -> new UserResponse(null , it.getEmail() , it.getCreatedAt(), it.getActive())
        ).toList();
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

        return new UserResponse(targetUser.getId(), targetUser.getEmail(), targetUser.getCreatedAt(), targetUser.getActive());
    }
}
