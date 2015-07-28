package com.mabook.xfsm;


import java.util.ArrayList;
import java.util.List;

public class XFSM {
	public enum When {ENTER, EXIT, TRANSITION};
	public interface ActionListener{
		void onAction(XFSM context, When when, String action);
	}

	RuleSet.State currentState;
	ActionListener actionListener;

	final RuleSet ruleSet;

	public XFSM(RuleSet ruleSet){
		this(ruleSet, null);
	}

	public XFSM(RuleSet ruleSet, ActionListener actionListener){
		this.ruleSet = ruleSet;
		this.actionListener = actionListener;
	}

	public ActionListener getActionListener() {
		return actionListener;
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	public String init(){
		this.currentState = ruleSet.getInitialState();

		if (currentState.onEnterAction != null) {
			if (actionListener != null) {
				actionListener.onAction(this, When.ENTER, currentState.onEnterAction);
			}
		}

		return currentState.onEnterAction;
	}

	public void emit(String event){
		if( currentState != null ) {
			RuleSet.Transition transition = ruleSet.getTransition(currentState, event);
			if (transition != null) {
				if (actionListener != null) {
					if (currentState.onExitAction != null) {
						actionListener.onAction(this, When.EXIT, currentState.onExitAction);
					}

					if (transition.onTransitAction != null) {
						actionListener.onAction(this, When.TRANSITION, transition.onTransitAction);
					}
				}

				currentState = transition.toState;

				if (actionListener != null) {
					if (currentState.onEnterAction != null) {
						actionListener.onAction(this, When.ENTER, currentState.onEnterAction);
					}
				}
			}
		}
	}

	public List<String> run(String event){
		ArrayList<String> actions = new ArrayList<>();
		if( currentState != null ) {
			RuleSet.Transition transition = ruleSet.getTransition(currentState, event);
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
		return actions;
	}
}
