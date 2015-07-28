package com.mabook.xfsm;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by sng2c on 15. 7. 28..
 */
public class RuleSet {

	public static class InitialStateNotSetException extends RuntimeException {
		public InitialStateNotSetException() {
			super(String.format("An initial state is not set."));
		}
	}

	public static RuleSet fromJson(String json){
		Gson gson = new Gson();
		return gson.fromJson(json, RuleSet.class);
	}

	public static class Builder {
		RuleSet ruleSet = new RuleSet();
		boolean initialStateSet = false;

		public Builder state(String stateName, String onEnterAction, String onExitAction) {
			ruleSet.registerState(stateName, onEnterAction, onExitAction);
			return this;
		}

		public Builder transition(String event, String fromStateName, String toStateName, String action) {
			ruleSet.registerTransition(event, fromStateName, toStateName, action);
			return this;
		}

		public Builder initialState(String stateName) {
			initialStateSet = true;
			ruleSet.setInitialStateName(stateName);
			return this;
		}

		public RuleSet build() {
			if (!initialStateSet) {
				throw new InitialStateNotSetException();
			}
			return ruleSet;
		}
	}

	HashMap<String, XFSM.State> states = new HashMap<>();
	HashMap<String, XFSM.Transition> transitions = new HashMap<>();
	final String initEvent;

	private RuleSet() {
		initEvent = "__init__";
	}

	private void registerState(String stateName, String onEnterAction, String onExitAction) {
		states.put(stateName, new XFSM.State(stateName, onEnterAction, onExitAction));
	}

	private void registerTransition(String event, String fromStateName, String toStateName, String action) {
		XFSM.State from = states.get(fromStateName);
		XFSM.State to = states.get(toStateName);
		if (from == null) {
			throw new XFSM.StateNotFoundException(fromStateName);
		}
		if (to == null) {
			throw new XFSM.StateNotFoundException(toStateName);
		}
		transitions.put(event + "@" + fromStateName, new XFSM.Transition(event, fromStateName, toStateName, action));
	}

	private void setInitialStateName(String initialStateName) {
		XFSM.State to = states.get(initialStateName);
		if (to == null) {
			throw new XFSM.StateNotFoundException(initialStateName);
		}
		transitions.put(initEvent, new XFSM.Transition(initEvent, null, initialStateName, null));
	}

	public XFSM.Transition getTransition(XFSM.State state, String event) {
		XFSM.Transition found;
		if (state == null) {
			found = transitions.get(initEvent);
		} else {
			found = transitions.get(event + "@" + state.name);
		}
		return found;
	}

	public XFSM.State getState(String stateName) {
		return states.get(stateName);
	}
}
