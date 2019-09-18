package com.mabook.xfsm.moore

typealias State = String
typealias Event = String
typealias Transitions = Map<Event, State>
typealias Routes = Map<State, Transitions>

class MooreMachine(private val routes: Routes) {

    fun transit(state0: State, event: Event): State? = routes[state0]?.get(event)

    fun findState0(event: Event): List<State> = routes.entries
            .filter { (_, transitions) -> event in transitions.keys }
            .map { (state0) -> state0 }

}

fun main() {
    val sm = MooreMachine(
            mapOf(
                    "S0" to mapOf(
                            "E0" to "S1"
                    )
            ))

    println(sm.transit("S0", "E0"))

    println(sm.transit("S0", "E1"))

    println(sm.transit("S1", "E1"))

    println(sm.findState0("E0"))
}