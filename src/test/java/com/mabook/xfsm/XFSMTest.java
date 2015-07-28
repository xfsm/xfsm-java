package com.mabook.xfsm;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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
	public void testNew(){
		XFSM.State init = new XFSM.State("init", "at home", "at street");
		XFSM.State hello = new XFSM.State("hello", "say hello", "say bye");

		ArrayList<XFSM.Transition> transitions = new ArrayList<>();
		transitions.add(new XFSM.Transition("go out", init, hello, "take a taxi"));
		transitions.add(new XFSM.Transition("go home", hello, init, "take a bus"));

		XFSM fsm = new XFSM(init, transitions, null);
		String initAction = fsm.init();
		assertEquals("initial action", initAction, "at home");

		List<String> actions;

		actions = fsm.run("go out");
		assertArrayEquals("go out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello"});

		actions = fsm.run("go home");
		assertArrayEquals("go home actions", actions.toArray(), new String[]{"say bye", "take a bus", "at home"});
	}
}