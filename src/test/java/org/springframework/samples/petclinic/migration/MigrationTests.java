package org.springframework.samples.petclinic.migration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.samples.petclinic.PetClinicApplication;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.Visit;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TDGHSQL.class, TDGSQLite.class})
public class MigrationTests {

    private ConsistencyChecker consistencyChecker;
    private Forklift forklift;
    private TDGHSQL hsqldb;
    private TDGSQLite sqlite;

    @Before
    public void setup() {
        hsqldb = new TDGHSQL("jdbc:hsqldb:test");
        sqlite = new TDGSQLite("jdbc:sqlite:test");

        consistencyChecker = new ConsistencyChecker();
        PowerMockito.mockStatic(TDGHSQL.class);
        PowerMockito.mockStatic(TDGSQLite.class);
    }

    @Test
    public void testForklift() {
        // Get fake ResultSets from testing DB
        ResultSet owners = sqlite.selectQuery("SELECT * FROM owners WHERE id = 1");
        ResultSet pets = sqlite.selectQuery("SELECT * FROM pets WHERE id = 1");
        ResultSet specialties = sqlite.selectQuery("SELECT * FROM specialties WHERE id = 1");
        ResultSet types = sqlite.selectQuery("SELECT * FROM types WHERE id = 1");
        ResultSet vetSpecialties = sqlite.selectQuery("SELECT * FROM types WHERE vet_id = 2 AND specialty_id = 1");
        ResultSet vets = sqlite.selectQuery("SELECT * FROM vets WHERE id = 1");
        ResultSet visits = sqlite.selectQuery("SELECT * FROM vets WHERE id = 1");
        // Mock out the forklift calls with the fake ResultSets
        when(TDGHSQL.forkliftAllOwners()).thenReturn(owners);
        when(TDGHSQL.forkliftAllPets()).thenReturn(pets);
        when(TDGHSQL.forkliftAllSpecialties()).thenReturn(specialties);
        when(TDGHSQL.forkliftAllTypes()).thenReturn(types);
        when(TDGHSQL.forkliftAllVetSpecialties()).thenReturn(vetSpecialties);
        when(TDGHSQL.forkliftAllVets()).thenReturn(vets);
        when(TDGHSQL.forkliftAllVisits()).thenReturn(visits);
        // Call class under test
        int counter = forklift.forkliftDatabase();
        // Assert correct amount of tables have been forklifted
        assertEquals(7, counter);
    }

    @Test
    public void testConsistencyCheckerOwners() {

        if (!PetClinicApplication.consistencyChecker)
            return;

        if(!PetClinicApplication.consistencyCheckerOwner)
            return;

        Owner expectedOwner = new Owner(1, "Bob", "Billy", "address", "city", "telephone");
        Owner actualOwner = new Owner(1, "Jones", "Billy", "address", "city", "telephone");

        List<Owner> oldDatastoreOwners = new ArrayList<>();
        oldDatastoreOwners.add(expectedOwner);

        List<Owner> newDatastoreOwners = new ArrayList<>();
        newDatastoreOwners.add(actualOwner);

        when(TDGHSQL.getAllOwners()).thenReturn(oldDatastoreOwners);
        when(TDGSQLite.getAllOwners()).thenReturn(newDatastoreOwners);

        consistencyChecker.ownerCheckConsistency();

        assertEquals(1, consistencyChecker.getNbOfOwnerInconsistencies());
    }

    @Test
    public void testConsistencyCheckerPets() {

        if (!PetClinicApplication.consistencyChecker)
            return;

        if(!PetClinicApplication.consistencyCheckerPet)
            return;

        PetType catType = new PetType();
        catType.setId(1);
        catType.setName("cat");

        Owner petOwner = new Owner(1, "Sam", "Billy", "address", "city", "telephone");

        Pet expectedPet = new Pet(1, "Bob", LocalDate.parse("2007-12-03"), catType, petOwner);
        Pet actualPet = new Pet(1, "Jones", LocalDate.parse("2007-12-03"), catType, petOwner);

        List<Pet> oldDatastorePets = new ArrayList<>();
        oldDatastorePets.add(expectedPet);

        List<Pet> newDatastorePets = new ArrayList<>();
        newDatastorePets.add(actualPet);

        when(TDGHSQL.getAllPets()).thenReturn(oldDatastorePets);
        when(TDGSQLite.getAllPets()).thenReturn(newDatastorePets);

        consistencyChecker.petCheckConsistency();

        assertEquals(1, consistencyChecker.getNbOfPetInconsistencies());
    }

