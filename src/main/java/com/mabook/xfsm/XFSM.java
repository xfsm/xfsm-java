package com.mabook.xfsm;


import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class XFSM {
	public enum When {ENTER, TRANSITION, EXIT}

	public interface ActionListener {
		void onAction(XFSM context, When when, String action);
	}

	public static class RuleSet {
		HashMap<String, State> stateRegistry = new HashMap<>();
		HashMap<String, Transition> eventRegistry = new HashMap<>();
		final String initEvent;

		public RuleSet(){
			initEvent = "__init__"+ this.hashCode();
		}

		public RuleSet registerState(String stateName, String onEnterAction, String onExitAction) {
			stateRegistry.put(stateName, new State(stateName, onEnterAction, onExitAction));
			return this;
		}

		public RuleSet registerEvent(String event, String fromStateName, String toStateName, String action) {
			State from = stateRegistry.get(fromStateName);
			State to = stateRegistry.get(toStateName);
			eventRegistry.put(event + "@" + fromStateName, new Transition(event, from, to, action));
			return this;
		}

		public RuleSet setInitialStateName(String initialStateName) {
			State to = stateRegistry.get(initialStateName);
			eventRegistry.put(initEvent, new Transition(initEvent, null, to, null));
			return this;
		}

		public Transition getTransition(State state, String event) {
			if( state == null ){
				return eventRegistry.get(initEvent);
			}
			return eventRegistry.get(event + "@" + state.name);
		}

		public State getState(String stateName){
			return stateRegistry.get(stateName);
		}
	}

	public static class State {
		public String name;
		public String onEnterAction;
		public String onExitAction;

		public State(String name, String onEnterAction, String onExitAction) {
			this.name = name;
			this.onEnterAction = onEnterAction;
			this.onExitAction = onExitAction;
		}
	}

	public static class Transition {
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


	String currentStateName;
	ActionListener actionListener;
	final BlockingQueue<String> eventQueue;
	final RuleSet ruleSet;

	public XFSM(BlockingQueue<String> eventQueue, RuleSet ruleSet) {
		this(eventQueue, ruleSet, null);
	}

	public XFSM(BlockingQueue<String> eventQueue, RuleSet ruleSet, ActionListener actionListener) {
		this.eventQueue = eventQueue;
		this.ruleSet = ruleSet;
		this.actionListener = actionListener;
	}

	public State getCurrentState() {
		return ruleSet.getState(currentStateName);
	}

	public ActionListener getActionListener() {
		return actionListener;
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	public void init(){
		emit(ruleSet.initEvent);
	}

	public void emit(String event) {
		eventQueue.offer(event);
	}

	private void consume(String event){
		State currentState = ruleSet.getState(currentStateName);

		Transition transition = ruleSet.getTransition(currentState, event);
		if (transition != null) {
			if( currentState != null ) {
				if (currentState.onExitAction != null) {
					if (actionListener != null) {
						actionListener.onAction(this, When.EXIT, currentState.onExitAction);
					}
				}
				if (transition.onTransitAction != null) {
					if (actionListener != null) {
						actionListener.onAction(this, When.TRANSITION, transition.onTransitAction);
					}
				}
			}

			currentState = transition.toState;
			currentStateName = currentState.name;

			if (currentState.onEnterAction != null) {
				if (actionListener != null) {
					actionListener.onAction(this, When.ENTER, currentState.onEnterAction);
				}
			}
		}
	}

	public void consumeOnce(){
		String event = eventQueue.poll();
		if( event != null ){
			consume(event);
		}
	}

	public void consumeAll(){
		while(true) {
			String event = eventQueue.poll();
			if (event != null) {
				consume(event);
			}
			else{
				break;
			}
		}
	}

	public void loop() throws InterruptedException {
		String event;
		while (true) {
			event = eventQueue.take();
			consume(event);
		}
	}

}
