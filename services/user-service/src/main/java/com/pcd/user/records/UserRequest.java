package com.pcd.user.records;

import com.pcd.user.enums.Role;
import com.pcd.user.model.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;


public record UserRequest
        (String id,
         @NotNull(message = "Customer firstname is required")
         String firstname,

         @NotNull(message = "Customer lastname is required")
         String lastname,

         @Email(message = "Customer email is not a valid email address")
         String email,
         @NotNull
         String password,
         @NotNull
         Role role,

         AddressDTO address,
         Boolean active) {


}



