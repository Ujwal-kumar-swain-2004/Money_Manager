package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.AuthDTO;
import in.bushansirgur.moneymanager.dto.ProfileDTO;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.ProfileRepository;
import in.bushansirgur.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    @Autowired
    private  ProfileRepository profileRepository;
    @Autowired
    private  EmailService emailService;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  AuthenticationManager authenticationManager;
    @Autowired
    private  JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationURL;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        if (profileRepository.findByEmail(profileDTO.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile.setActive(true); // Automatically activate the profile
        newProfile = profileRepository.save(newProfile);
        //send activation email (disabled to allow immediate registration without email setup)
        // String activationLink = activationURL+"/api/v1.0/activate?token=" + newProfile.getActivationToken();
        // String subject = "Activate your Money Manager account";
        // String body = "Click on the following link to activate your account: " + activationLink;
        // emailService.sendEmail(newProfile.getEmail(), subject, body);
        return toDTO(newProfile);
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        ProfileEntity entity = new ProfileEntity();
        entity.setId(profileDTO.getId());
        entity.setFullName(profileDTO.getFullName());
        entity.setEmail(profileDTO.getEmail());
        entity.setPassword(passwordEncoder.encode(profileDTO.getPassword()));
        entity.setProfileImageUrl(profileDTO.getProfileImageUrl());
        entity.setCreatedAt(profileDTO.getCreatedAt());
        entity.setUpdatedAt(profileDTO.getUpdatedAt());
        return entity;
    }


    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(profileEntity.getId());
        dto.setFullName(profileEntity.getFullName());
        dto.setEmail(profileEntity.getEmail());
        dto.setProfileImageUrl(profileEntity.getProfileImageUrl());
        dto.setCreatedAt(profileEntity.getCreatedAt());
        dto.setUpdatedAt(profileEntity.getUpdatedAt());
        return dto;
    }

    public boolean activateProfile(String activationToken) {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email) {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser;

        if (email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        ProfileDTO dto = new ProfileDTO();
        dto.setId(currentUser.getId());
        dto.setFullName(currentUser.getFullName());
        dto.setEmail(currentUser.getEmail());
        dto.setProfileImageUrl(currentUser.getProfileImageUrl());
        dto.setCreatedAt(currentUser.getCreatedAt());
        dto.setUpdatedAt(currentUser.getUpdatedAt());

        return dto;
    }


    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));

            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(authDTO.getEmail())
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}
