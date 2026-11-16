package com.srijon.orderflow.service;

import com.srijon.orderflow.dto.auth.LoginRequestDto;
import com.srijon.orderflow.dto.auth.LoginResponseDto;
import com.srijon.orderflow.dto.auth.RegisterRequestDto;
import com.srijon.orderflow.dto.auth.RegisterResponseDto;
import com.srijon.orderflow.entity.User;
import com.srijon.orderflow.enums.Role;
import com.srijon.orderflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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

    public LoginResponseDto login(LoginRequestDto loginRequestDto){

        //Authenticating user credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        //Fetch The user
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

        //Generating token
        String token = jwtService.generateToken(loginRequestDto.getEmail());

        //Return login info
        return new LoginResponseDto(
                user.getEmail(),
                user.getRole(),
                token
        );
    }
}
