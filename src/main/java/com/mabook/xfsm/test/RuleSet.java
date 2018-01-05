package com.mabook.xfsm.test;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sng2c on 15. 7. 28..
 */
public class RuleSet {

	static Pattern pattern = Pattern.compile("\\s");
	final String initialEvent;
	HashMap<String, State> states = new HashMap<>();
	HashMap<String, Transition> transitions = new HashMap<>();

	RuleSet() {
		initialEvent = "__init__";
	}

	public static RuleSet fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, RuleSet.class);
	}

	static void validateName(String name) {
		if (name != null) {
			Matcher m = pattern.matcher(name);
			if (m.find()) {
				throw new InvalidNameException(name);
			}
		}
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public String toPlantUml() {
		StringBuilder sb = new StringBuilder();
		sb.append("@startuml").append("\n");
		for (Map.Entry<String, State> entry : states.entrySet()) {
			State state = entry.getValue();
			sb.append("State ").append(state.name);
			sb.append("\n");
			if (state.onEnter != null || state.onExit != null) {
				if (state.onEnter != null) {
					sb.append(state.name).append(" : in '").append(state.onEnter).append("'\n");
				}
				if (state.onExit != null) {
					sb.append(state.name).append(" : out '").append(state.onExit).append("'\n");
				}
				sb.append("\n");
			}

		}
		for (Map.Entry<String, Transition> entry : transitions.entrySet()) {
			Transition tr = entry.getValue();
			String from = tr.fromStateName;
			if (from == null) {
				from = "[*]";
			}
			sb.append(from)
					.append(" --> ")
					.append(tr.toStateName)
					.append(" : event '")
					.append(tr.event)
					.append("'");
			if (tr.onTransition != null) {
				sb.append(" do '")
						.append(tr.onTransition)
						.append("'");
			}
			sb.append("\n");
		}
		sb.append("@enduml").append("\n");
		return sb.toString();
	}

	private void registerState(String stateName, String onEnterAction, String onExitAction) {
		states.put(stateName, new State(stateName, onEnterAction, onExitAction));
	}

	private void registerTransition(String event, String fromStateName, String toStateName, String action) {
		State from = states.get(fromStateName);
		State to = states.get(toStateName);
		if (from == null) {
			throw new StateNotFoundException(fromStateName);
		}
		if (to == null) {
			throw new StateNotFoundException(toStateName);
		}
		transitions.put(event + "@" + fromStateName, new Transition(event, fromStateName, toStateName, action));
	}

	private void setInitialStateName(String initialStateName) {
		State to = states.get(initialStateName);
		if (to == null) {
			throw new StateNotFoundException(initialStateName);
		}
		transitions.put(initialEvent, new Transition(initialEvent, null, initialStateName, "INIT_ACTION"));
	}

	public Transition getTransition(State state, String event) {
		Transition found;
		if (state == null) {
			found = transitions.get(initialEvent);
		} else {
			found = transitions.get(event + "@" + state.name);
		}
		return found;
	}

	public State getState(String stateName) {
		return states.get(stateName);
	}

	public static class InitialStateNotSetException extends RuntimeException {
		public InitialStateNotSetException() {
			super(String.format("An initial state is not set."));
		}
	}

	public static class StateNotFoundException extends RuntimeException {
		public StateNotFoundException(String stateName) {
			super(String.format("'%s' is not defined.", stateName));
		}
	}

	public static class InvalidNameException extends RuntimeException {
		public InvalidNameException(String stateName) {
			super(String.format("'%s' is not valid. Spaces are not allowed.", stateName));
		}
	}

	public static class State {
		public String name;
		public String onEnter;
		public String onExit;

		public State(String name, String onEnterAction, String onExitAction) {
			validateName(name);
			this.name = name;
			this.onEnter = onEnterAction;
			this.onExit = onExitAction;
		}
	}

	public static class Transition {
		public String event;
		public String fromStateName;
		public String toStateName;
		public String onTransition;

		public Transition(String event, String fromStateName, String toStateName, String onTransitionAction) {
			validateName(event);
			validateName(fromStateName);
			validateName(toStateName);
			this.event = event;
			this.fromStateName = fromStateName;
			this.toStateName = toStateName;
			this.onTransition = onTransitionAction;
		}
	}

	public static class TransitionBuilder {
		String fromStateName;
		String toStateName;
		String action;
		String event;
		Builder builder;
		boolean isBuilt = false;

		public TransitionBuilder(Builder builder, String fromStateName) {
			this.builder = builder;
			this.fromStateName = fromStateName;
		}

		public TransitionBuilder onEvent(String event, String toStateName) {
			builder.transition(event, fromStateName, toStateName, null);
			return this;
		}

		public TransitionBuilder onEvent(String event, String toStateName, String action) {
			builder.transition(event, fromStateName, toStateName, action);
			return this;
		}

		public TransitionBuilder onState(String fromStateName) {
			return new TransitionBuilder(builder, fromStateName);
		}

		public Builder initialState(String stateName) {
			return builder.initialState(stateName);
		}

		public RuleSet build() {
			return builder.build();
		}
	}

	public static class Builder {
		RuleSet ruleSet = new RuleSet();
		boolean initialStateSet = false;

		public State getState(String stateName) {
			return ruleSet.getState(stateName);
		}

		public Builder state(String stateName, String onEnterAction, String onExitAction) {
			ruleSet.registerState(stateName, onEnterAction, onExitAction);
			return this;
		}

		public Builder transition(String event, String fromStateName, String toStateName, String action) {
			ruleSet.registerTransition(event, fromStateName, toStateName, action);
			return this;
		}

		public TransitionBuilder onState(String fromStateName) {
			return new TransitionBuilder(this, fromStateName);
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
}
