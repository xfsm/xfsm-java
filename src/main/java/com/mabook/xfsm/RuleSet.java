package com.mabook.xfsm;

import java.util.HashMap;

/**
 * Created by sng2c on 15. 7. 28..
 */
public class RuleSet {

	public static class State{
		public String name;
		public String onEnterAction;
		public String onExitAction;

		public State(String name, String onEnterAction, String onExitAction) {
			this.name = name;
			this.onEnterAction = onEnterAction;
			this.onExitAction = onExitAction;
		}
	}

	public static class Transition{
		public String event;
		public State fromState;
		public State toState;
		public String onTransitAction;

		public Transition(String event, State fromState, State toState, String onTransitAction) {
			this.event = event;
			this.fromState = fromState;
			this.toState = toState;
			this.onTransitAction = onTransitAction;
		}
	}

	HashMap<String, State> stateRegistry = new HashMap<>();
	HashMap<String, Transition> eventRegistry = new HashMap<>();
	String initialStateName;
	public RuleSet(){

	}

	public RuleSet registerState(String stateName, String onEnterAction, String onExitAction){
		stateRegistry.put(stateName, new State(stateName, onEnterAction, onExitAction));
		return this;
	}

	public RuleSet registerEvent(String event, String fromStateName, String toStateName, String action){
		State from = stateRegistry.get(fromStateName);
		State to = stateRegistry.get(toStateName);
		eventRegistry.put(event+"@"+fromStateName, new Transition(event, from, to, action));
		return this;
	}

	public State getInitialState(){
		return stateRegistry.get(initialStateName);
	}

	public RuleSet setInitialStateName(String initialStateName) {
		this.initialStateName = initialStateName;
		return this;
	}

	public Transition getTransition(State state, String event){
		return eventRegistry.get(event+"@"+state.name);
	}
}
