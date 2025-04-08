package com.pcd.imagemanagment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ImageManagmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageManagmentApplication.class, args);
	}

}
