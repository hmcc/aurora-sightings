package org.aurorasightings.builder;

import org.springframework.social.twitter.api.Tweet;

/**
 * Helper class for building {@link Tweet}. 
 */
public class TweetBuilder implements Builder<Tweet> {

	private long id;
	private boolean retweet;
	private String text;
	
	public TweetBuilder withID(long id) {
		this.id = id;
		return this;
	}
	
	public TweetBuilder withRetweet(boolean retweet) {
		this.retweet = retweet;
		return this;
	}
	
	public TweetBuilder withText(String text) {
		this.text = text;
		return this;
	}
	
	public Tweet build() {
		Tweet tweet = new Tweet(id, text, null, null, null, null, 0, text, text);
		if (retweet) {
			Tweet retweet = new Tweet(id + 100, text, null, null, null, null, 0, text, text);
			tweet.setRetweetedStatus(retweet);
		}
		return tweet;
	}
	
}
