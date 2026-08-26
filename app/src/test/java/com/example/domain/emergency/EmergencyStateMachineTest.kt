package com.example.domain.emergency

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EmergencyStateMachineTest {

    private lateinit var stateMachine: EmergencyStateMachine

    @Before
    fun setUp() {
        stateMachine = EmergencyStateMachine(EmergencyState.NORMAL)
    }

    @Test
    fun testValidTransitionFromNormalToPossibleIncident() {
        val success = stateMachine.transitionTo(EmergencyState.POSSIBLE_INCIDENT)
        assertTrue(success)
        assertEquals(EmergencyState.POSSIBLE_INCIDENT, stateMachine.currentState)
    }

    @Test
    fun testValidEscalationWorkflow() {
        assertTrue(stateMachine.transitionTo(EmergencyState.POSSIBLE_INCIDENT))
        assertTrue(stateMachine.transitionTo(EmergencyState.CONFIRMATION_REQUIRED))
        assertTrue(stateMachine.transitionTo(EmergencyState.EMERGENCY_ACTIVE))
        assertTrue(stateMachine.transitionTo(EmergencyState.HELP_REQUESTED))
        assertTrue(stateMachine.transitionTo(EmergencyState.RESPONDER_FOUND))
        assertTrue(stateMachine.transitionTo(EmergencyState.RESOLVED))
        assertTrue(stateMachine.transitionTo(EmergencyState.NORMAL))
        assertEquals(EmergencyState.NORMAL, stateMachine.currentState)
    }

    @Test
    fun testCancellationWorkflow() {
        assertTrue(stateMachine.transitionTo(EmergencyState.POSSIBLE_INCIDENT))
        assertTrue(stateMachine.transitionTo(EmergencyState.CONFIRMATION_REQUIRED))
        assertTrue(stateMachine.transitionTo(EmergencyState.CANCELLED))
        assertTrue(stateMachine.transitionTo(EmergencyState.NORMAL))
        assertEquals(EmergencyState.NORMAL, stateMachine.currentState)
    }

    @Test
    fun testInvalidTransition() {
        // Cannot jump directly from NORMAL to RESOLVED without incident
        val success = stateMachine.transitionTo(EmergencyState.RESOLVED)
        assertFalse(success)
        assertEquals(EmergencyState.NORMAL, stateMachine.currentState)
    }

    @Test
    fun testListenerNotification() {
        var observedOld: EmergencyState? = null
        var observedNew: EmergencyState? = null

        stateMachine.addListener { old, new ->
            observedOld = old
            observedNew = new
        }

        stateMachine.transitionTo(EmergencyState.POSSIBLE_INCIDENT)
        assertEquals(EmergencyState.NORMAL, observedOld)
        assertEquals(EmergencyState.POSSIBLE_INCIDENT, observedNew)
    }
}
