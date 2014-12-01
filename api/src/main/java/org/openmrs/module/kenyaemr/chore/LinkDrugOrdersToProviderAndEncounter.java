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
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
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
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	Provider provider = providerService.getProviderByUuid("ae01b8ff-a4cc-4012-bcf7-72359e852e14");
	EncounterRole encounterRole = encounterService.getEncounterRoleByUuid("a0b03050-c99b-11e0-9572-0800200c9a66");
	Location defaultLocation = Context.getService(KenyaEmrService.class).getDefaultLocation();

		//fix nulls here
		fillNullsInOrderFrequencies(out);
		//patients with missing encounter for drug orders
		fillProvidersAndEncountersForDrugOrders(allPatients, out);
	}

	private void fillProvidersAndEncountersForDrugOrders(List<Patient> allPatients, PrintWriter out) {
		for(Patient patient : allPatients) {
		List<DrugOrder> allOrdersForAPatient;
			int count = 0;
		List<Encounter> allEncounterForThisPatient = Context.getEncounterService().getEncounters(patient, null, null, null, null, Arrays.asList(MetadataUtils.existing(EncounterType.class, HivMetadata._EncounterType.DRUG_ORDER)), null, null, null, true);
			for(Encounter encounters : allEncounterForThisPatient){
				allOrdersForAPatient = Context.getOrderService().getOrders(DrugOrder.class, Arrays.asList(encounters.getPatient()), null, OrderService.ORDER_STATUS.ANY, null, null, null);
				for (DrugOrder drugOrder : allOrdersForAPatient) {
					if(drugOrder.getOrderer() == null) {
						drugOrder.setOrderer(encounters.getCreator());
					}
					if(drugOrder.getEncounter() == null) {
						drugOrder.setEncounter(encounters);
						count++;
					}
				}
				out.println("Created "+count+" encounters");
			}
		}
	}

	private void fillEncountersForDrugOrders(List<Patient> allPatients) {
		Encounter encounter;
		Provider provider = Context.getProviderService().getProviderByUuid("ae01b8ff-a4cc-4012-bcf7-72359e852e14");
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByUuid("a0b03050-c99b-11e0-9572-0800200c9a66");
		Map<Date, Patient> allReturnedMatch = new HashMap<Date, Patient>(patientOrdersByDate(allPatients));
		for (Map.Entry<Date, Patient> entry : allReturnedMatch.entrySet()) {
			encounter = new Encounter();
			encounter.setEncounterDatetime(entry.getKey());
			encounter.setPatient(entry.getValue());
			encounter.setLocation(Context.getService(KenyaEmrService.class).getDefaultLocation());
			encounter.setForm(MetadataUtils.existing(Form.class, HivMetadata._Form.DRUG_ORDER));
			encounter.setEncounterType(MetadataUtils.existing(EncounterType.class, HivMetadata._EncounterType.DRUG_ORDER));
			encounter.setCreator(Context.getAuthenticatedUser());
			encounter.setProvider(encounterRole, provider);
			encounter.setDateCreated(new Date());
			Context.getEncounterService().saveEncounter(encounter);
		}
	}

	private Map<Date, Patient> patientOrdersByDate(List<Patient> allPatients) {
		OrderService orderService = Context.getOrderService();
		List<Order> allOrdersForPatient;
		Map<Date, Patient> allReturnedMatch = new HashMap<Date, Patient>();
		 for(Patient patient : allPatients) {
			 allOrdersForPatient = new ArrayList<Order>(orderService.getOrders(Order.class, Arrays.asList(patient), null, OrderService.ORDER_STATUS.ANY, null, null, null));
			 //go through all orders and save a patient and the start date in a map
			 for(Order orderForEachPatient : allOrdersForPatient) {
				 if (orderForEachPatient.getEncounter() == null) {
					 allReturnedMatch.put(orderForEachPatient.getStartDate(), patient);
				 }
			 }
		 }
		return  allReturnedMatch;
	}

	private void fillNullsInOrderFrequencies(PrintWriter out) {

		List<DrugOrder> allDrugOrdersForPatient = Context.getOrderService().getOrders(DrugOrder.class, null, null, OrderService.ORDER_STATUS.ANY, null, null, null);
		int countNulls = 0 ;
		int fixedNulls = 0;
			for (DrugOrder drugOrder: allDrugOrdersForPatient) {

				if (drugOrder.getFrequency().equals("null")) {
					countNulls++;
					//set it to once daily but can be changed later after migration
					drugOrder.setFrequency("OD");
					fixedNulls++;
				}
			}
		out.println("Found " + allDrugOrdersForPatient.size() + " drug orders and " + countNulls + " saved with null and fixed " + fixedNulls);
	}
}