    @Test
    public void testConsistencyCheckerVisits() {

        if (!PetClinicApplication.consistencyChecker)
            return;

        if(!PetClinicApplication.consistencyCheckerVisit)
            return;

        Visit expectedVisit = new Visit(1, 2, "Expected Pet", LocalDate.parse("2007-12-03"));
        Visit actualVisit = new Visit(1, 2, "Actual Pet", LocalDate.parse("2007-12-03"));

        List<Visit> oldDatastoreVisits = new ArrayList<>();
        oldDatastoreVisits.add(expectedVisit);

        List<Visit> newDatastoreVisits = new ArrayList<>();
        newDatastoreVisits.add(actualVisit);

        when(TDGHSQL.getAllVisits()).thenReturn(oldDatastoreVisits);
        when(TDGSQLite.getAllVisits()).thenReturn(newDatastoreVisits);

        consistencyChecker.visitCheckConsistency();

        assertEquals(1, consistencyChecker.getNbOfVisitInconsistencies());
    }

    @Test
    public void testConsistencyCheckerVets() {

        if (!PetClinicApplication.consistencyChecker)
            return;

        if(!PetClinicApplication.consistencyCheckerVet)
            return;

        Vet expectedVet = new Vet(1, "Billy", "Maze");
        Vet actualVet = new Vet(1, "Billy", "Jones");

        List<Vet> oldDatastoreVets = new ArrayList<>();
        oldDatastoreVets.add(expectedVet);

        List<Vet> newDatastoreVets = new ArrayList<>();
        newDatastoreVets.add(actualVet);

        when(TDGHSQL.getAllVets()).thenReturn(oldDatastoreVets);
        when(TDGSQLite.getAllVets()).thenReturn(newDatastoreVets);

        consistencyChecker.vetCheckConsistency();

        assertEquals(1, consistencyChecker.getNbOfVetInconsistencies());
    }

    @Test
    public void testConsistencyCheckerSpecialties() {

        if (!PetClinicApplication.consistencyChecker)
            return;

        if(!PetClinicApplication.consistencyCheckerSpecialty)
            return;

        Specialty expectedSpecialty = new Specialty(1, "radiology");
        Specialty actualSpecialty = new Specialty(1, "surgery");

        List<Specialty> oldDatastoreSpecialties = new ArrayList<>();
        oldDatastoreSpecialties.add(expectedSpecialty);

        List<Specialty> newDatastoreSpecialties = new ArrayList<>();
        newDatastoreSpecialties.add(actualSpecialty);

        when(TDGHSQL.getAllSpecialties()).thenReturn(oldDatastoreSpecialties);
        when(TDGSQLite.getAllSpecialties()).thenReturn(newDatastoreSpecialties);

        consistencyChecker.specialtiesCheckConsistency();

        assertEquals(1, consistencyChecker.getNbOfSpecialtiesInconsistencies());
    }

    @Test
    public void testConsistencyCheckerTypes() {

        if (!PetClinicApplication.consistencyChecker)
            return;

        if(!PetClinicApplication.consistencyCheckerType)
            return;

        PetType expectedPetType = new PetType(1, "cat");
        PetType actualPetType = new PetType(1, "dog");

        List<PetType> oldDatastorePetTypes = new ArrayList<>();
        oldDatastorePetTypes.add(expectedPetType);

        List<PetType> newDatastorePetTypes = new ArrayList<>();
        newDatastorePetTypes.add(actualPetType);

        when(TDGHSQL.getAllTypes()).thenReturn(oldDatastorePetTypes);
        when(TDGSQLite.getAllTypes()).thenReturn(newDatastorePetTypes);

        consistencyChecker.typesCheckConsistency();

        assertEquals(1, consistencyChecker.getNbOfTypeInconsistencies());
    }
}
