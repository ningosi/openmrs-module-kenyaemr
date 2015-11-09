package org.openmrs.module.kenyaemr.chore;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.common.DateUtil;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by codehub on 11/6/15.
 * This class tries to conform the orders to the order entry API in openmrs 1.10+
 */
@Component("kenyaemr.chore.fixDrugEncountersAndProvider")
public class FixDrugEncountersAndProvider extends AbstractChore {

    @Override
    public void perform(PrintWriter out) {
        patientDrugOrders(out);
    }

    void patientDrugOrders(PrintWriter out){
        OrderService orderService = Context.getOrderService();
        List<Patient> allPatients = Context.getPatientService().getAllPatients();
        List<DrugOrder> requiredPatientDrugOrders = new ArrayList<DrugOrder>();
        int count = 0;
        for(Patient patient: allPatients) {

            requiredPatientDrugOrders.addAll(orderService.getDrugOrdersByPatient(patient, OrderService.ORDER_STATUS.ANY, true));
            if(requiredPatientDrugOrders.size() > 0) {
                for (DrugOrder drugOrder : requiredPatientDrugOrders) {
                    Encounter encounter = drugEncounter(drugOrder.getStartDate(), drugOrder.getPatient());
                    if (drugOrder.getEncounter() == null && encounter != null) {
                        drugOrder.setEncounter(encounter);
                        drugOrder.setOrderer(encounter.getCreator());
                        orderService.saveOrder(drugOrder);
                        count++;
                    }

                }

            }
        }
        out.println("Fixed "+count+" drug orders");


    }


    Encounter drugEncounter(Date date, Patient patient){
        Encounter encounter =  null;
        EncounterService encounterService = Context.getEncounterService();
        EncounterType hivConsultation = MetadataUtils.existing(EncounterType.class, HivMetadata._EncounterType.HIV_CONSULTATION);
        EncounterType hivEnrollment = MetadataUtils.existing(EncounterType.class, HivMetadata._EncounterType.HIV_ENROLLMENT);
        EncounterType hivRegistration = MetadataUtils.existing(EncounterType.class, CommonMetadata._EncounterType.REGISTRATION);


        List<Encounter> encounterList = encounterService.getEncounters(patient, null, null, date, null, Arrays.asList(hivConsultation), null,null,null, false);
        List<Encounter> hivEnrollmentEncounter = encounterService.getEncounters(patient, null, null, date, null, Arrays.asList(hivEnrollment), null,null,null, false);
        List<Encounter> hivRegistrationEncounter = encounterService.getEncounters(patient, null, null, date, null, Arrays.asList(hivRegistration), null,null,null, false);

        for(Encounter encounter1:encounterList){
            if(DateUtil.getStartOfDay(date).equals(DateUtil.getStartOfDay(encounter1.getEncounterDatetime()))){
                encounter = encounter1;
                break;
            }

        }
        if(encounter == null && encounterList.size() > 0){
            encounter = encounterList.get(0);
        }
        else if(encounter == null && hivEnrollmentEncounter.size() > 0){
            encounter = hivEnrollmentEncounter.get(0);
        }
        else if(encounter == null && hivRegistrationEncounter.size() > 0){
            encounter = hivRegistrationEncounter.get(0);
        }
        return encounter;

    }
}
