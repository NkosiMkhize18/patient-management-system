package com.codewithnkosi.patientservice.service;

import com.codewithnkosi.patientservice.dto.PatientRequestDTO;
import com.codewithnkosi.patientservice.dto.PatientResponseDTO;
import com.codewithnkosi.patientservice.exception.EmailAlreadyExistsException;
import com.codewithnkosi.patientservice.exception.PatientNotFoundException;
import com.codewithnkosi.patientservice.mapper.PatientMapper;
import com.codewithnkosi.patientservice.model.Patient;
import com.codewithnkosi.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

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

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) throws EmailAlreadyExistsException {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException(patientRequestDTO.getEmail());
        }
        Patient patient = patientRepository.save(PatientMapper.toPatient(patientRequestDTO));
        return PatientMapper.toPatientResponseDTO(patient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) throws PatientNotFoundException, EmailAlreadyExistsException {

        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException(id.toString()));

        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists ");
        }

        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());

        return PatientMapper.toPatientResponseDTO(patientRepository.save(patient));
    }
}
