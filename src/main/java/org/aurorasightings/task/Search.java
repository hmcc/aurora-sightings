package org.aurorasightings.task;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Search {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private static final Log log = LogFactory.getLog(Search.class);

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
    	log.debug("The time is now " + dateFormat.format(new Date()));
    }
}
