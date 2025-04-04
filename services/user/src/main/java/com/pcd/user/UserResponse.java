package com.pcd.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserResponse
        (String id,

         String firstname,

         String lastname,

         String email,

         Address address) {

}
