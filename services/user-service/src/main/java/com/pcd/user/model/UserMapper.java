package com.pcd.user.model;

import com.pcd.user.enums.Role;
import com.pcd.user.records.AddressDTO;
import com.pcd.user.records.UserRequest;
import com.pcd.user.records.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public User toUser(UserRequest request) {
        Address address = new Address();
        address.setStreet(request.address().street());
        address.setCity(request.address().city());
        address.setZip(request.address().zipCode());

        User user = new User();
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setEmail(request.email());
        user.setAddress(address);
        return user;
    }

    public UserResponse fromUser(User user) {
        AddressDTO addressDTO = null;
        if (user.getAddress() != null) {
            // Using AddressMapper instead of direct method on Address
            addressDTO = AddressMapper.toAddressDTO(user.getAddress());
        }

        return new UserResponse(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                addressDTO,
                user.getRole()  // Make sure this field exists in User class
        );
    }
}