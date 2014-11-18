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
		String names = Context.getPatientService().getPatient(patient.getId()).getPersonName().getFullName();
		String gender = Context.getPatientService().getPatient(patient.getId()).getGender();
		String dob = Context.getPatientService().getPatient(patient.getId()).getBirthdate().toString();
		String age = Context.getPatientService().getPatient(patient.getId()).getAge().toString();
		String address = Context.getPatientService().getPatient(patient.getId()).getPersonAddress().getAddress1();
		String tel = "N/A";
		//tel = Context.getPersonService().getPerson(patient.getId()).getAttribute("Telephone contact").getValue();
		//tel = tel != null ? tel : "";
		if(pcnIdentifiers.size() > 0) {
			patient_clinic_number = pcnIdentifiers.get(0).getIdentifier();
		}

		if(upnIdentifiers.size() > 0) {
			unique_patient_number = upnIdentifiers.get(0).getIdentifier();
		}

		Demographics demographics = new Demographics(facility, patient_clinic_number, unique_patient_number, names, gender, dob, age, address, tel);
		model.addAttribute("profile", demographics);
	}

	class Demographics {

		private String facilityName;
		private String patientClinicNumber;
		private String uniquePatientNumber;
		private String gender;
		private String address;

		public String getTel() {
			return tel;
		}

		public void setTel(String tel) {
			this.tel = tel;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		private String tel;

		public String getGender() {
			return gender;
		}

		public void setGender(String gender) {
			this.gender = gender;
		}

		public String getAge() {
			return age;
		}

		public void setAge(String age) {
			this.age = age;
		}

		public String getDob() {
			return dob;
		}

		public void setDob(String dob) {
			this.dob = dob;
		}

		private String dob;
		private String age;


		public String getNames() {
			return names;
		}

		public void setNames(String names) {
			this.names = names;
		}

		private String names;

		Demographics() {

		}

		Demographics(String facilityName, String patientClinicNumber, String uniquePatientNumber, String names, String gender, String dob, String age, String address, String tel) {
			this.facilityName = facilityName;
			this.patientClinicNumber = patientClinicNumber;
			this.uniquePatientNumber = uniquePatientNumber;
			this.names = names;
			this.gender = gender;
			this.dob = dob;
			this.age = age;
			this.address = address;
			this.tel = tel;
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
