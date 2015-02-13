package org.aurorasightings.twitter;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aurorasightings.config.TwitterProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.social.RateLimitExceededException;
import org.springframework.social.twitter.api.SearchParameters;
import org.springframework.social.twitter.api.SearchParameters.ResultType;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(TwitterProperties.class)
public class Stream implements Iterator<Tweet> {
	
	private static final Log log = LogFactory.getLog(Stream.class);
	
	private SearchParameters params;
	
	@Autowired
	private Twitter twitter;
	
	@Autowired
	private TwitterProperties props;
	
	
	private SearchResults results;
	private Iterator<Tweet> tweetIterator;
	private long minIdSeen = Long.MAX_VALUE;
	
	public Stream() {
		params = new SearchParameters("aurora OR \"northern lights\"");
		params.resultType(ResultType.RECENT);
		params.sinceId(0);
		params.maxId(Long.MAX_VALUE);
	}
	
	private void loadMore() {
		// Set the max ID to one less than the lowest ID seen so far
		if (minIdSeen - 1 < params.getMaxId()) {
			params.maxId(minIdSeen - 1);
		}
		
		// If that makes the max ID lower than tweets we loaded previously,
		// we're all done
		if (params.getMaxId() < params.getSinceId()) {
			return;
		}
		
		// Try to make the Twitter API call. If the rate limit is exceeded,
		// try a sleep then retry, up to 5 tries in total.
		int sleepMillis = props.getRateLimitWait() * 1000;
		for (int i = 0; i < 5; i++) {
			try {
				results = twitter.searchOperations().search(params);
				tweetIterator = results.getTweets().iterator();
				return;
			} catch (RateLimitExceededException e) {
				log.info(e.toString() + ": sleeping for " + sleepMillis + "ms");
				try {
					Thread.sleep(sleepMillis);
				} catch (InterruptedException ie) {
					log.warn("Interrupted while waiting for rate limit: " + ie);
				}
			}
		}
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
