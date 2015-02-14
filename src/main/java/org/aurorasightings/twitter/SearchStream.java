package org.aurorasightings.twitter;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aurorasightings.config.TwitterSearchProperties;
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
@EnableConfigurationProperties(TwitterSearchProperties.class)
public class SearchStream implements Iterator<Tweet> {
	
	private static final Log log = LogFactory.getLog(SearchStream.class);
	
	@Autowired
	private Twitter twitter;
	
	@Autowired
	private TwitterSearchProperties props;
	
	private SearchParameters params;
	private Iterator<Tweet> tweetIterator;
	private Tweet nextResult;
	private long minIdSeen = Long.MAX_VALUE;
	
	private void initParams() {
		if (params == null) {
			params = new SearchParameters(props.getSearchTerm());
			params.resultType(ResultType.RECENT);
			params.sinceId(0);
			params.maxId(Long.MAX_VALUE);
		}
	}
	
	private void loadMore() {
		// Set the max ID to one less than the lowest ID seen so far
		initParams();
		if (minIdSeen - 1 < params.getMaxId()) {
			params.maxId(minIdSeen - 1);
		}
		
		// If that makes the max ID lower than tweets we loaded previously,
		// we're all done
		if (params.getMaxId() < params.getSinceId()) {
			return;
		}
		
		// Try to make the Twitter API call. If the rate limit is exceeded,
		// try a sleep then retry.
		int sleepMillis = props.getRateLimitWait() * 1000;
		int tries = props.getRateLimitRetries() + 1;
		for (int i = 0; i < tries; i++) {
			try {
				SearchResults results = twitter.searchOperations().search(params);
				if (results == null || results.getTweets() == null) {
					return;
				}
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
	
	// protected methods for unit testing
	protected void setTwitter(Twitter twitter) {
		this.twitter = twitter;
	}
	
	protected void setProps(TwitterSearchProperties props) {
		this.props = props;
	}
	
	protected TwitterSearchProperties getProps() {
		return props;
	}

	/**
	 * Allows the caller to limit the results to Tweets with IDs greater than
	 * or equal to that specified.
	 * 
	 * @param sinceId - lowest ID that will be returned
	 */
	public void configureSinceId(long sinceId) {
		initParams();
		params.sinceId(sinceId);
	}
	
	// Iterator methods
	@Override
	public boolean hasNext() {

		while  (nextResult == null) {
			// Is the iterator uninitialised or exhausted? If so, try loading more results.
			if (tweetIterator == null || !tweetIterator.hasNext()) {
				loadMore();
			}
			// If there are no more results, we're done
			if (tweetIterator == null || !tweetIterator.hasNext()) {
				return false;
			}
			
			nextResult = tweetIterator.next();
			updateMinIdSeen(nextResult);
			
			// If this result is a retweet and we're excluding those, move on
			if (!props.isIncludeRetweets() && nextResult.isRetweet()) {
				nextResult = null;
			}
		}
		return true;
	}

	@Override
	public Tweet next() {
		if (nextResult == null) {
			throw new NoSuchElementException();
		}
		Tweet resultToReturn = nextResult;
		nextResult = null;
		return resultToReturn;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
