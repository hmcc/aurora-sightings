package org.aurorasightings.twitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.aurorasightings.builder.TweetBuilder;
import org.aurorasightings.config.TwitterSearchProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.SearchOperations;
import org.springframework.social.twitter.api.SearchParameters;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;

/**
 * Tests for {@link SearchStream}.
 *
 */
public class SearchStreamTest {
	
	// Mocks
	private QueryParser parser = mock(QueryParser.class);
	private Twitter twitter = mock(Twitter.class);
	private SearchOperations searchOperations = mock(SearchOperations.class);
	private SearchResults searchResults = mock(SearchResults.class);
	
	// Class to test
	private SearchStream stream;
	
	// Test data
	private List<Tweet> empty = new ArrayList<>();
	private List<Tweet> retweetsAllMatching = Arrays.asList(
			new TweetBuilder().withID(1).withText("word").withRetweet(true).build(), 
			new TweetBuilder().withID(2).withText("word").withRetweet(true).build(), 
			new TweetBuilder().withID(3).withText("word").withRetweet(true).build());
	private List<Tweet> mixedAllMatching = Arrays.asList(
			new TweetBuilder().withID(1).withText("word").build(), 
			new TweetBuilder().withID(2).withText("word").withRetweet(true).build(), 
			new TweetBuilder().withID(3).withText("word").build());
	private List<Tweet> retweetsTwoMatching = Arrays.asList(
			new TweetBuilder().withID(1).withText("This tweet contains the search WORD").withRetweet(true).build(), 
			new TweetBuilder().withID(2).withText("This tweet contains a Phrase matching the search").withRetweet(true).build(), 
			new TweetBuilder().withID(3).withText("This tweet doesn't match the wholeword").withRetweet(true).build(),
			new TweetBuilder().withID(4).withText("This tweet doesn't match a phraseatall").withRetweet(true).build());
	
	@Before
	public void setUp() {
		TwitterSearchProperties props = new TwitterSearchProperties();
		props.setIncludeRetweets(true);
		props.setRateLimitRetries(1);
		props.setRateLimitWait(1);
		
		when(parser.getArgs(any(String.class))).thenReturn(Arrays.asList("word", "a phrase"));
		when(twitter.searchOperations()).thenReturn(searchOperations);
		when(searchOperations.search(any(SearchParameters.class))).thenReturn(searchResults);
		
		stream = new SearchStream();
		stream.setParser(parser);
		stream.setProps(props);
		stream.setTwitter(twitter);
		stream.configureSinceId(1);
	}
	
	/**
	 * Test iterator behaviour when the tweets list is null.
	 */
	@Test
    public void testHasNext_NullResults () {
		when(searchResults.getTweets()).thenReturn(null);
		
		assertFalse(stream.hasNext());
    }
	
	@Test(expected=NoSuchElementException.class)
    public void testNext_NullResults () {
		when(searchResults.getTweets()).thenReturn(null);
		
		stream.next();
    }
	
	/**
	 * Test iterator behaviour when the tweets list is empty.
	 */
	@Test
    public void testHasNext_EmptyResults () {
		when(searchResults.getTweets()).thenReturn(empty);
		
		assertFalse(stream.hasNext());
    }
	
	@Test(expected=NoSuchElementException.class)
    public void testNext_EmptyResults () {
		when(searchResults.getTweets()).thenReturn(empty);
		
		stream.next();
    }
	
	/**
	 * Test that all retweets are returned when retweets are included
	 */
	@Test
    public void testIterator_AllRetweets_RetweetsIncluded () {
		when(searchResults.getTweets()).thenReturn(retweetsAllMatching);
		
		for (Tweet retweet : retweetsAllMatching) {	
			assertTrue(stream.hasNext());
			assertEquals(retweet.getId(), stream.next().getId());
		}
		assertFalse(stream.hasNext());
    }
	
	/**
	 * Test that no retweets are returned when retweets are excluded
	 */
	@Test
    public void testHasNext_AllRetweets_RetweetsNotIncluded () {
		stream.getProps().setIncludeRetweets(false);
		when(searchResults.getTweets()).thenReturn(retweetsAllMatching);
		
		assertFalse(stream.hasNext());
    }
	
	@Test(expected=NoSuchElementException.class)
    public void testNext_AllRetweets_RetweetsNotIncluded () {
		stream.getProps().setIncludeRetweets(false);
		when(searchResults.getTweets()).thenReturn(retweetsAllMatching);
		
		stream.next();
    }
	
	/**
	 * Test that both retweets and original tweets are returned when retweets
	 * are included.
	 */
	@Test
    public void testIterator_Mixed_RetweetsIncluded () {
		when(searchResults.getTweets()).thenReturn(mixedAllMatching);
		
		for (Tweet tweet : mixedAllMatching) {	
			assertTrue(stream.hasNext());
			assertEquals(tweet.getId(), stream.next().getId());
		}
		assertFalse(stream.hasNext());
    }
	
	/**
	 * Test that only retweets are returned when retweets are excluded.
	 */
	@Test
    public void testIterator_Mixed_RetweetsNotIncluded () {
		stream.getProps().setIncludeRetweets(false);
		when(searchResults.getTweets()).thenReturn(mixedAllMatching);
		
		for (Tweet tweet : mixedAllMatching) {	
			if (!tweet.isRetweet()) {
				assertTrue(stream.hasNext());
				assertEquals(tweet.getId(), stream.next().getId());
			}
		}
		assertFalse(stream.hasNext());
    }
	
	/**
	 * Test that tweets whose text does not match are excluded.
	 */
	@Test
    public void testIterator_Retweets_TwoMatching () {
		when(searchResults.getTweets()).thenReturn(retweetsTwoMatching);
		
		for (int i = 0; i < 2; i++) {
			Tweet tweet = retweetsTwoMatching.get(i);
			assertTrue(stream.hasNext());
			assertEquals(tweet.getId(), stream.next().getId());
		}
		assertFalse(stream.hasNext());
    }
	
}
