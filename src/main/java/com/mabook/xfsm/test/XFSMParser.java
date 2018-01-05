package com.mabook.xfsm.test;

import com.mabook.java.runtimelexer.Lexer;
import com.mabook.java.runtimelexer.MatchResult;
import com.mabook.java.runtimelexer.Rule;
import com.mabook.java.runtimelexer.Token;

import java.util.ArrayList;

/**
 * Created by sng2c on 15. 7. 30..
 */
public class XFSMParser {
    public RuleSet parse(String xfsmDsl) {


        com.mabook.java.runtimelexer.RuleSet lexRule = new com.mabook.java.runtimelexer.RuleSet();

        final com.mabook.java.runtimelexer.RuleSet.State CTX_STATE = new com.mabook.java.runtimelexer.RuleSet.State("CTX_STATE");
        final com.mabook.java.runtimelexer.RuleSet.State CTX_EVENT = new com.mabook.java.runtimelexer.RuleSet.State("CTX_EVENT");

        lexRule.appendRule(new Rule("BEGIN_STATE", "BEGIN_STATE\\n"));
        lexRule.appendRule(new Rule("BEGIN_EVENT", "BEGIN_EVENT\\n"));

        lexRule.appendRule(CTX_STATE, new Rule("END_STATE", "END_STATE\\n"));
        lexRule.appendRule(CTX_STATE, new Rule("STATE_ITEM", "-\\s+(\\S+)\\n"));

        lexRule.appendRule(CTX_EVENT, new Rule("END_EVENT", "END_STATE\\n"));
        lexRule.appendRule(CTX_EVENT, new Rule("EVENT_ITEM", "-\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\n"));

        final RuleSet.Builder builder = new RuleSet.Builder();


		Rule.OnMatchListener listener = new Rule.OnMatchListener() {
			@Override
			public MatchResult onMatch(Lexer lexer, MatchResult matchResult) {

				String ruleName = matchResult.getRule().getName();
                System.out.println("--> "+ruleName);
				com.mabook.java.runtimelexer.RuleSet.State curState = lexer.getState();
				if( curState == com.mabook.java.runtimelexer.RuleSet.DEFAULT) {
                    switch (ruleName) {
                        case "BEGIN_STATE":
                            lexer.pushState(CTX_STATE);
                            break;
                        case "BEGIN_EVENT":
                            lexer.pushState(CTX_EVENT);
                            break;
                    }
                }

                if( curState == CTX_STATE ){
                    switch (ruleName) {
                        case "STATE_ITEM":
                            String stateName = matchResult.getTokenMatchers().get(0).getMatcher().group(1);
                            builder.state(stateName, stateName+"_IN", stateName+"_OUT");
                            if( !builder.initialStateSet ){
                                builder.initialState(stateName);
                            }
                            break;
                        case "END_STATE":
                            lexer.popState();
                            break;
                    }
                }

                if( curState == CTX_EVENT ){
                    switch (ruleName) {
                        case "EVENT_ITEM":
                            String eventName = matchResult.getTokenMatchers().get(0).getMatcher().group(1);
                            String fromState = matchResult.getTokenMatchers().get(0).getMatcher().group(2);
                            String toState = matchResult.getTokenMatchers().get(0).getMatcher().group(3);
                            builder.transition(eventName, fromState, toState, eventName+"_ACTION");
                            break;
                        case "END_EVENT":
                            lexer.popState();
                            break;
                    }
                }

				return matchResult;
			}
		};

//        lexRule.setUseAutoSkip(false);
        Lexer lexer = new Lexer(lexRule, listener);
        lexer.lex(xfsmDsl);


        return builder.build();
    }
}
