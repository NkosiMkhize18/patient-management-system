package com.codewithnkosi.patientservice;

import com.codewithnkosi.patientservice.model.Patient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class PatientServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PatientServiceApplication.class, args);
	}

}
