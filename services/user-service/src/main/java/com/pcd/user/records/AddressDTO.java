package com.pcd.user.records;

public record AddressDTO(
        String street,
        String city,
        String zipCode
) {}