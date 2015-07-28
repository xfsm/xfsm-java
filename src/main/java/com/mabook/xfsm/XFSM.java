package com.mabook.xfsm;


import java.util.*;

/**
 * Created by sng2c on 15. 7. 27..
 */
public class XFSM {

	public interface ActionListener{
		void onAction(XFSM context, String action);
	}

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



	State currentState;
	ActionListener actionListener;

	final State initialState;
	final HashMap<State, HashMap<String, Transition>> stateMap;

	public XFSM(State initialState, List<Transition> stateList, ActionListener actionListener){
		this.initialState = initialState;

		stateMap = new HashMap<>();
		for ( Transition transition : stateList ){
			if( !stateMap.containsKey(transition.fromState) ){
				stateMap.put(transition.fromState, new HashMap<String, Transition>());
			}
			stateMap.get(transition.fromState).put(transition.event, transition);
		}

		this.actionListener = actionListener;
	}

	public String init(){
		this.currentState = initialState;

		if (currentState.onEnterAction != null) {
			if (actionListener != null) {
				actionListener.onAction(this, currentState.onEnterAction);
			}
		}

		return currentState.onEnterAction;
	}

	public void emit(String event){
		if( currentState != null ) {
			HashMap<String, Transition> eventMap = stateMap.get(currentState);
			if( eventMap != null ) {
				Transition transition = eventMap.get(event);
				if (transition != null) {
					if (actionListener != null) {
						if (currentState.onExitAction != null) {
							actionListener.onAction(this, currentState.onExitAction);
						}

						if (transition.onTransitAction != null) {
							actionListener.onAction(this, transition.onTransitAction);
						}
					}

					currentState = transition.toState;

					if (actionListener != null) {
						if (currentState.onEnterAction != null) {
							actionListener.onAction(this, currentState.onEnterAction);
						}
					}
				}
			}
		}
	}

	public List<String> run(String event){
		ArrayList<String> actions = new ArrayList<>();
		if( currentState != null ) {
			HashMap<String, Transition> eventMap = stateMap.get(currentState);
			if( eventMap != null ) {
				Transition transition = eventMap.get(event);
				if (transition != null) {
					if (currentState.onExitAction != null)
						actions.add(currentState.onExitAction);

					if (transition.onTransitAction != null)
						actions.add(transition.onTransitAction);

					currentState = transition.toState;

					if (currentState.onEnterAction != null)
						actions.add(currentState.onEnterAction);
				}
			}
		}
		return actions;
	}
}
