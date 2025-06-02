package com.codewithnkosi.patientservice.exception;

public class PatientNotFoundException extends Throwable {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
