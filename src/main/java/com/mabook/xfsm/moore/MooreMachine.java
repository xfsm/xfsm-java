package com.mabook.xfsm.moore;

import java.util.HashMap;
import java.util.Map;

public class MooreMachine {
    public static class NoRoutesException extends Exception{
        public NoRoutesException(String state, String event) {
            super(String.format("No routes for '%s' on '%s'", event, state));
        }
    }
    private final Map<String, Map<String, String>> routes;

    public MooreMachine(Map<String, Map<String, String>> routes) {
        this.routes = routes;
    }

    public String transit(String state0, String event) throws NoRoutesException {
        if( routes == null ) throw new NoRoutesException(state0, event);

        Map<String, String> transitionMap = routes.get(state0);
        if( transitionMap == null ) throw new NoRoutesException(state0, event);

        String newState = transitionMap.get(event);
        if( newState == null ) throw new NoRoutesException(state0, event);

        return newState;
    }

    public static <K,V> Map<K,V> MAP(Object... kv){
        HashMap<K, V> map = new HashMap<>();
        for(int i=0; i<kv.length; i+=2 ){
            map.put((K) kv[i], (V) kv[i + 1]);
        }
        return map;
    }
}
