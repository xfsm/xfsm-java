package com.mabook.xfsm;


import java.util.concurrent.BlockingQueue;

public class XFSM {
	public enum When {ENTER, TRANSITION, EXIT}

	public interface ActionListener {
		void onAction(XFSM context, When when, String action);
	}

	public static class StateNotFoundException extends RuntimeException {
		public StateNotFoundException(String stateName) {
			super(String.format("'%s' is not defined.", stateName));
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
		public String fromStateName;
		public String toStateName;
		public String onTransitAction;

		public Transition(String event, String fromStateName, String toStateName, String onTransitAction) {
			this.event = event;
			this.fromStateName = fromStateName;
			this.toStateName = toStateName;
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

	public void init() {
		emit(ruleSet.initEvent);
	}

	public void emit(String event) {
		eventQueue.offer(event);
	}

	private void consume(String event) {
		State currentState = ruleSet.getState(currentStateName);

		Transition transition = ruleSet.getTransition(currentState, event);
		if (transition != null) {
			// EXIT
			if (actionListener != null) {
				if (currentState != null && currentState.onExitAction != null) {
					actionListener.onAction(this, When.EXIT, currentState.onExitAction);
				}
				if (transition.onTransitAction != null) {
					actionListener.onAction(this, When.TRANSITION, transition.onTransitAction);
				}
			}

			currentState = ruleSet.getState(transition.toStateName);
			currentStateName = currentState.name;

			// ENTER
			if (actionListener != null) {
				if (currentState.onEnterAction != null) {
					if (actionListener != null) {
						actionListener.onAction(this, When.ENTER, currentState.onEnterAction);
					}
				}
			}
		}
	}

	public void consumeOnce() {
		String event = eventQueue.poll();
		if (event != null) {
			consume(event);
		}
	}

	public void consumeAll() {
		while (true) {
			String event = eventQueue.poll();
			if (event != null) {
				consume(event);
			} else {
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
