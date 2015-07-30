package com.mabook.xfsm;

import com.mabook.java.runtimelexer.Lexer;
import com.mabook.java.runtimelexer.MatchResult;
import com.mabook.java.runtimelexer.Rule;

/**
 * Created by sng2c on 15. 7. 30..
 */
public class PlantUmlParser {
	public RuleSet parse(String uml) {


		com.mabook.java.runtimelexer.RuleSet lexRule = new com.mabook.java.runtimelexer.RuleSet();
		lexRule.appendRule(new Rule("START", "@startuml\\n"));
		lexRule.appendRule(new Rule("END", "@enduml\\n"));
		lexRule.appendRule(new Rule("STATE", "State (\\S+?)\\n"));
		lexRule.appendRule(new Rule("STATE_IN", "(\\S+) : in '(.+?)'\\n"));
		lexRule.appendRule(new Rule("STATE_OUT", "(\\S+) : out '(.+?)'\\n"));
		lexRule.appendRule(new Rule("INIT_STATE", "\\[\\*\\] --> (.+?) : event '__init__'\n"));
		lexRule.appendRule(new Rule("EVENT_DO", "(\\S+?) --> (\\S+?) : event '(\\S+?)' do '(.+?)'\n"));
		lexRule.appendRule(new Rule("EVENT", "(\\S+?) --> (\\S+?) : event '(\\S+?)'\n"));


		final RuleSet.Builder builder = new RuleSet.Builder();

		Rule.OnMatchListener listener = new Rule.OnMatchListener() {
			@Override
			public MatchResult onMatch(Lexer lexer, MatchResult matchResult) {
				String ruleName = matchResult.getRule().getName();

				if ("STATE".equals(ruleName)) {
					String stateName = matchResult.getTokenMatchers().get(0).getMatcher().group(1);
					builder.state(stateName, null, null);
				} else if ("STATE_IN".equals(ruleName)) {
					String stateName = matchResult.getTokenMatchers().get(0).getMatcher().group(1);
					String action = matchResult.getTokenMatchers().get(0).getMatcher().group(2);
					RuleSet.State state = builder.getState(stateName);
					state.onEnter = action;
				} else if ("STATE_OUT".equals(ruleName)) {
					String stateName = matchResult.getTokenMatchers().get(0).getMatcher().group(1);
					String action = matchResult.getTokenMatchers().get(0).getMatcher().group(2);
					RuleSet.State state = builder.getState(stateName);
					state.onExit = action;
				} else if ("INIT_STATE".equals(ruleName)) {
					String stateName = matchResult.getTokenMatchers().get(0).getMatcher().group(1);
					builder.initialState(stateName);
				} else if ("EVENT".equals(ruleName)) {
					String fromStateName = matchResult.getTokenMatchers().get(0).getMatcher().group(1);
					String toStateName = matchResult.getTokenMatchers().get(0).getMatcher().group(2);
					String event = matchResult.getTokenMatchers().get(0).getMatcher().group(3);
					builder.transition(event, fromStateName, toStateName, null);
				} else if ("EVENT".equals(ruleName)) {
					String fromStateName = matchResult.getTokenMatchers().get(0).getMatcher().group(1);
					String toStateName = matchResult.getTokenMatchers().get(0).getMatcher().group(2);
					String event = matchResult.getTokenMatchers().get(0).getMatcher().group(3);
					String action = matchResult.getTokenMatchers().get(0).getMatcher().group(4);
					builder.transition(event, fromStateName, toStateName, action);
				}

				return matchResult;
			}
		};

		Lexer lexer = new Lexer(lexRule, listener);

		lexer.lex(uml);

		return builder.build();
	}
}
