package org.aurorasightings.task;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Component;

@Component
public class Search {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final Log log = LogFactory.getLog(Search.class);
	
	@Autowired
	private Twitter twitter;
    
    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
    	log.debug(twitter);
    	log.debug("The time is now " + dateFormat.format(new Date()));
    }
}
