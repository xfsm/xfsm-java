package com.mabook.xfsm;

import org.junit.Test;

/**
 * Created by sng2c on 15. 7. 30..
 */
public class PlantUmlParserTest {
	@Test
	public void testEmitConsumeAll() {
		RuleSet.Builder builder = new RuleSet.Builder();
		builder
				.state("STOPPING", "IN_STOPPING", null)
				.state("STOPPED", "IN_STOPPED", null)
				.state("STARTING", "IN_STARTING", null)
				.state("STARTED", "IN_STARTED", null)
				.initialState("STOPPED");

		builder
				.onState("STOPPED")
				.onEvent(":START", "STARTING")

				.onState("STARTING")
				.onEvent(":STARTING_OK", "STARTED")
				.onEvent(":STARTING_FAIL", "STOPPED")

				.onState("STARTED")
				.onEvent(":STOP", "STOPPING")

				.onState("STOPPING")
				.onEvent(":STOPPING_OK", "STOPPED");
		String uml = builder.build().toPlantUml();

		System.out.println(uml);
		System.out.println("============================================================");

		PlantUmlParser parser = new PlantUmlParser();
		RuleSet rules = parser.parse(uml);

		String newUml = rules.toPlantUml();
		System.out.println("============================================================");
		System.out.println(newUml);

	}

}
