package eu.interedition.fragmentContext.text;

import static org.junit.Assert.*;

import org.junit.Test;

public class TextConstraintTest {

	@Test
	public void testCheckMethods() throws Exception {
//		Looking at the base functionality from the constructed object
//		
//		Creating instance of TextConstraint
		TextConstraint constraint = new TextConstraint(10,20);
		assertEquals("Starting position should be 10", 10, constraint.getStartPos());
		assertEquals("Starting position should be 20", 20, constraint.getEndPos());
		String str = "TextConstraint[10, 20]";
		
		assertEquals("String " + str + " should equal " + constraint.toString(), 
				str, constraint.toString());
	}

}
