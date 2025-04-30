package com.codewithnkosi.patientservice.service;

import com.codewithnkosi.patientservice.dto.PatientRequestDTO;
import com.codewithnkosi.patientservice.dto.PatientResponseDTO;
import com.codewithnkosi.patientservice.mapper.PatientMapper;
import com.codewithnkosi.patientservice.model.Patient;
import com.codewithnkosi.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO> getAllPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toPatientResponseDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        Patient patient = patientRepository.save(PatientMapper.toPatient(patientRequestDTO));
        return PatientMapper.toPatientResponseDTO(patient);
    }
}
