package com.mabook.xfsm;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

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


	public void testEmitConsumeAll(){
		XFSM.RuleSet ruleSet = new XFSM.RuleSet();
		ruleSet
				.registerState("init", "at home", "at street")
				.registerState("hello", "say hello", "say bye")
				.setInitialStateName("init")
				.registerEvent("go out", "init", "hello", "take a taxi")
				.registerEvent("go home", "hello", "init", "take a bus");


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
		assertEquals("initial action", actions.get(0), "at home");
		actions.clear();

		fsm.emit("go out");
		fsm.consumeAll();
		assertArrayEquals("go out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello"});
		actions.clear();

		fsm.emit("go home");
		fsm.consumeAll();
		assertArrayEquals("go home actions", actions.toArray(), new String[]{"say bye", "take a bus", "at home"});
	}

	public void testEmitLoop() throws InterruptedException {
		XFSM.RuleSet ruleSet = new XFSM.RuleSet();
		ruleSet
				.registerState("init", "at home", "at street")
				.registerState("hello", "say hello", "say bye")
				.setInitialStateName("init")
				.registerEvent("go out", "init", "hello", "take a taxi")
				.registerEvent("go home", "hello", "init", "take a bus");


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
		assertEquals("initial action", actions.get(0), "at home");
		actions.clear();

		fsm.emit("go out");
		Thread.sleep(100);
		assertArrayEquals("go out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello"});
		actions.clear();

		fsm.emit("go home");
		Thread.sleep(100);
		assertArrayEquals("go home actions", actions.toArray(), new String[]{"say bye", "take a bus", "at home"});
	}

	public void testEmitNested() throws InterruptedException {
		XFSM.RuleSet ruleSet = new XFSM.RuleSet();
		ruleSet
				.registerState("init", "at home", "at street")
				.registerState("hello", "say hello", "say bye")
				.setInitialStateName("init")
				.registerEvent("go out", "init", "hello", "take a taxi")
				.registerEvent("go home", "hello", "init", "take a bus");


		final List<String> actions = new ArrayList<>();
		final XFSM fsm = new XFSM(new LinkedBlockingQueue<String>(), ruleSet);
		fsm.setActionListener(new XFSM.ActionListener() {
			@Override
			public void onAction(XFSM context, XFSM.When when, String action) {
				if(context.getCurrentState().name.equals("init") && when == XFSM.When.EXIT){
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
		assertEquals("initial action", actions.get(0), "at home");
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

}