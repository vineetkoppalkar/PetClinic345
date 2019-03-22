package org.springframework.samples.petclinic.migration;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class MigrationTests {
    // Put your testing code for migration, consistency checking, etc below this comment:

    TDGHSQL mockHsql = mock(TDGHSQL.class);
    TDGSQLite mockSqLite = mock(TDGSQLite.class);
    when(mockHsql.)thenReturn();

    ConsistencyChecker cc = new ConsistencyChecker(mockHsql, mockSqLite);

    @Test
    public void testConsistencyChecker() {

    }
}
