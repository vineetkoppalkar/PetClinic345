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

    @Before
    public void setup(){

        owner1 = new Owner(1, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
        owner2 = new Owner(1, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
        owner3 = new Owner(1, "Peter", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");

        pet1 = new Pet();
        pet1.setBirthDate(LocalDate.of(1990,9,3));
        pet1.setOwnerTdg(owner1);
        pet1.setId(1);
        pet1.setName("Smith");

        pet2 = new Pet();
        pet2.setBirthDate(LocalDate.of(1990,9,3));
        pet2.setOwnerTdg(owner1);
        pet2.setId(1);
        pet2.setName("Smith");

        pet3 = new Pet();
        pet3.setBirthDate(LocalDate.of(1990,9,3));
        pet3.setOwnerTdg(owner2);

        visit1 = new Visit();
        visit1.setDescription("First visit");
        visit1.setPetId(10);
        visit1.setDate(LocalDate.of(1990,9,3));
        visit1.setId(9);

        visit2 = new Visit();
        visit2.setDescription("First visit");
        visit2.setPetId(10);
        visit2.setDate(LocalDate.of(1990,9,3));
        visit2.setId(9);

        visit3 = new Visit();
        visit3.setDescription("Third visit");
        visit3.setPetId(10);
        visit3.setDate(LocalDate.of(1990,9,3));
        visit3.setId(9);
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

}
