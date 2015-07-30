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
				.transition("go_out", "init", "hello", "take a taxi")
				.transition("go_home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();


		final List<String> actions = new ArrayList<>();
		XFSM fsm = new XFSM(ruleSet);
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

		fsm.emit("go_out");
		fsm.consumeAll();
		assertArrayEquals("go_out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello"});
		actions.clear();

		fsm.emit("go_home");
		fsm.consumeAll();
		assertArrayEquals("go_home actions", actions.toArray(), new String[]{"say bye", "take a bus", "at home"});
	}

	@Test
	public void testEmitLoop() throws InterruptedException {
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.transition("go_out", "init", "hello", "take a taxi")
				.transition("go_home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();

		final List<String> actions = new ArrayList<>();
		final XFSM fsm = new XFSM(ruleSet);
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

		fsm.emit("go_out");
		Thread.sleep(100);
		assertArrayEquals("go_out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello"});
		actions.clear();

		fsm.emit("go_home");
		Thread.sleep(100);
		assertArrayEquals("go_home actions", actions.toArray(), new String[]{"say bye", "take a bus", "at home"});
	}

	@Test
	public void testEmitNested() throws InterruptedException {
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.transition("go_out", "init", "hello", "take a taxi")
				.transition("go_home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();


		final List<String> actions = new ArrayList<>();
		final XFSM fsm = new XFSM(ruleSet);
		fsm.setActionListener(new XFSM.ActionListener() {
			@Override
			public void onAction(XFSM context, XFSM.When when, String action) {
				if (context.getCurrentState().name.equals("init") && when == XFSM.When.EXIT) {
					context.emit("go_home");
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

		fsm.emit("go_out");
		Thread.sleep(100);
		assertArrayEquals("go_out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello", "say bye", "take a bus", "at home"});
		actions.clear();

		fsm.emit("go_home");
		Thread.sleep(100);
		assertArrayEquals("go_home actions", actions.toArray(), new String[]{});

		fsm.emit("go_out");
		Thread.sleep(100);
		assertArrayEquals("go_out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello", "say bye", "take a bus", "at home"});
		actions.clear();
	}

	@Test(expected = RuleSet.StateNotFoundException.class)
	public void testNotDefinedState(){
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.transition("go_out", "init2", "hello", "take a taxi")
				.transition("go_home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();
	}

	@Test(expected = RuleSet.InitialStateNotSetException.class)
	public void testNoInitialState(){
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.transition("go_out", "init", "hello", "take a taxi")
				.transition("go_home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();
	}

	@Test
	public void testFromJSON() throws InterruptedException {
		String json = "{\n" +
				"    \"states\": {\n" +
				"        \"hello\": {\n" +
				"            \"name\": \"hello\",\n" +
				"            \"onEnter\": \"say hello\",\n" +
				"            \"onExit\": \"say bye\"\n" +
				"        },\n" +
				"        \"init\": {\n" +
				"            \"name\": \"init\",\n" +
				"            \"onEnter\": \"at home\",\n" +
				"            \"onExit\": \"at street\"\n" +
				"        }\n" +
				"    },\n" +
				"    \"transitions\": {\n" +
				"        \"__init__\": {\n" +
				"            \"event\": \"__init__\",\n" +
				"            \"toStateName\": \"init\"\n" +
				"        },\n" +
				"        \"go_home@hello\": {\n" +
				"            \"event\": \"go_home\",\n" +
				"            \"fromStateName\": \"hello\",\n" +
				"            \"toStateName\": \"init\",\n" +
				"            \"onTransition\": \"take a bus\"\n" +
				"        },\n" +
				"        \"go_out@init\": {\n" +
				"            \"event\": \"go_out\",\n" +
				"            \"fromStateName\": \"init\",\n" +
				"            \"toStateName\": \"hello\",\n" +
				"            \"onTransition\": \"take a taxi\"\n" +
				"        }\n" +
				"    },\n" +
				"    \"initialEvent\": \"__init__\"\n" +
				"}";

		System.out.println(json);
		RuleSet ruleSet2 = RuleSet.fromJson(json);
		final List<String> actions = new ArrayList<>();
		final XFSM fsm = new XFSM(ruleSet2);
		fsm.setActionListener(new XFSM.ActionListener() {
			@Override
			public void onAction(XFSM context, XFSM.When when, String action) {
				if (context.getCurrentState().name.equals("init") && when == XFSM.When.EXIT) {
					context.emit("go_home");
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

		fsm.emit("go_out");
		Thread.sleep(100);
		assertArrayEquals("go_out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello", "say bye", "take a bus", "at home"});
		actions.clear();

		fsm.emit("go_home");
		Thread.sleep(100);
		assertArrayEquals("go_home actions", actions.toArray(), new String[]{});

		fsm.emit("go_out");
		Thread.sleep(100);
		assertArrayEquals("go_out actions", actions.toArray(), new String[]{"at street", "take a taxi", "say hello", "say bye", "take a bus", "at home"});
		actions.clear();
	}


	@Test
	public void testBuilder() {
		RuleSet.Builder rb = new RuleSet.Builder();
		rb
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.transition("go_out", "init", "hello", "take a taxi")
				.transition("go_home", "hello", "init", "take a bus");
		RuleSet ruleSet = rb.build();

		RuleSet.Builder rb2 = new RuleSet.Builder();
		rb2
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.onState("init")
				.onEvent("go_out", "hello", "take a taxi")
				.onState("hello")
				.onEvent("go_home", "init", "take a bus");
		RuleSet ruleSet2 = rb2.build();

		System.out.println(ruleSet.toJson());
		System.out.println(ruleSet2.toJson());
		assertTrue("builder same", ruleSet.toJson().equals(ruleSet2.toJson()));
	}


	@Test
	public void testPlantUml() {

		RuleSet.Builder rb2 = new RuleSet.Builder();
		rb2
				.state("init", "at home", "at street")
				.state("hello", "say hello", "say bye")
				.initialState("init")
				.onState("init")
				.onEvent("go_out", "hello", "take a taxi")
				.onState("hello")
				.onEvent("go_home", "init", "take a bus");
		RuleSet ruleSet2 = rb2.build();


		System.out.println(ruleSet2.toPlantUml());
	}

	@Test
	public void dump() {
		RuleSet.Builder builder = new RuleSet.Builder();
		builder
				.state("NONE", null, null)
				.state("STOPPED", "DO_STOP", null)
				.state("STARTING", "DO_START", null)
				.state("READY", "DO_CHECK_PLAY", null)
				.state("PLAYING", "DO_PREPARE_PLAY", "DO_CLEAR_PLAY")
				.state("RECORDING", "DO_PREPARE_RECORD", "DO_CLEAR_RECORD")
				.state("SENDING", "DO_PREPARE_SEND", "DO_CLEAR_SEND")
				.state("INT", null, null)
				.state("HUP", null, null)

				.initialState("NONE")

				.onState("NONE")
				.onEvent("START", "STARTING")

				.onState("STARTING")
				.onEvent("START_DONE", "READY")

				.onState("READY")
				.onEvent("RESTART", "STARTING", "DO_RESET")
				.onEvent("STOP", "STOPPED")
				.onEvent("PLAY", "PLAYING", "DO_PLAY")
				.onEvent("TOGGLE_RECORD", "RECORDING", "DO_RECORD")

				.onState("PLAYING")
				.onEvent("PLAY_DONE", "READY")
				.onEvent("STOP", "STOPPED")

				.onState("RECORDING")
				.onEvent("TOGGLE_RECORD", "SENDING", "DO_SEND")
				.onEvent("STOP", "STOPPED")

				.onState("SENDING")
				.onEvent("SEND_DONE", "READY")
				.onEvent("STOP", "STOPPED");

		System.out.println(builder.build().toPlantUml());
	}
}