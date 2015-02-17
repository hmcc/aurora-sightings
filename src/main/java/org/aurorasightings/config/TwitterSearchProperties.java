package org.aurorasightings.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.social.twitter.search")
public class TwitterSearchProperties {
	
	/**
	 * Whether to include retweets in the search results
	 */
	private Boolean includeRetweets = false;
	
	/**
	 * Search string
	 */
	private String searchTerm;
	
	/**
	 * Number of times to retry if rate limit exceeded
	 */
	private Integer rateLimitRetries = 2;
	
	/**
	 * Time in seconds to wait after rate limit exceeded
	 */
	private Integer rateLimitWait = 900;
	
	public Boolean isIncludeRetweets() {
		return includeRetweets;
	}

	public void setIncludeRetweets(Boolean includeRetweets) {
		this.includeRetweets = includeRetweets;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public Integer getRateLimitRetries() {
		return rateLimitRetries;
	}

	public void setRateLimitRetries(Integer rateLimitRetries) {
		this.rateLimitRetries = rateLimitRetries;
	}

	public Integer getRateLimitWait() {
		return rateLimitWait;
	}

	public void setRateLimitWait(Integer rateLimitWait) {
		this.rateLimitWait = rateLimitWait;
	}

	
	
	
}
