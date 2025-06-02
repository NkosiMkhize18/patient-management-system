package com.codewithnkosi.patientservice.controller;

import com.codewithnkosi.patientservice.dto.PatientRequestDTO;
import com.codewithnkosi.patientservice.dto.PatientResponseDTO;
import com.codewithnkosi.patientservice.exception.EmailAlreadyExistsException;
import com.codewithnkosi.patientservice.exception.PatientNotFoundException;
import com.codewithnkosi.patientservice.service.PatientService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getPatients() {
        List<PatientResponseDTO> patientResponseDTOS = patientService.getAllPatients();
        return ResponseEntity.ok().body(patientResponseDTOS);
    }

    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO patientRequestDTO) throws EmailAlreadyExistsException {
        PatientResponseDTO patientResponseDTO = patientService.createPatient(patientRequestDTO);
        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id, @Validated(Default.class) @RequestBody PatientRequestDTO patientRequestDTO) throws EmailAlreadyExistsException, PatientNotFoundException {
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);
        return ResponseEntity.ok().body(patientResponseDTO);
    }

}
