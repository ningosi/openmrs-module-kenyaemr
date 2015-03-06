package org.openmrs.module.kenyaemr.calculation.library.hiv;

import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyacore.calculation.AbstractPatientCalculation;
import org.openmrs.module.kenyacore.calculation.BooleanResult;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by codehub on 24/02/15.
 */
public class WrongPCNPatientsCalculation extends AbstractPatientCalculation {

	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {

		CalculationResultMap ret = new CalculationResultMap();

		PatientIdentifierType patientIdentifierType = MetadataUtils.existing(PatientIdentifierType.class, CommonMetadata._PatientIdentifierType.PATIENT_CLINIC_NUMBER);

		for (Integer ptId : cohort) {
			boolean hasWrongPcn = false;

			List<PatientIdentifier> patientIdentifier = Context.getPatientService().getPatientIdentifiers(null, Arrays.asList(patientIdentifierType), null, Arrays.asList(Context.getPatientService().getPatient(ptId)), null);
			if (!(patientIdentifier.isEmpty())) {
				String identifier = patientIdentifier.get(0).getIdentifier();
				if(!(checkIfNumber(identifier))) {
					hasWrongPcn = true;
				}
			}
			ret.put(ptId, new BooleanResult(hasWrongPcn, this));
		}

		return ret;
	}

	private boolean checkIfNumber(String val) {
		boolean num = false;
		if (val.matches("[0-9]+")){
			num = true;
		}
		return num;
	}
}

