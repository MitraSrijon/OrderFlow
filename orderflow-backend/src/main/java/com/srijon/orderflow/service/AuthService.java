package com.srijon.orderflow.service;

import com.srijon.orderflow.dto.auth.RegisterRequestDto;
import com.srijon.orderflow.dto.auth.RegisterResponseDto;
import com.srijon.orderflow.entity.User;
import com.srijon.orderflow.enums.Role;
import com.srijon.orderflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponseDto  register(RegisterRequestDto requestDto){

        if(userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("This email is already registered");
        }

        User user = new User();
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);

        return new RegisterResponseDto(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }
}
