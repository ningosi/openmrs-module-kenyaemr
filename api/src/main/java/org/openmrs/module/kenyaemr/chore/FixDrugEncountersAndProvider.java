package org.openmrs.module.kenyaemr.chore;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        EncounterService encounterService = Context.getEncounterService();
        ProviderService providerService = Context.getProviderService();

        List<Patient> allPatients = Context.getPatientService().getAllPatients();
        Set<Encounter> encountersForPatient = new HashSet<Encounter>();
        for(Patient patient: allPatients){
            List<DrugOrder> patientDrugorder = orderService.getDrugOrdersByPatient(patient);
            //loop through for every drug order and have a set of dates
            Set<Date> uniqueDrugOrderSet = new HashSet<Date>();
            for(DrugOrder drugOrder:patientDrugorder){
              uniqueDrugOrderSet.add(drugOrder.getStartDate());
            }
            //create encounters based on the dates in the set for this patient
            if(uniqueDrugOrderSet.size() > 0){
                for(Date encounterDate: uniqueDrugOrderSet){
                    Encounter drugEncounter = new Encounter();
                    Provider provider = providerService.getProviderByUuid(CommonMetadata._Provider.UNKNOWN);
                    EncounterRole role = encounterService.getEncounterRoleByUuid("a0b03050-c99b-11e0-9572-0800200c9a66");

                    drugEncounter.setEncounterDatetime(encounterDate);
                    drugEncounter.setProvider(role,provider);
                    drugEncounter.setEncounterType(MetadataUtils.existing(EncounterType.class, HivMetadata._EncounterType.HIV_CONSULTATION));
                    drugEncounter.setPatient(patient);
                    drugEncounter.setLocation(Context.getService(KenyaEmrService.class).getDefaultLocation());
                    drugEncounter.setCreator(Context.getAuthenticatedUser());
                    drugEncounter.setDateCreated(new Date());

                    //save the encounter here
                    encounterService.saveEncounter(drugEncounter);
                    encountersForPatient.add(drugEncounter);
                    //the same encounter needs to be set where drug orders have the same date as the encounter

                    for(DrugOrder drugOrder:patientDrugorder){
                        if(drugOrder.getStartDate().equals(drugEncounter.getEncounterDatetime())){
                            drugOrder.setEncounter(drugEncounter);
                            drugOrder.setOrderer(Context.getAuthenticatedUser());
                        }
                    }

                }
            }
            out.println("Patient "+patient.getPatientId()+" has "+encountersForPatient.size()+" drug encounters added");
        }

    }
}
