package org.aurorasightings.data;

import org.springframework.beans.factory.annotation.Autowired;

public class TweetRepositoryImpl implements TweetRepositoryCustom {
	
	@Autowired
	private TweetRepository repository;

	@Override
	public long getMaxTweetID() {
		Tweet tweet = repository.findTopByOrderByTweetIDDesc();
		return tweet == null ? -1 : tweet.getTweetID();
	}

}
