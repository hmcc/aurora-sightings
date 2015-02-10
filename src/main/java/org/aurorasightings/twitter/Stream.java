package org.aurorasightings.twitter;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.SearchParameters;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Service;

@Service
public class Stream implements Iterator<Tweet> {
	
	private static final Log log = LogFactory.getLog(Stream.class);
	
	private SearchParameters params;
	
	@Autowired
	private Twitter twitter;
	
	
	private SearchResults results;
	private Iterator<Tweet> tweetIterator;
	private long minIdSeen = Long.MAX_VALUE;
	
	public Stream() {
		params = new SearchParameters("aurora OR \"northern lights\"");
		params.sinceId(0);
		params.maxId(Long.MAX_VALUE);
	}
	
	
	private void loadMore() {
		if (minIdSeen - 1 < params.getMaxId()) {
			params.maxId(minIdSeen - 1);
		}
		if (params.getMaxId() < params.getSinceId()) {
			return;
		}
		results = twitter.searchOperations().search(params);
		tweetIterator = results.getTweets().iterator();
	}
	
	private void updateMinIdSeen(Tweet tweet) {
		if (tweet.getId() < minIdSeen) {
			minIdSeen = tweet.getId();
		}
	}

	public void configureSinceId(long sinceId) {
		params.sinceId(sinceId);
	}
	
	@Override
	public boolean hasNext() {
		if (tweetIterator == null || !tweetIterator.hasNext()) {
			loadMore();
		}
		return tweetIterator != null && tweetIterator.hasNext();
	}

	@Override
	public Tweet next() {
		if (tweetIterator == null || !tweetIterator.hasNext()) {
			loadMore();
		}
		if (tweetIterator == null || !tweetIterator.hasNext()) {
			throw new NoSuchElementException();
		}
		Tweet tweet = tweetIterator.next();
		updateMinIdSeen(tweet);
		return tweet;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
