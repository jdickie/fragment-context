package eu.interedition.fragmentContext.text;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class TextPrimaryTest extends TestCase {
	private String corpora;
	private TextPrimary source;
	
	@Before
//	Create the instance of TextPrimary to use
	protected void setUp() throws Exception {
		super.setUp();
		corpora = "The quick, brown fox jumped over the cat.";
		source = new TextPrimary(corpora);
	}

	@After
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
//	Running simple tests on methods --
//	FIXME: add test(s) to assert Object/Class sameness
	public void testMethods() throws Exception {
		assertEquals(corpora, source.getContent());
		assertEquals("text/plain", source.getMimeType());
	}

}
