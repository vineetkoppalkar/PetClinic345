package org.springframework.samples.petclinic.migration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.visit.Visit;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TDGHSQL.class, TDGSQLite.class})
public class MigrationTests {

    @Before
    public void setup() {
        TDGHSQL hsqldb = new TDGHSQL("jdbc:hsqldb:test");
        TDGSQLite sqlite = new TDGSQLite("jdbc:sqlite:test");

        PowerMockito.mockStatic(TDGHSQL.class);
        PowerMockito.mockStatic(TDGSQLite.class);
    }

    @Test
    public void testConsistencyCheckerOwners() {
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
    public void testConsistencyCheckerVisits() {
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
