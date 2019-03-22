package org.springframework.samples.petclinic.migration;

import org.junit.Test;
import org.mockito.Mock;
import org.springframework.samples.petclinic.owner.Owner;

import javax.validation.constraints.AssertTrue;
import java.sql.SQLException;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

public class MigrationTests {
    // Put your testing code for migration, consistency checking, etc below this comment:

    @Mock
    Owner owner1;

    @Mock
    Owner owner2;

    @Test
    public void testShadowWriteOwnerConsistencyChecker(){
        when(owner1.getId()).thenReturn(1);
        when(owner1.getFirstName()).thenReturn("George");
        when(owner1.getLastName()).thenReturn("Franklin");
        when(owner1.getAddress()).thenReturn("110 W. Liberty St.");
        when(owner1.getCity()).thenReturn("Madison");
        when(owner1.getTelephone()).thenReturn("6085551023");

        when(owner2.getId()).thenReturn(1);
        when(owner2.getFirstName()).thenReturn("George");
        when(owner2.getLastName()).thenReturn("Franklin");
        when(owner2.getAddress()).thenReturn("110 W. Liberty St.");
        when(owner2.getCity()).thenReturn("Madison");
        when(owner2.getTelephone()).thenReturn("6085551023");

        try {
            assertTrue(ConsistencyChecker.shadowWritesOwner(owner1, owner2));

        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

}
