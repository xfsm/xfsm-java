package com.mabook.xfsm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by sng2c on 15. 7. 27..
 */
public class XFSMTest {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testEmitConsumeAll(){
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.event("go out", "init", "hello", "take a taxi")
				.event("go home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();


		final List<String> actions = new ArrayList<>();
		XFSM fsm = new XFSM(new LinkedBlockingQueue<String>(), ruleSet);
		fsm.setActionListener(new XFSM.ActionListener() {
			@Override
			public void onAction(XFSM context, XFSM.When when, String action) {
				actions.add(action);
			}
		});

		fsm.init();
		fsm.consumeAll();
		assertTrue("initial action", actions.get(0).equals("at home"));
		actions.clear();

		fsm.emit("go out");
		fsm.consumeAll();
		assertArrayEquals("go out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello"});
		actions.clear();

		fsm.emit("go home");
		fsm.consumeAll();
		assertArrayEquals("go home actions", actions.toArray(), new String[]{"say bye", "take a bus", "at home"});
	}

	@Test
	public void testEmitLoop() throws InterruptedException {
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.event("go out", "init", "hello", "take a taxi")
				.event("go home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();

		final List<String> actions = new ArrayList<>();
		final XFSM fsm = new XFSM(new LinkedBlockingQueue<String>(), ruleSet);
		fsm.setActionListener(new XFSM.ActionListener() {
			@Override
			public void onAction(XFSM context, XFSM.When when, String action) {
				actions.add(action);
			}
		});

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					fsm.loop();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		fsm.init();
		Thread.sleep(100);

		assertTrue("initial action", actions.get(0).equals("at home"));
		actions.clear();

		fsm.emit("go out");
		Thread.sleep(100);
		assertArrayEquals("go out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello"});
		actions.clear();

		fsm.emit("go home");
		Thread.sleep(100);
		assertArrayEquals("go home actions", actions.toArray(), new String[]{"say bye", "take a bus", "at home"});
	}

	@Test
	public void testEmitNested() throws InterruptedException {
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.event("go out", "init", "hello", "take a taxi")
				.event("go home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();


		final List<String> actions = new ArrayList<>();
		final XFSM fsm = new XFSM(new LinkedBlockingQueue<String>(), ruleSet);
		fsm.setActionListener(new XFSM.ActionListener() {
			@Override
			public void onAction(XFSM context, XFSM.When when, String action) {
				if (context.getCurrentState().name.equals("init") && when == XFSM.When.EXIT) {
					context.emit("go home");
				}
				actions.add(action);
			}
		});

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					fsm.loop();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		fsm.init();
		Thread.sleep(100);

		assertTrue("initial action", actions.get(0).equals("at home"));
		actions.clear();

		fsm.emit("go out");
		Thread.sleep(100);
		assertArrayEquals("go out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello", "say bye", "take a bus", "at home"});
		actions.clear();

		fsm.emit("go home");
		Thread.sleep(100);
		assertArrayEquals("go home actions", actions.toArray(), new String[]{});

		fsm.emit("go out");
		Thread.sleep(100);
		assertArrayEquals("go out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello", "say bye", "take a bus", "at home"});
		actions.clear();
	}

	@Test(expected = XFSM.StateNotFoundException.class)
	public void testNotDefinedState(){
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.event("go out", "init2", "hello", "take a taxi")
				.event("go home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();
	}

	@Test(expected = RuleSet.InitialStateNotSetException.class)
	public void testNoInitialState(){
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.event("go out", "init", "hello", "take a taxi")
				.event("go home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();
	}

}