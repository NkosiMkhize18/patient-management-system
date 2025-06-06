package com.codewithnkosi.patientservice.dto;

import com.codewithnkosi.patientservice.dto.validators.PatientValidatorGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PatientRequestDTO {

    @NotBlank(message = "Name should not be blank")
    @Size(max = 50, message = "Name cannot exceed 100 chars")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Date of birth required")
    private String dateOfBirth;

    @NotBlank(groups = PatientValidatorGroup.class, message = "Registered Date is required")
    private String registeredDate;

}
