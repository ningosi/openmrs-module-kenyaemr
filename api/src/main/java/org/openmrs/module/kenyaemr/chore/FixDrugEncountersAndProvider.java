package org.openmrs.module.kenyaemr.chore;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Program;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.kenyacore.calculation.Calculations;
import org.openmrs.module.kenyacore.calculation.Filters;
import org.openmrs.module.kenyacore.chore.AbstractChore;
import org.openmrs.module.kenyaemr.api.KenyaEmrService;
import org.openmrs.module.kenyaemr.calculation.EmrCalculationUtils;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.HivMetadata;
import org.openmrs.module.kenyaemr.metadata.TbMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.Arrays;
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
        PatientService patientService = Context.getPatientService();
        Set<Integer> orderPatientsOnly = new HashSet<Integer>();
        PatientCalculationContext context = Context.getService(PatientCalculationService.class).createCalculationContext();
        User user = someoneWhoSavedEncounter();
        for(DrugOrder order :orderService.getOrders(DrugOrder.class, null, null, OrderService.ORDER_STATUS.ANY, null, null, null)){
            if(order.getEncounter() == null) {
                orderPatientsOnly.add(order.getPatient().getPatientId());
            }
        }
        System.out.println("We expect ::::"+orderPatientsOnly.size()+" patients' drug orders would be updated");
        List<DrugOrder> requiredPatientDrugOrders;
        int count = 0;
        for(Integer patient: orderPatientsOnly) {

            requiredPatientDrugOrders = orderService.getOrders(DrugOrder.class, Arrays.asList(patientService.getPatient(patient)), null, OrderService.ORDER_STATUS.ANY, null, null, null);
                for (DrugOrder drugOrder : requiredPatientDrugOrders) {
                    context.setNow(drugOrder.getStartDate());
                    Encounter encounter = drugEncounter(drugOrder.getPatient(), context);

                    if (drugOrder.getEncounter() == null && encounter != null) {
                        drugOrder.setEncounter(encounter);
                        drugOrder.setOrderer(user);
                        orderService.saveOrder(drugOrder);

                    }
                }
            count++;

            System.out.println("Done with patients :::::"+count);
        }


    }


    Encounter drugEncounter(Patient patient, PatientCalculationContext context){
        Encounter encounter =  null;
        EncounterService encounterService = Context.getEncounterService();
        KenyaEmrService kenyaEmrService = Context.getService(KenyaEmrService.class);
        EncounterType hivConsultation = MetadataUtils.existing(EncounterType.class, HivMetadata._EncounterType.HIV_CONSULTATION);
        EncounterType hivEnrollment = MetadataUtils.existing(EncounterType.class, HivMetadata._EncounterType.HIV_ENROLLMENT);
        EncounterType hivRegistration = MetadataUtils.existing(EncounterType.class, CommonMetadata._EncounterType.REGISTRATION);
        EncounterType tbEnrollment = MetadataUtils.existing(EncounterType.class, TbMetadata._EncounterType.TB_ENROLLMENT);

        Program hivProgram = MetadataUtils.existing(Program.class, HivMetadata._Program.HIV);
        Set<Integer> inHivProgram = Filters.inProgram(hivProgram, Arrays.asList(patient.getPatientId()), context);


        CalculationResultMap encounterList = Calculations.lastEncounter(hivConsultation, Arrays.asList(patient.getPatientId()), context);
        Encounter encounter1 = EmrCalculationUtils.encounterResultForPatient(encounterList, patient.getPatientId());

        CalculationResultMap hivEnrollmentEncounter = Calculations.lastEncounter(hivEnrollment, Arrays.asList(patient.getPatientId()), context);
        Encounter encounter2 = EmrCalculationUtils.encounterResultForPatient(hivEnrollmentEncounter, patient.getPatientId());

        CalculationResultMap hivRegistrationEncounter = Calculations.lastEncounter(hivRegistration, Arrays.asList(patient.getPatientId()), context);
        Encounter encounter3 = EmrCalculationUtils.encounterResultForPatient(hivRegistrationEncounter, patient.getPatientId());

        List<Encounter> allOtherEncounters = Context.getEncounterService().getEncounters(patient, null, null, null, null,Arrays.asList(hivConsultation,hivEnrollment,hivRegistration,tbEnrollment),null,null,null, true);

        if(encounter1 != null){
            encounter = encounter1;
        }
        else if(encounter2 != null){
            encounter = encounter2;
        }
        else if(encounter3 != null){
            encounter =encounter3;
        }
        else if(allOtherEncounters.size() > 0){
            encounter = allOtherEncounters.get(allOtherEncounters.size() - 1);
        }


        if(encounter == null && inHivProgram.contains(patient.getPatientId())){
            ProviderService providerService = Context.getProviderService();
            EncounterRole encounterRole = encounterService.getEncounterRoleByUuid("a0b03050-c99b-11e0-9572-0800200c9a66");
            Provider provider = providerService.getProviderByUuid("ae01b8ff-a4cc-4012-bcf7-72359e852e14");

            Encounter hivEncounter = new Encounter();
            hivEncounter.setEncounterDatetime(context.getNow());
            hivEncounter.setLocation(kenyaEmrService.getDefaultLocation());
            hivEncounter.setPatient(patient);
            hivEncounter.setEncounterType(hivConsultation);
            hivEncounter.setProvider(encounterRole, provider);
            hivEncounter.setCreator(Context.getAuthenticatedUser());

            encounterService.saveEncounter(hivEncounter);

            encounter = hivEncounter;
        }
        return encounter;

    }

    User someoneWhoSavedEncounter(){
        UserService userService = Context.getUserService();
        PersonService personService = Context.getPersonService();
        ProviderService providerService = Context.getProviderService();

        PersonName personName = new PersonName();
        personName.setCreator(Context.getAuthenticatedUser());
        personName.setDateCreated(new Date());
        personName.setFamilyName("Drugs");
        personName.setGivenName("Other");
        personName.setMiddleName("Migrator");

        Person person = new Person();
        person.setCreator(Context.getAuthenticatedUser());
        person.setDateCreated(new Date());
        person.setBirthdate(new Date());
        person.addName(personName);
        person.setGender("M");

        personService.savePerson(person);


        User user = new User();
        user.setUsername("drugOrderUser");
        user.setCreator(Context.getAuthenticatedUser());
        user.setPerson(person);


        //save the user
        userService.saveUser(user, "Test12345");

        //make this use a provider
        Provider provider = new Provider();
        provider.setPerson(person);
        provider.setName("Drug orderer provider");
        provider.setDateCreated(new Date());
        provider.setCreator(Context.getAuthenticatedUser());
        provider.setIdentifier("DRUG ORDERS");
        provider.setDescription("This provider is mainly for setting up orders that were entered previously");
        providerService.saveProvider(provider);

        return  user;
    }
}
