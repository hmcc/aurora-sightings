package org.aurorasightings.data;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.social.twitter.api.Entities;
import org.springframework.social.twitter.api.MediaEntity;
import org.springframework.social.twitter.api.TwitterProfile;

public class Tweet {

	public Tweet() {

	}

	public Tweet(long tweetID, Date createdAt, String text, long userID,
			String userScreenName, String userName, Set<String> url) {
		super();
		this.tweetID = tweetID;
		this.createdAt = createdAt;
		this.text = text;
		this.userID = userID;
		this.userScreenName = userScreenName;
		this.userName = userName;
		this.url = url;
	}

	@Id
	private String id;

	@Field(value = "tweet_id")
	@Indexed(unique=true)
	private long tweetID;

	@Field(value = "created_at")
	private Date createdAt;

	private String text;

	@Field(value = "user_id")
	private long userID;

	@Field(value = "user_screen_name")
	private String userScreenName;

	@Field(value = "user_name")
	private String userName;

	private Set<String> url;

	public String getId() {
		return id;
	}

	public long getTweetID() {
		return tweetID;
	}

	public void setTweetID(long tweetID) {
		this.tweetID = tweetID;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getUserScreenName() {
		return userScreenName;
	}

	public void setUserScreenName(String userScreenName) {
		this.userScreenName = userScreenName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Set<String> getUrl() {
		return url;
	}

	public void setUrl(Set<String> url) {
		this.url = url;
	}
	
	public String toCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append(createdAt == null ? "" : createdAt).append(',');
		sb.append(tweetID).append(',');
		sb.append('"').append(text).append('"').append(',');
		sb.append(userID).append(',');
		sb.append(userScreenName == null ? "" : userScreenName).append(',');
		sb.append('"').append(userName == null ? "" : userName).append('"').append(',');
		if (url != null) {
			for (String item : url) {
				sb.append(item).append(' ');
			}
			
			if (sb.charAt(sb.length() - 1) == ' ') {
				sb.replace(sb.length() - 1, sb.length(), "");
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Tweet.class.getName()).append('[');
		sb.append(id).append(',');
		sb.append(toCSV());
		sb.append(']');
		return sb.toString();
	}

	public static Tweet getInstance(org.springframework.social.twitter.api.Tweet apiTweet) {
		if (apiTweet == null) {
			return null;
		}
		
		Tweet tweet = new Tweet();
		tweet.setTweetID(apiTweet.getId());
		tweet.setCreatedAt(apiTweet.getCreatedAt());
		tweet.setText(apiTweet.getText());
		
		TwitterProfile user = apiTweet.getUser();
		if (user != null) {
			tweet.setUserID(user.getId());
			tweet.setUserName(user.getName());
			tweet.setUserScreenName(user.getScreenName());
		}
		
		Entities entities = apiTweet.getEntities();
		if (entities != null) {
			Collection<MediaEntity> mediaEntities = entities.getMedia();
			if (mediaEntities != null) {
				Set<String> urls = new HashSet<>();
				for (MediaEntity entity : mediaEntities) {
					urls.add(entity.getUrl());
				}
				tweet.setUrl(urls);
			}
		}
		
		return tweet;
	}
}
