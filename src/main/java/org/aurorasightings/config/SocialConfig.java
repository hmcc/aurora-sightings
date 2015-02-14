package org.aurorasightings.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.social.TwitterProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(TwitterProperties.class)
public class SocialConfig extends SocialConfigurerAdapter {
	
	@Autowired
	private TwitterSearchProperties props;
	
	@Bean
	public Twitter twitter() {
		return new TwitterTemplate(props.getAppId(), props.getAppSecret());
	}
}
