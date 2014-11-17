package org.openmrs.module.kenyaemr.fragment.controller.summaries;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.Arrays;
import java.util.List;

/**
 * patient profile fragment.
 */
public class PatientProfileFragmentController {

	public void controller(@FragmentParam("patient")
						   Patient patient,
						   FragmentModel model,
						   UiUtils ui) {
		PatientIdentifierType pcn = MetadataUtils.existing(PatientIdentifierType.class, CommonMetadata._PatientIdentifierType.PATIENT_CLINIC_NUMBER);
		PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class, HivMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
		List<PatientIdentifier> pcnIdentifiers = Context.getPatientService().getPatientIdentifiers(null, Arrays.asList(pcn), null, Arrays.asList(patient), null);
		List<PatientIdentifier> upnIdentifiers = Context.getPatientService().getPatientIdentifiers(null, Arrays.asList(upn), null, Arrays.asList(patient), null);

		String facility = Context.getService(KenyaEmrService.class).getDefaultLocation().getName();
		String patient_clinic_number = "Not available";
		String unique_patient_number = "Not available";

		if(pcnIdentifiers.size() > 0) {
			patient_clinic_number = pcnIdentifiers.get(0).getIdentifier();
		}

		if(upnIdentifiers.size() > 0) {
			unique_patient_number = upnIdentifiers.get(0).getIdentifier();
		}

		Demographics demographics = new Demographics(facility, patient_clinic_number, unique_patient_number);
		model.addAttribute("profile", demographics);
	}

	class Demographics {

		private String facilityName;
		private String patientClinicNumber;
		private String uniquePatientNumber;

		Demographics() {

		}

		Demographics(String facilityName, String patientClinicNumber, String uniquePatientNumber) {
			this.facilityName = facilityName;
			this.patientClinicNumber = patientClinicNumber;
			this.uniquePatientNumber = uniquePatientNumber;
		}

		public String getFacilityName() {
			return facilityName;
		}

		public void setFacilityName(String facilityName) {
			this.facilityName = facilityName;
		}

		public String getPatientClinicNumber() {
			return patientClinicNumber;
		}

		public void setPatientClinicNumber(String patientClinicNumber) {
			this.patientClinicNumber = patientClinicNumber;
		}

		public String getUniquePatientNumber() {
			return uniquePatientNumber;
		}

		public void setUniquePatientNumber(String uniquePatientNumber) {
			this.uniquePatientNumber = uniquePatientNumber;
		}
	}
}
