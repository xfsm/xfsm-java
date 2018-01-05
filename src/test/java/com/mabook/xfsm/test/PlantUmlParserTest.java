package com.mabook.xfsm.test;

import org.junit.Test;

/**
 * Created by sng2c on 15. 7. 30..
 */
public class PlantUmlParserTest {
	@Test
	public void testPlantUml() {
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

	@Test
	public void testFromPlantUml() {
		String uml = "@startuml\n" +
				"State HOME\n" +
				"HOME : in 'SAY_I_AM_BACK'\n" +
				"HOME : out 'SAY_I_WILL_BE_BACK'\n" +
				"\n" +
				"State SCHOOL\n" +
				"SCHOOL : in 'YO_FRIENDS'\n" +
				"SCHOOL : out 'BYE_FRIENDS'\n" +
				"\n" +
				"[*] --> HOME : event '__init__'\n" +
				"HOME --> SCHOOL : event 'EV_AM8'\n" +
				"SCHOOL --> HOME : event 'EV_PM7' do 'HAVE_DINNER'\n" +
				"@enduml";
		PlantUmlParser parser = new PlantUmlParser();
		RuleSet rules = parser.parse(uml);
		System.out.println(rules.toJson());
		System.out.println(rules.toPlantUml());
	}

}
