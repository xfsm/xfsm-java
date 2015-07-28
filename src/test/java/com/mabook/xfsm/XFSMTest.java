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

		XFSM.State init = new XFSM.State("init", "woke up", "go out");
		XFSM.State hello = new XFSM.State("init", "say hello", "say bye");

		ArrayList<XFSM.Transition> transitions = new ArrayList<>();
		transitions.add(new XFSM.Transition("hi",init,hello,"meet somebody"));

		XFSM fsm = new XFSM(init, transitions, null);
		System.out.println(fsm.init());
		List<String> actions = fsm.run("hi");
		System.out.println(actions.toString());
	}
}