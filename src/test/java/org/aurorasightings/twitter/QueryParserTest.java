package org.aurorasightings.twitter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link QueryParser}.
 *
 */
public class QueryParserTest {

	// Class to test
	private QueryParser parser;
	
	@Before
	public void setUp() {
		parser = new QueryParser();
	}
	
	/**
	 * Single words separated by single space.
	 */
	@Test
    public void testGetArgs_SingleSpace () {
		String input = "one two three four five";
		List<String> expected = Arrays.asList("one", "two", "three", "four", "five");
		List<String> output = parser.getArgs(input);
		assertEquals(expected, output);
    }
	
	/**
	 * Single words separated by multiple spaces.
	 */
	@Test
    public void testGetArgs_MultipleSpace () {
		String input = "one two  three   four    five";
		List<String> expected = Arrays.asList("one", "two", "three", "four", "five");
		List<String> output = parser.getArgs(input);
		assertEquals(expected, output);
    }
	
	/**
	 * Single words separated by space, tab, newline.
	 */
	@Test
    public void testGetArgs_TabNewline () {
		String input = "one two\tthree\nfour\r\nfive";
		List<String> expected = Arrays.asList("one", "two", "three", "four", "five");
		List<String> output = parser.getArgs(input);
		assertEquals(expected, output);
    }
	
	/**
	 * Quoted phrases separated by single space.
	 */
	@Test
    public void testGetArgs_Phrases () {
		String input = "\"one two\" \"three four five\"";
		List<String> expected = Arrays.asList("one two", "three four five");
		List<String> output = parser.getArgs(input);
		assertEquals(expected, output);
    }
	
	/**
	 * Quoted phrases with missing end quote.
	 */
	@Test
    public void testGetArgs_PhrasesMissingQuote () {
		String input = "\"one two\" \"three four five";
		List<String> expected = Arrays.asList("one two", "three", "four", "five");
		List<String> output = parser.getArgs(input);
		assertEquals(expected, output);
    }
	
	/**
	 * Mixed words, phrases, and spacing.
	 */
	@Test
    public void testGetArgs_Mixed () {
		String input = "\"one two\"\tthree\n\nfour\r\n five";
		List<String> expected = Arrays.asList("one two", "three", "four", "five");
		List<String> output = parser.getArgs(input);
		assertEquals(expected, output);
    }
	
	/**
	 * Ignore words.
	 */
	@Test
    public void testGetArgs_Ignore () {
		String input = "\"one AND two\" AND three OR four -five -";
		List<String> expected = Arrays.asList("one AND two", "three", "four");
		List<String> output = parser.getArgs(input);
		assertEquals(expected, output);
    }
}
