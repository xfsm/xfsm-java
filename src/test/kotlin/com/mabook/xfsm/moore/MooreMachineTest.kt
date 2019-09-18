package com.mabook.xfsm.moore

import org.junit.Assert.*
import org.junit.Test

class MooreMachineTest {

    @Test
    fun testTransition() {
        val map: Routes = mapOf(
                "S0" to mapOf("E0" to "S1")
        )
        val mm = MooreMachine(map)
        val newState = mm.transit("S0", "E0")
        assertEquals("S1", newState)
        assertEquals("S1", newState)
    }

    @Test()
    fun testTransitionException1() {
        val map: Routes = mapOf(
                "S0" to mapOf("E0" to "S1")
        )
        val mm = MooreMachine(map)

        // throws NoRoutesException
        mm.transit("S1", "E0")
    }

    @Test
    fun testTransitionException2() {
        val map: Routes = mapOf(
                "S0" to mapOf("E0" to "S1")
        )
        val mm = MooreMachine(map)

        // throws NoRoutesException
        var thrown = false
        val state1 = mm.transit("S0", "E1")
        if (state1 == null) {
            thrown = true
        }
        assertTrue(thrown)
    }


    @Test
    fun testFind() {
        val map: Routes = mapOf(
                "S0" to mapOf("E0" to "S1")
        )
        assertNotNull(map)
        val mm = MooreMachine(map)

        val s = mm.findState0("E0")
        assertEquals(1, s.size.toLong())
        assertTrue(s.contains("S0"))

    }
}