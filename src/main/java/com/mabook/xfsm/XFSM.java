package com.mabook.xfsm;



import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("unused")
public class XFSM {
    public enum When {ENTER, TRANSITION, EXIT}

    public interface ActionListener {
        void onAction(XFSM context, When when, String action);
    }

    private String currentStateName;
    private ActionListener actionListener;
    private BlockingQueue<String> eventQueue;
    private RuleSet ruleSet;

    @SuppressWarnings("WeakerAccess")
    public XFSM(RuleSet ruleSet, ActionListener actionListener) {
        this.eventQueue = new LinkedBlockingQueue<>();
        this.ruleSet = ruleSet;
        this.actionListener = actionListener;
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    public void setEventQueue(BlockingQueue<String> eventQueue) {
        this.eventQueue = eventQueue;
    }

    public BlockingQueue<String> getEventQueue() {
        return eventQueue;
    }

    public String getCurrentStateName() {
        return currentStateName;
    }

    public void setCurrentStateName(String currentStateName) {
        this.currentStateName = currentStateName;
    }

    public RuleSet.State getCurrentState() {
        return ruleSet.getState(currentStateName);
    }

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void init() {
        emit(ruleSet.initialEvent);
    }

    public void emit(String event) {
        eventQueue.offer(event);
    }

    private void consume(String event) {
        RuleSet.State currentState = ruleSet.getState(currentStateName);

        RuleSet.Transition transition = ruleSet.getTransition(currentState, event);
        if (transition != null) {
            // EXIT
            if (currentState != null && currentState.onExit != null) {
                if (actionListener != null) {
                    actionListener.onAction(this, When.EXIT, currentState.onExit);
                }
            }

            // TRANSITION
            if (transition.onTransition != null) {
                if (actionListener != null) {
                    actionListener.onAction(this, When.TRANSITION, transition.onTransition);
                }
            }

            currentState = ruleSet.getState(transition.toStateName);
            currentStateName = currentState.name;

            // ENTER
            if (actionListener != null) {
                if (currentState.onEnter != null) {
                    if (actionListener != null) {
                        actionListener.onAction(this, When.ENTER, currentState.onEnter);
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
