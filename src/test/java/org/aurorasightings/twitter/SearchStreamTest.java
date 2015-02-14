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
 * Tests for @link SearchStream.
 *
 */
public class SearchStreamTest {
	
	// Mocks
	private Twitter twitter = mock(Twitter.class);
	private SearchOperations searchOperations = mock(SearchOperations.class);
	private SearchResults searchResults = mock(SearchResults.class);
	
	// Class to test
	private SearchStream stream;
	
	// Test data
	private List<Tweet> empty = new ArrayList<>();
	private List<Tweet> retweets = Arrays.asList(new TweetBuilder().withID(1).withRetweet(true).build(), 
			new TweetBuilder().withID(2).withRetweet(true).build(), 
			new TweetBuilder().withID(3).withRetweet(true).build());
	private List<Tweet> mixed = Arrays.asList(new TweetBuilder().withID(1).build(), 
			new TweetBuilder().withID(2).withRetweet(true).build(), 
			new TweetBuilder().withID(3).build());
	
	@Before
	public void setUp() {
		TwitterSearchProperties props = new TwitterSearchProperties();
		props.setAppId("appId");
		props.setAppSecret("appSecret");
		props.setIncludeRetweets(true);
		props.setRateLimitRetries(1);
		props.setRateLimitWait(1);
		
		stream = new SearchStream();
		stream.setProps(props);
		stream.setTwitter(twitter);
		stream.configureSinceId(1);
		
		when(twitter.searchOperations()).thenReturn(searchOperations);
		when(searchOperations.search(any(SearchParameters.class))).thenReturn(searchResults);
	}
	
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
	
	@Test
    public void testIterator_AllRetweets_RetweetsIncluded () {
		when(searchResults.getTweets()).thenReturn(retweets);
		
		for (Tweet retweet : retweets) {	
			assertTrue(stream.hasNext());
			assertEquals(retweet.getId(), stream.next().getId());
		}
		assertFalse(stream.hasNext());
    }
	
	@Test
    public void testHasNext_AllRetweets_RetweetsNotIncluded () {
		stream.getProps().setIncludeRetweets(false);
		when(searchResults.getTweets()).thenReturn(retweets);
		
		assertFalse(stream.hasNext());
    }
	
	@Test(expected=NoSuchElementException.class)
    public void testNext_AllRetweets_RetweetsNotIncluded () {
		stream.getProps().setIncludeRetweets(false);
		when(searchResults.getTweets()).thenReturn(retweets);
		
		stream.next();
    }
	
	@Test
    public void testIterator_Mixed_RetweetsIncluded () {
		when(searchResults.getTweets()).thenReturn(mixed);
		
		for (Tweet tweet : mixed) {	
			assertTrue(stream.hasNext());
			assertEquals(tweet.getId(), stream.next().getId());
		}
		assertFalse(stream.hasNext());
    }
	
	@Test
    public void testIterator_Mixed_RetweetsNotIncluded () {
		stream.getProps().setIncludeRetweets(false);
		when(searchResults.getTweets()).thenReturn(mixed);
		
		for (Tweet tweet : mixed) {	
			if (!tweet.isRetweet()) {
				assertTrue(stream.hasNext());
				assertEquals(tweet.getId(), stream.next().getId());
			}
		}
		assertFalse(stream.hasNext());
    }
	
}
