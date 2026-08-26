package com.example.domain.emergency

import com.example.domain.model.EmergencyState
import com.example.domain.model.EmergencyStateMachine
import com.example.domain.model.StateTransitionResult
import org.junit.Assert.assertEquals
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
        val result = stateMachine.transitionTo(EmergencyState.POSSIBLE_INCIDENT)
        assertTrue(result is StateTransitionResult.Success)
        assertEquals(EmergencyState.POSSIBLE_INCIDENT, stateMachine.currentState)
    }

    @Test
    fun testValidEscalationWorkflow() {
        assertTrue(stateMachine.transitionTo(EmergencyState.POSSIBLE_INCIDENT) is StateTransitionResult.Success)
        assertTrue(stateMachine.transitionTo(EmergencyState.CONFIRMATION_REQUIRED) is StateTransitionResult.Success)
        assertTrue(stateMachine.transitionTo(EmergencyState.EMERGENCY_ACTIVE) is StateTransitionResult.Success)
        assertTrue(stateMachine.transitionTo(EmergencyState.HELP_REQUESTED) is StateTransitionResult.Success)
        assertTrue(stateMachine.transitionTo(EmergencyState.RESPONDER_FOUND) is StateTransitionResult.Success)
        assertTrue(stateMachine.transitionTo(EmergencyState.RESOLVED) is StateTransitionResult.Success)
        assertTrue(stateMachine.transitionTo(EmergencyState.NORMAL) is StateTransitionResult.Success)
        assertEquals(EmergencyState.NORMAL, stateMachine.currentState)
    }

    @Test
    fun testCancellationWorkflow() {
        assertTrue(stateMachine.transitionTo(EmergencyState.POSSIBLE_INCIDENT) is StateTransitionResult.Success)
        assertTrue(stateMachine.transitionTo(EmergencyState.CONFIRMATION_REQUIRED) is StateTransitionResult.Success)
        assertTrue(stateMachine.transitionTo(EmergencyState.CANCELLED) is StateTransitionResult.Success)
        assertTrue(stateMachine.transitionTo(EmergencyState.NORMAL) is StateTransitionResult.Success)
        assertEquals(EmergencyState.NORMAL, stateMachine.currentState)
    }

    @Test
    fun testInvalidTransition() {
        // Cannot jump directly from NORMAL to RESOLVED without incident
        val result = stateMachine.transitionTo(EmergencyState.RESOLVED)
        assertTrue(result is StateTransitionResult.InvalidTransition)
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
