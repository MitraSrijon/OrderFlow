package com.srijon.orderflow.dto.auth;

import com.srijon.orderflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponseDto {

    private long id;
    private String name;
    private String email;
    private Role role;
}
