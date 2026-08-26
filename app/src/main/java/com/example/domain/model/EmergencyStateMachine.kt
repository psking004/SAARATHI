package com.example.domain.model

sealed class StateTransitionResult {
    data class Success(val oldState: EmergencyState, val newState: EmergencyState) : StateTransitionResult()
    data class InvalidTransition(val currentState: EmergencyState, val attemptedState: EmergencyState, val reason: String) : StateTransitionResult()
}

class EmergencyStateMachine(
    initialState: EmergencyState = EmergencyState.NORMAL
) {
    var currentState: EmergencyState = initialState
        private set

    private val listeners = mutableListOf<(EmergencyState, EmergencyState) -> Unit>()

    fun addListener(listener: (EmergencyState, EmergencyState) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (EmergencyState, EmergencyState) -> Unit) {
        listeners.remove(listener)
    }

    fun transitionTo(nextState: EmergencyState): StateTransitionResult {
        if (currentState == nextState) {
            return StateTransitionResult.Success(currentState, nextState)
        }

        if (!currentState.canTransitionTo(nextState)) {
            return StateTransitionResult.InvalidTransition(
                currentState = currentState,
                attemptedState = nextState,
                reason = "Cannot transition directly from $currentState to $nextState in Golden Hour protocol."
            )
        }

        val previous = currentState
        currentState = nextState
        listeners.forEach { it(previous, nextState) }
        return StateTransitionResult.Success(previous, nextState)
    }

    fun reset() {
        val previous = currentState
        currentState = EmergencyState.NORMAL
        listeners.forEach { it(previous, EmergencyState.NORMAL) }
    }
}
