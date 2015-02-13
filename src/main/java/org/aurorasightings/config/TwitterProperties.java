package org.aurorasightings.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.social.twitter")
public class TwitterProperties extends org.springframework.boot.autoconfigure.social.TwitterProperties {

	/**
	 * Time in seconds to wait after rate limit exceeded
	 */
	private Integer rateLimitWait;

	public Integer getRateLimitWait() {
		return rateLimitWait;
	}

	public void setRateLimitWait(Integer rateLimitWait) {
		this.rateLimitWait = rateLimitWait;
	}
}
