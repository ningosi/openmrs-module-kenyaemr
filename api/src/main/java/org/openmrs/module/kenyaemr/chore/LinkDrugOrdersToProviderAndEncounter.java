/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.kenyaemr.chore;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * To migrate to 1.10.x it is a requirement that each drug order should be associated with a provider and an encounter
 */
@Component("kenyaemr.chore.linkDrugOrdersToProviderAndEncounter")
public class LinkDrugOrdersToProviderAndEncounter extends AbstractChore {

	/**
	 * @see org.openmrs.module.kenyacore.chore.AbstractChore#perform(java.io.PrintWriter)
	 */

	@Override
	public void perform(PrintWriter out) {

		OrderService orderService = Context.getOrderService();
		PatientService patientService = Context.getPatientService();
		EncounterService encounterService = Context.getEncounterService();
		ProviderService providerService = Context.getProviderService();
		FormService formService = Context.getFormService();
		VisitService visitService = Context.getVisitService();
		Encounter drugEncounter = new Encounter();


		List<Patient> allPatients = patientService.getAllPatients();
		List<DrugOrder> allDrugOrdersForPatient;
		List<Order> allOrdersForPatient;



		Provider provider = providerService.getProviderByUuid("ae01b8ff-a4cc-4012-bcf7-72359e852e14");
		EncounterRole encounterRole = encounterService.getEncounterRoleByUuid("a0b03050-c99b-11e0-9572-0800200c9a66");
		Location defaultLocation = Context.getService(KenyaEmrService.class).getDefaultLocation();

		for (Patient patient: allPatients){
				allDrugOrdersForPatient = orderService.getDrugOrdersByPatient(patient);
				allOrdersForPatient = orderService.getOrdersByPatient(patient);
			Map<Date,Integer> drugsOrdersOnSameDate = new HashMap<Date, Integer>();
			for (DrugOrder drugOrder: allDrugOrdersForPatient) {

				if (drugOrder.getFrequency().equals("null")) {
					//set it to once daily but can be changed later after migration
					drugOrder.setFrequency("OD");
				}
			}
			//go through all orders and save a patient and the start date in a map
			for(Order orderForEachPatient : allOrdersForPatient) {
				drugsOrdersOnSameDate.put(orderForEachPatient.getStartDate(), orderForEachPatient.getPatient().getId());
			}

			//go through the map now that is has unique keys
			for(Order orderForEachPatient : allOrdersForPatient) {
				if (orderForEachPatient.getOrderer() == null) {
					orderForEachPatient.setOrderer(Context.getUserService().getUser(2));
				}
				for (Map.Entry<Date, Integer> entry : drugsOrdersOnSameDate.entrySet()) {
					//assign all orders tha have no provider a default one
					Set<Encounter> encountersForVisits = new HashSet<Encounter>();
					if (orderForEachPatient.getEncounter() == null) {
						//create an encounter for one and the use it to assign to the order
						drugEncounter.setEncounterDatetime(entry.getKey());
						drugEncounter.setProvider(encounterRole, provider);
						drugEncounter.setLocation(defaultLocation);

						drugEncounter.setPatient(patientService.getPatient(entry.getValue()));
						drugEncounter.setEncounterType(encounterService.getEncounterTypeByUuid(HivMetadata._EncounterType.DRUG_ORDER));
						drugEncounter.setForm(formService.getFormByUuid(HivMetadata._Form.DRUG_ORDER));

						//put this encounter in a set
						encountersForVisits.add(drugEncounter);
						//set up a visit for this encounter
						Visit visit = new Visit();
						visit.setLocation(defaultLocation);
						visit.setPatient(patientService.getPatient(entry.getValue()));
						visit.setEncounters(encountersForVisits);
						visit.setStartDatetime(entry.getKey());
						visit.setVisitType(visitService.getVisitType(1));

						//save the visit this will save plus all the encounters
						visitService.saveVisit(visit);
						//set the encounter to the order
						orderForEachPatient.setEncounter(drugEncounter);
					}
				}
			}
		}

	}
}
