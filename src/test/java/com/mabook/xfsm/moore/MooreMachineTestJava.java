package com.mabook.xfsm.moore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MooreMachineTestJava {
    @Test
    public void testInJava() {
        String routesJson = "{ \"S0\" : { \"E0\" : \"S1\" } }";
        Type tt = new TypeToken<Map<String, Map<String, String>>>() {
        }.getType();
        Map<String, Map<String, String>> routes = new Gson().fromJson(routesJson, tt);

        MooreMachine mm = new MooreMachine(routes);
        String state1 = mm.transit("S0", "E1");

        assertNull(state1);


        List<String> states = mm.findState0("E0");
        assertEquals(1, states.size());
        assertTrue(states.contains("S0"));
    }

}