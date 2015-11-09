package org.openmrs.module.kenyaemr.chore;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.module.kenyacore.calculation.CalculationUtils;
import org.openmrs.module.kenyacore.calculation.Calculations;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
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
        PatientCalculationContext context = Context.getService(PatientCalculationService.class).createCalculationContext();
        List<Patient> allPatients = Context.getPatientService().getAllPatients();
        List<DrugOrder> requiredPatientDrugOrders = new ArrayList<DrugOrder>();
        int count = 0;
        for(Patient patient: allPatients) {

            requiredPatientDrugOrders.addAll(orderService.getDrugOrdersByPatient(patient));
            for(DrugOrder drugOrder:requiredPatientDrugOrders){
                Encounter encounter = drugEncounter(drugOrder.getStartDate(), drugOrder.getPatient(), context);
                if(drugOrder.getEncounter() == null && encounter != null){
                    drugOrder.setEncounter(encounter);
                    drugOrder.setOrderer(encounter.getCreator());
                    orderService.saveOrder(drugOrder);
                    count++;
                }
            }
        }
        out.println(count+" drug orders fixed");

    }


    Encounter drugEncounter(Date date, Patient patient, PatientCalculationContext context){
        Encounter encounter =  null;
        EncounterType hivConsultation = MetadataUtils.existing(EncounterType.class, HivMetadata._EncounterType.HIV_CONSULTATION);
        EncounterType hivEnrollment = MetadataUtils.existing(EncounterType.class, HivMetadata._EncounterType.HIV_ENROLLMENT);
        EncounterType hivRegistration = MetadataUtils.existing(EncounterType.class, CommonMetadata._EncounterType.REGISTRATION);
        CalculationResultMap consultationEncounters = Calculations.allEncounters(hivConsultation, Arrays.asList(patient.getPatientId()), context);
        ListResult consultationEncountersListResults = (ListResult) consultationEncounters.get(patient.getPatientId());
        List<Encounter> consultationEncountersList = CalculationUtils.extractResultValues(consultationEncountersListResults);

        CalculationResultMap hivEnrollmentEncounterMap = Calculations.lastEncounter(hivEnrollment, Arrays.asList(patient.getPatientId()), context);
        Encounter hivEnrollmentEncounter = EmrCalculationUtils.encounterResultForPatient(hivEnrollmentEncounterMap, patient.getPatientId());

        CalculationResultMap hivRegistrationEncounterMap = Calculations.lastEncounter(hivRegistration, Arrays.asList(patient.getPatientId()), context);
        Encounter  hivRegistrationEncounter = EmrCalculationUtils.encounterResultForPatient(hivRegistrationEncounterMap, patient.getPatientId());
        for(Encounter encounter1:consultationEncountersList){
            if(DateUtil.getStartOfDay(date).equals(DateUtil.getStartOfDay(encounter1.getEncounterDatetime()))){
                encounter = encounter1;
                break;
            }
        }
        if(encounter == null && consultationEncountersList.size() > 0){
            encounter = consultationEncountersList.get(0);
        }
        else if(encounter == null && hivEnrollmentEncounter != null){
            encounter = hivEnrollmentEncounter;
        }
        else if(encounter == null && hivRegistrationEncounter != null){
            encounter = hivRegistrationEncounter;
        }
        return encounter;

    }
}
