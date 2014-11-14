package org.openmrs.module.kenyaemr.fragment.controller.summaries;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.Arrays;
import java.util.List;

/**
 * Patient summaries fragment
 */
public class SummariesFragmentController {

	public void controller(@FragmentParam("patient")
						   Patient patient,
						   FragmentModel model,
						   UiUtils ui) {
		Form MOH_257_THERAPY_ENCOUNTER_FORM = Context.getFormService().getFormByUuid(HivMetadata._Form.MOH_257_VISIT_SUMMARY);
		EncounterType encounterType = Context.getEncounterService().getEncounterTypeByUuid(HivMetadata._EncounterType.HIV_CONSULTATION);
		List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, Arrays.asList(MOH_257_THERAPY_ENCOUNTER_FORM), Arrays.asList(encounterType), null, null, null, false);
		Fields visitDetails = null;
		ConceptService conceptService = Context.getConceptService();
		for(Encounter enc : encounters) {
			String visitType = "";
			String scheduled = "";
			for(Obs obs : enc.getAllObs(false)) {
				if (obs.getConcept().equals(conceptService.getConceptByUuid("161643AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
					visitType = obs.getValueCoded().getName().getName();
				}
				if (obs.getConcept().equals(conceptService.getConceptByUuid("1246AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
					scheduled = "YES";
				}
				if ( !(obs.getConcept().equals(conceptService.getConceptByUuid("1246AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")))) {
					scheduled = "NO";
				}
			}
			visitDetails = new Fields(visitType, scheduled);
		}
		model.addAttribute("visitDetails", visitDetails);
		model.addAttribute("allEncounters", encounters);
	}

	class Fields {
		private String visittype;
		private String scheduled;

		Fields(String visittype, String scheduled) {
			this.visittype = visittype;
			this.scheduled = scheduled;
		}

		public String getVisittype() {
			return visittype;
		}

		public void setVisittype(String visittype) {
			this.visittype = visittype;
		}

		public String getScheduled() {
			return scheduled;
		}

		public void setScheduled(String scheduled) {
			this.scheduled = scheduled;
		}

	}
}
