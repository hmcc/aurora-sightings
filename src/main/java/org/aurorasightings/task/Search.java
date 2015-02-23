package org.aurorasightings.task;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aurorasightings.data.Tweet;
import org.aurorasightings.data.TweetRepository;
import org.aurorasightings.twitter.SearchStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Search {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final Log log = LogFactory.getLog(Search.class);
	
	@Autowired
	private SearchStream stream;
	
	@Autowired
	private TweetRepository repository;
    
    @Scheduled(fixedRate = 60000)
    public void reportCurrentTime() {
    	log.debug("The time is now " + dateFormat.format(new Date()));
    	while (stream.hasNext()) {
    		Tweet tweet = Tweet.getInstance(stream.next());
    		
    		log.debug("TWEET " + tweet.toCSV());
    		
    		repository.save(tweet);
    	}
    }
}
