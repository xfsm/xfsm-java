package com.mabook.xfsm.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

public class XFSMParserTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    String loadResource(String resname) {
        try {
            InputStream is = getClass().getResourceAsStream(resname);
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = is.read()) != -1)
                sb.append((char) ch);
            return sb.toString();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testFromPlantUml() {

        String xfsmDsl = loadResource("/test.xfsm");
        String puml = loadResource("/test.puml");
        System.out.println(xfsmDsl);

        XFSMParser xp = new XFSMParser();
        RuleSet ruleset = xp.parse(xfsmDsl);
        System.out.println(ruleset.toJson());
        System.out.println(ruleset.toPlantUml());

    }
}