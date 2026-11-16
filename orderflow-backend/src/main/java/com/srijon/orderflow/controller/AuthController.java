package com.srijon.orderflow.controller;

import com.srijon.orderflow.dto.auth.LoginRequestDto;
import com.srijon.orderflow.dto.auth.LoginResponseDto;
import com.srijon.orderflow.dto.auth.RegisterRequestDto;
import com.srijon.orderflow.dto.auth.RegisterResponseDto;
import com.srijon.orderflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponseDto register(
            @Valid @RequestBody RegisterRequestDto requestDto){

        return authService.register(requestDto);
    }

    @PostMapping("/login")
    public LoginResponseDto login(
            @RequestBody LoginRequestDto loginRequestDto
            ){

        return authService.login(loginRequestDto);
    }
}
