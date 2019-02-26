package org.springframework.samples.petclinic.owner;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

/**
 * Test class for {@link Owner}
 *
 * @author kjanei
 * @author vineetkoppalkar
 * @author Mstc08
 */

public class OwnerTests {

    private static final int TEST_OWNER_ID = 1;

    private Owner george;

    private Pet mockNewPet;

    private Pet mockExistingPet;

    @Before
    public void setup() {
        george = new Owner();
        george.setId(TEST_OWNER_ID);
        george.setFirstName("George");
        george.setLastName("Franklin");
        george.setAddress("110 W. Liberty St.");
        george.setCity("Madison");
        george.setTelephone("6085551023");

        mockNewPet = mock(Pet.class);
        when(mockNewPet.getName()).thenReturn("Gary");
        when(mockNewPet.isNew()).thenReturn(true);

        mockExistingPet = mock(Pet.class);
        when(mockExistingPet.getName()).thenReturn("Garfield");
        when(mockExistingPet.isNew()).thenReturn(false);

        HashSet<Pet> petSet = new HashSet<>();
        petSet.add(mockNewPet);
        petSet.add(mockExistingPet);
        george.setPetsInternal(petSet);
    }

    @Test
    public void testGetNewlyCreatedPet() {
        // Test ignoreNew = true, expect not found
        Pet result = george.getPet("Gary",true);
        assertNull(result);
        // Test ignoreNew = false, expect found
        result = george.getPet("Gary", false);  // Equivalent to 1-parameter call
        assertEquals(mockNewPet, result);
    }

    @Test
    public void testGetExistingPet() {
        // Test ignoreNew = true, expect found
        Pet result = george.getPet("Garfield",true);
        assertEquals(mockExistingPet, result);
        // Test ignoreNew = false, expect found
        result = george.getPet("Garfield", false);  // Equivalent to 1-parameter call
        assertEquals(mockExistingPet, result);
    }

    @Test
    public void testGetNonExistentPetFail(){
        Pet result = george.getPet("Marques");
        assertNull(result);
    }
}
