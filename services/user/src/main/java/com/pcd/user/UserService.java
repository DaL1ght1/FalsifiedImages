package com.pcd.user;

import com.pcd.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class UserService {

    private final  UserRepository repository;
    private final  UserMapper mapper;


    public User createUser(UserRequest request) {
        return  repository.save(mapper.toUser(request));

    }

    public void updateUser(UserRequest request) {
        var user = repository.findById(request.id())
                .orElseThrow(()->new UserNotFoundException(
                        format("Cannot Update user: User with id %s not found", request.id())
        ));
        mergeUser(user,request);
        repository.save(user);
    }

    public void mergeUser(User user, UserRequest request) {
        if (StringUtils.isNotBlank(request.firstname())){
            user.setFirstname(request.firstname());
        }
        if (StringUtils.isNotBlank(request.lastname())){
            user.setLastname(request.lastname());
        }
        if (StringUtils.isNotBlank(request.email())){
            user.setEmail(request.email());
        }
        if (request.address() != null ){
            user.setAddress(request.address());
        }
    }

    public List<UserResponse> getAllUsers(){
        return repository.findAll()
                .stream().map(mapper::fromUser)
                .collect(Collectors.toList());
    }

    public boolean existsById(String id) {
        return repository.findById(id).isPresent();}

    public UserResponse getUserById(String id) {
        return repository.findById(id)
                .map(mapper::fromUser)
                .orElseThrow(()->new UserNotFoundException(
                        format("Cannot Find user: User with id %s not found", id)
                ));
    }

    public void deleteUserById(String id) {
        if (existsById(id)) {
            repository.deleteById(id);
        }
        else {
            throw new UserNotFoundException(
                    format("Cannot Delete user: User with id %s not found", id)
            );
        }
    }
}
