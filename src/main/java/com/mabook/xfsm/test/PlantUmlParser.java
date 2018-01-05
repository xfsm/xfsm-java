package com.mabook.xfsm.test;

import com.mabook.java.runtimelexer.Lexer;
import com.mabook.java.runtimelexer.MatchResult;
import com.mabook.java.runtimelexer.Rule;

/**
 * Created by sng2c on 15. 7. 30..
 */
public class PlantUmlParser {
	public RuleSet parse(String uml) {


		com.mabook.java.runtimelexer.RuleSet lexRule = new com.mabook.java.runtimelexer.RuleSet();
		lexRule.appendRule(new Rule("STATE", "State\\s+(\\S+?)\\n"));
		lexRule.appendRule(new Rule("STATE_IN", "(\\S+)\\s*:\\s*in\\s+'(.+?)'\\n"));
		lexRule.appendRule(new Rule("STATE_OUT", "(\\S+)\\s*:\\s*out\\s+'(.+?)'\\n"));
		lexRule.appendRule(new Rule("INIT_STATE", "\\[\\*\\]\\s*-->\\s*(.+?)\\s*:\\s*event\\s+'__init__'\\n"));
		lexRule.appendRule(new Rule("EVENT_DO", "(\\S+?)\\s*-->\\s*(\\S+?)\\s*:\\s*event\\s+'(\\S+?)'\\s+do\\s+'(\\S+?)'\\n"));
		lexRule.appendRule(new Rule("EVENT", "(\\S+?)\\s*-->\\s*(\\S+?)\\s*:\\s*event\\s+'(\\S+?)'\\n"));


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
				} else if ("EVENT_DO".equals(ruleName)) {
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
