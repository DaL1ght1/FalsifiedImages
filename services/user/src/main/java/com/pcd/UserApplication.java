package com.pcd;

import com.pcd.authentication.AuthenticationService;
import com.pcd.authentication.RegisterRequest;
import com.pcd.user.Address;
import com.pcd.user.Roles;
import com.pcd.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static com.pcd.user.Roles.Admin;

@SpringBootApplication
@RequiredArgsConstructor
public class UserApplication {

	private final UserRepository userRepository;
	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(AuthenticationService service, UserRepository userRepository) {
		return args -> {
			var exists = userRepository.findByEmail("admin@admin.com");
			if (exists.isEmpty()) { // Check if the admin user does not exist
				var admin = RegisterRequest.builder()
						.firstname("Admin")
						.lastname("Admin")
						.email("admin@admin.com")
						.password("admin")
						.role(Admin)
						.address(new Address("Compus", "Technopol", "Mannouba", "2010", "Tunisia"))
						.build();
				System.out.println("Admin token: " + service.register(admin).getAccessToken());
			}
		};
	}

}
