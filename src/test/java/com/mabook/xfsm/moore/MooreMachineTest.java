package com.mabook.xfsm.moore;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static com.mabook.xfsm.moore.MooreMachine.*;
import static org.junit.Assert.*;

public class MooreMachineTest {
    @Test(expected = NoRoutesException.class)
    public void testTrasition() throws NoRoutesException {
        Map<String, Map<String, String>> map = MAP("S0", MAP("E0", "S1"));
        MooreMachine mm = new MooreMachine(map);
        String newState = mm.transit("S0", "E0");
        assertEquals("S1", newState);

        newState = mm.transit("S1", "E0");
    }
}