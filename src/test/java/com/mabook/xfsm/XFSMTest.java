package com.mabook.xfsm;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by sng2c on 15. 7. 27..
 */
public class XFSMTest extends TestCase {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testRun(){

		RuleSet ruleSet = new RuleSet();
		ruleSet
				.registerState("init", "at home", "at street")
				.registerState("hello", "say hello", "say bye")
				.setInitialStateName("init")
				.registerEvent("go out", "init", "hello", "take a taxi")
				.registerEvent("go home", "hello", "init", "take a bus");


		XFSM fsm = new XFSM(ruleSet);
		String initAction = fsm.init();
		assertEquals("initial action", initAction, "at home");

		List<String> actions;

		actions = fsm.run("go out");
		assertArrayEquals("go out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello"});

		actions = fsm.run("go home");
		assertArrayEquals("go home actions", actions.toArray(), new String[]{"say bye", "take a bus", "at home"});
	}

	public void testEmit(){
		RuleSet ruleSet = new RuleSet();
		ruleSet
				.registerState("init", "at home", "at street")
				.registerState("hello", "say hello", "say bye")
				.setInitialStateName("init")
				.registerEvent("go out", "init", "hello", "take a taxi")
				.registerEvent("go home", "hello", "init", "take a bus");


		final List<String> actions = new ArrayList<>();
		XFSM fsm = new XFSM(ruleSet);
		fsm.setActionListener(new XFSM.ActionListener() {
			@Override
			public void onAction(XFSM context, XFSM.When when, String action) {
				actions.add(action);
			}
		});
		fsm.init();
		assertEquals("initial action", actions.get(0), "at home");
		actions.clear();

		fsm.emit("go out");
		assertArrayEquals("go out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello"});
		actions.clear();

		fsm.emit("go home");
		assertArrayEquals("go home actions", actions.toArray(), new String[]{"say bye", "take a bus", "at home"});
	}
}