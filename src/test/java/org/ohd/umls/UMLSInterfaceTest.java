package org.ohd.umls;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

public class UMLSInterfaceTest extends TestCase {
	private Logger log = Logger.getLogger(UMLSInterfaceTest.class.getName());
	private UMLSInterface umls = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		if (umls == null){
		Properties config = new Properties();
		config.load(this.getClass().getResourceAsStream("/config.properties"));
		log.log(Level.INFO, "Found {0} Properties", config.size());
		umls = new UMLSInterface(config);
		log.log(Level.INFO, "Setup Complete");
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		umls.close();
	}

	
	public final void testNormalize() {
		String term = "leaves";
		String expected = "leaf";
		String result = umls.normalize(term);
		log.info("Normalized "+term+" to "+ result);
		assertEquals(expected, result);
	}

	public final void testGetCUIs() {
		String term = "Diabetes";
		String norm = umls.normalize(term);
		List<String> cuis = umls.getCUIs(norm);
		StringBuffer result = new StringBuffer();
		for (String c : cuis){
			result.append(c);
			result.append(", ");
		}
		result.delete(result.length()-2, result.length());
		log.info("Found cuis: "+result.toString());
	}

	public final void testGetCodingSystem() {
		List<CodingSystem> vocabs = umls.getCodingSystem();
		assertFalse(vocabs.isEmpty());
	}

	public final void testGetCode() {
		String cui = "C0038829";
		String expected = "53224005";
		CodingSystem cs = new CodingSystem("snomedct", "SNOMED CT", "2009");
		Code c = umls.getCode(cui, cs);
		log.info("Found code: "+c.getValue()+" for term "+c.getTerm());
		assertEquals(expected, c.getValue());
	}

}
