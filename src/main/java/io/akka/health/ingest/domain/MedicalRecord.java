package io.akka.health.ingest.domain;

/**
    * Represents a medical record for a patient.
    *
    * @param patientId The unique identifier for the patient.
    * @param reasonForVisit The reason for the patient's visit.
    * @param diagnosis The diagnosis made by the healthcare provider.
    * @param prescribedMedication The medication prescribed to the patient.
    * @param notes Additional notes or comments from the healthcare provider.
*/
public record MedicalRecord(
        String patientId,
        String reasonForVisit,
        String diagnosis,
        String prescribedMedication,
        String notes) {
}
