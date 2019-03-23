package org.springframework.samples.petclinic.migration;

import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.Visit;

import java.sql.SQLException;
import java.time.LocalDate;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.samples.petclinic.owner.PetType;

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


    Vet vet1;

    Vet vet2;

    Vet vet3;

    Collection<Owner> collection1 = new ArrayList<Owner>();


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

        pet2 = new Pet();
        pet2.setBirthDate(LocalDate.of(1990, 9, 3));
        pet2.setOwnerTdg(owner1);
        pet2.setId(1);
        pet2.setName("Smith");

        pet3 = new Pet();
        pet3.setBirthDate(LocalDate.of(1990, 9, 3));
        pet3.setOwnerTdg(owner2);

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

        vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("Bob");
        vet1.setLastName("Bubbly");

        vet2 = new Vet();
        vet2.setId(1);
        vet2.setFirstName("Bob");
        vet2.setLastName("Bubbly");

        vet3 = new Vet();
        vet3.setId(1);
        vet3.setFirstName("Bob");
        vet3.setLastName("Bobba");

        TDGHSQL hsqldb = new TDGHSQL("jdbc:hsqldb:test");
        TDGSQLite sqlite = new TDGSQLite("jdbc:sqlite:test");

        PowerMockito.mockStatic(TDGHSQL.class);
        PowerMockito.mockStatic(TDGSQLite.class);
    }

    @Test
    public void testShadowWriteAndReadConsistencyCheckerSameOwner(){
        try {
            assertTrue(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerOwner(owner1, owner2));
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testShadowWriteAndReadConsistencyCheckerDifferentOwner(){
        try {
            assertFalse(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerOwner(owner1, owner3));
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testShadowWriteAndReadConsistencyCheckerSamePet(){
        try {
            assertTrue(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerPet(pet1, pet2));
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testShadowWriteAndReadConsistencyCheckerDifferentPet(){
        try {
            assertFalse(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerPet(pet1, pet3));
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testShadowWriteAndReadConsistencyCheckerSameVisit(){
        try {
            assertTrue(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerVisit(visit1, visit2));
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

    @Test
    public void testShadowWriteAndReadConsistencyCheckerDifferentVisit(){
        try {
            assertFalse(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerVisit(visit1, visit3));
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testShadowWriteAndReadConsistencyCheckerSameVet(){
        try {
            assertFalse(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerVet(vet1, vet2));
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Test
    public void testShadowWriteAndReadConsistencyCheckerDifferentVet(){
        try {
            assertFalse(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerVet(vet1, vet3));
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }


    @Test
    public void testShadowWriteAndReadConsistencyCheckerConsistentCollection(){
        Owner expectedOwner = owner1;
        Owner actualOwner = owner2;

        List<Owner> oldDatastoreOwners = new ArrayList<>();
        oldDatastoreOwners.add(expectedOwner);
        oldDatastoreOwners.add(actualOwner);

        List<Owner> newDatastoreOwners = new ArrayList<>();
        newDatastoreOwners.add(expectedOwner);
        newDatastoreOwners.add(actualOwner);

        when(TDGHSQL.getAllOwners()).thenReturn(oldDatastoreOwners);
        when(TDGSQLite.getAllOwners()).thenReturn(newDatastoreOwners);

        assertTrue(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerOwners(oldDatastoreOwners));
    }

    @Test
    public void testShadowWriteAndReadConsistencyCheckerInconsistentCollection(){
        Owner expectedOwner = owner1;
        Owner actualOwner = owner2;

        List<Owner> oldDatastoreOwners = new ArrayList<>();
        oldDatastoreOwners.add(expectedOwner);
        oldDatastoreOwners.add(actualOwner);

        List<Owner> newDatastoreOwners = new ArrayList<>();
        newDatastoreOwners.add(expectedOwner);

        when(TDGHSQL.getAllOwners()).thenReturn(oldDatastoreOwners);
        when(TDGSQLite.getAllOwners()).thenReturn(newDatastoreOwners);

        assertFalse(ConsistencyChecker.shadowWritesAndReadsConsistencyCheckerOwners(oldDatastoreOwners));
    }

        @Test
        public void testConsistencyCheckerOwners () {
            Owner expectedOwner = new Owner(1, "Bob", "Billy", "address", "city", "telephone");
            Owner actualOwner = new Owner(1, "Jones", "Billy", "address", "city", "telephone");

            List<Owner> oldDatastoreOwners = new ArrayList<>();
            oldDatastoreOwners.add(expectedOwner);

            List<Owner> newDatastoreOwners = new ArrayList<>();
            newDatastoreOwners.add(actualOwner);

            when(TDGHSQL.getAllOwners()).thenReturn(oldDatastoreOwners);
            when(TDGSQLite.getAllOwners()).thenReturn(newDatastoreOwners);

            ConsistencyChecker cc = new ConsistencyChecker();
            cc.ownerCheckConsistency();

            assertEquals(1, cc.getNbOfOwnerInconsistencies());
        }

        @Test
        public void testConsistencyCheckerPets () {
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

            ConsistencyChecker cc = new ConsistencyChecker();
            cc.petCheckConsistency();

            assertEquals(1, cc.getNbOfPetInconsistencies());
        }

        @Test
        public void testConsistencyCheckerVisits () {
            Visit expectedVisit = new Visit(1, 2, "Expected Pet", LocalDate.parse("2007-12-03"));
            Visit actualVisit = new Visit(1, 2, "Actual Pet", LocalDate.parse("2007-12-03"));

            List<Visit> oldDatastoreVisits = new ArrayList<>();
            oldDatastoreVisits.add(expectedVisit);

            List<Visit> newDatastoreVisits = new ArrayList<>();
            newDatastoreVisits.add(actualVisit);

            when(TDGHSQL.getAllVisits()).thenReturn(oldDatastoreVisits);
            when(TDGSQLite.getAllVisits()).thenReturn(newDatastoreVisits);

            ConsistencyChecker cc = new ConsistencyChecker();
            cc.visitCheckConsistency();

            assertEquals(1, cc.getNbOfVisitInconsistencies());
        }

}
