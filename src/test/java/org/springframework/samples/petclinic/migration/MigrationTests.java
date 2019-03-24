package org.springframework.samples.petclinic.migration;

import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.visit.Visit;

import java.sql.SQLException;
import java.time.LocalDate;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.samples.petclinic.PetClinicApplication;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TDGHSQL.class, TDGSQLite.class})
public class MigrationTests {
    // Put your testing code for migration, consistency checking, etc below this comment:


    Owner owner1;


    Owner owner2;

    Owner owner3;


    Pet pet1;

    Pet pet2;

    Pet pet3;


    Visit visit1;

    Visit visit2;

    Visit visit3;

    private ConsistencyChecker consistencyChecker;

    @Before
    public void setup() {

        owner1 = new Owner(1, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
        owner2 = new Owner(1, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
        owner3 = new Owner(1, "Peter", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");

        pet1 = new Pet();
        pet1.setBirthDate(LocalDate.of(1990, 9, 3));
        pet1.setOwnerTdg(owner1);
        pet1.setId(1);
        pet1.setName("Smith");
        PetType type = new PetType(1, "cat");
        pet1.setType(type);

        pet2 = new Pet();
        pet2.setBirthDate(LocalDate.of(1990, 9, 3));
        pet2.setOwnerTdg(owner1);
        pet2.setId(1);
        pet2.setName("Smith");
        pet2.setType(type);

        pet3 = new Pet();
        pet3.setBirthDate(LocalDate.of(1990, 9, 3));
        pet3.setOwnerTdg(owner2);
        pet3.setType(type);

        visit1 = new Visit();
        visit1.setDescription("First visit");
        visit1.setPetId(10);
        visit1.setDate(LocalDate.of(1990, 9, 3));
        visit1.setId(9);

        visit2 = new Visit();
        visit2.setDescription("First visit");
        visit2.setPetId(10);
        visit2.setDate(LocalDate.of(1990, 9, 3));
        visit2.setId(9);

        visit3 = new Visit();
        visit3.setDescription("Third visit");
        visit3.setPetId(10);
        visit3.setDate(LocalDate.of(1990, 9, 3));
        visit3.setId(9);

        consistencyChecker = new ConsistencyChecker();

        TDGHSQL hsqldb = new TDGHSQL("jdbc:hsqldb:test");
        TDGSQLite sqlite = new TDGSQLite("jdbc:sqlite:test");

        PowerMockito.mockStatic(TDGHSQL.class);
        PowerMockito.mockStatic(TDGSQLite.class);
    }
    @Test
    public void testShadowWritesConsistencyCheckerSameOwner(){
        try {
            assertTrue(ConsistencyChecker.shadowWritesConsistencyCheckerOwner(owner1, owner2));
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testShadowWritesConsistencyCheckerDifferentOwner(){
        try {
            assertFalse(ConsistencyChecker.shadowWritesConsistencyCheckerOwner(owner1, owner3));
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testShadowWritesConsistencyCheckerSamePet(){
        try {
            assertTrue(ConsistencyChecker.shadowWritesConsistencyCheckerPet(pet1, pet2));
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testShadowWritesConsistencyCheckerDifferentPet(){
        try {
            assertFalse(ConsistencyChecker.shadowWritesConsistencyCheckerPet(pet1, pet3));
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testShadowWritesConsistencyCheckerSameVisit(){
        try {
            assertTrue(ConsistencyChecker.shadowWritesConsistencyCheckerVisit(visit1, visit2));
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testShadowWritesConsistencyCheckerDifferentVisit(){
        try {
            assertFalse(ConsistencyChecker.shadowWritesConsistencyCheckerVisit(visit1, visit3));
        }
        catch(SQLException e){
            e.printStackTrace();
        }
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
        when(TDGSQLite.getAllVetsConsistencyChecker()).thenReturn(newDatastoreVets);

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
