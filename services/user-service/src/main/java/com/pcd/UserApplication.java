package com.pcd;

import com.pcd.authentication.AuthenticationService;
import com.pcd.authentication.RegisterRequest;
import com.pcd.user.model.Address;
import com.pcd.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import static com.pcd.user.enums.Role.ADMIN;

@SpringBootApplication
@RequiredArgsConstructor
@EnableDiscoveryClient
public class UserApplication {

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
						.password("admin123")
						.role(ADMIN)
						.address(new Address("Compus", "Technopol", "Mannouba", "2010", "Tunisia"))
						.build();
				System.out.println("Admin token: " + service.register(admin).getAccessToken());
			}
		};
	}

}
