package org.aurorasightings.twitter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

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
	private QueryParser parser;
	
	@Autowired
	private Twitter twitter;
	
	@Autowired
	private TwitterSearchProperties props;
	
	
	private SearchParameters params;
	private Iterator<Tweet> tweetIterator;
	private Tweet nextResult;
	private long minIdSeen = Long.MAX_VALUE;
	private List<Pattern> searchRegexes;
	
	private void initParams() {
		if (params == null) {
			params = new SearchParameters(props.getSearchTerm());
			params.resultType(ResultType.RECENT);
			params.lang("en");
			params.sinceId(0);
			params.maxId(Long.MAX_VALUE);
		}
		if (searchRegexes == null) {
			searchRegexes = new ArrayList<>();
			List<String> searchTerms = parser.getArgs(props.getSearchTerm());
			for (String term : searchTerms) {
				searchRegexes.add(Pattern.compile("\\b" + term + "\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
			}
		}
	}
	
	private void loadMore() {
		// Set the max ID we want to one less than the lowest ID seen so far
		// (results are returned sorted by ID descending)
		initParams();
		if (minIdSeen - 1 < params.getMaxId()) {
			params.maxId(minIdSeen - 1);
		}
		
		// If that makes the max ID lower than tweets we loaded previously,
		// we're all done
		log.debug("loadMore(): MaxId " + params.getMaxId() + ", sinceId " + params.getSinceId());
		if (params.getMaxId() < params.getSinceId()) {
			log.info("MaxId " + params.getMaxId() + " is less than sinceId " + params.getSinceId() + ": all done");
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
					log.info("Null SearchResults or SearchResults with null tweets: all done");
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
	protected void setParser(QueryParser parser) {
		this.parser = parser;
	}
	
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
	 * Reset the {@link SearchStream} to its newly initialised state
	 * @return {@link SearchStream}
	 */
	public SearchStream reset() {
		initParams();
		minIdSeen = Long.MAX_VALUE;
		log.debug("SearchStream reset");
		return this;
	}

	/**
	 * Allows the caller to limit the results to Tweets with IDs greater than
	 * or equal to that specified.
	 * 
	 * @param sinceId - lowest ID that will be returned
	 * 
	 * @return {@link SearchStream}
	 */
	public SearchStream configureSinceId(long sinceId) {
		initParams();
		params.sinceId(sinceId);
		log.debug("SearchStream configured with sinceId " + sinceId);
		return this;
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
				log.debug("hasNext(): tried loading more but tweetIterator " + tweetIterator + " is still uninitialised or exhausted, giving up.");
				return false;
			}
			
			nextResult = tweetIterator.next();
			updateMinIdSeen(nextResult);
			
			// If this result is a retweet and we're excluding those, move on
			if (!props.isIncludeRetweets() && nextResult.isRetweet()) {
				nextResult = null;
				
			// If the result doesn't actually contain the search text (Twitter
			// also returns results matching on e.g. username), move on
			} else {
				boolean found = false;
				for (Pattern regex : searchRegexes) {
					
					if (regex.matcher(nextResult.getText()).find()) {
						found = true;
						break;
					}
				}
				if (!found) {
					nextResult = null;
				}
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
