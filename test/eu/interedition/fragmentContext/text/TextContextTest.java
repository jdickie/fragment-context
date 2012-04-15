package eu.interedition.fragmentContext.text;


import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class TextContextTest extends TestCase {
	// Requires use of a primary source and the related
	// constraint. 
	private TextPrimary source;
	private TextConstraint constraint;
	// Constructing TextContext using it's 2 available
	// constructors
	private TextContext annoType1;
	private TextContext annoType2;
	
	@Before
	// Build required primary, constraints
	protected void setUp() throws Exception {
		super.setUp();
		String txt = "The quick, brown fox jumped over the lazy cat. " +
				"The slow, lazy cat meowed incessantly and just kept drinking" +
				"her beer.";
		// Using basic text, each test inserts 
		// an annotation within the text to be checked
		source = new TextPrimary(txt);
		constraint = new TextConstraint(12, 18);
		
	}

	@Test
	public void testContextMethods() {
		fail("Not yet implemented");
	}

}
