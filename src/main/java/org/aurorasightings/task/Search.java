package org.aurorasightings.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aurorasightings.twitter.SearchStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.twitter.api.Entities;
import org.springframework.social.twitter.api.MediaEntity;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.stereotype.Component;

@Component
public class Search {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final Log log = LogFactory.getLog(Search.class);
	
	@Autowired
	private SearchStream stream;
    
    @Scheduled(fixedRate = 60000)
    public void reportCurrentTime() {
    	log.debug("The time is now " + dateFormat.format(new Date()));
    	while (stream.hasNext()) {
    		Tweet tweet = stream.next();
    		TwitterProfile user = tweet.getUser();
    		String tweetStr = tweet.getCreatedAt() + "," + tweet.getId() + ",\"" + tweet.getText() + "\"";
    		String userStr = user.getId() + "," + user.getScreenName() + "," + user.getName();
    		String imageStr = "";
    		Entities entities = tweet.getEntities();
    		if (entities != null) {
    			List<MediaEntity> media = entities.getMedia();
    			if (!(media == null || media.isEmpty())) {
    				for (MediaEntity entity : media) {
    					imageStr = imageStr + entity.getUrl() + " ";
    				}
    				imageStr = imageStr.substring(0, imageStr.length() - 1);
    			}
    		}
    		String geoStr = "";
    		Map<String, Object> extra = tweet.getExtraData();
    		if (extra != null) {
    			Object geo = extra.get("geo");
    			if (geo != null) {
    				geoStr = geo.toString();
    			}
    		}
    		log.debug("TWEET " + tweetStr + "," + userStr + "," + imageStr + "," + geoStr);
    	}
    }
}
