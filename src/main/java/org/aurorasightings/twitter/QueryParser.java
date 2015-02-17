package org.aurorasightings.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class QueryParser {

	// Split string by whitespace unless surrounded by quotes.
	private Pattern regex = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
	
	private List<Pattern> ignore = Arrays.asList(Pattern.compile("AND"), Pattern.compile("OR"), Pattern.compile("^-.*"));
	
	public List<String> getArgs(String input) {
		List<String> args = new ArrayList<>();
		Matcher m = regex.matcher(input);
		while (m.find()) {
			String arg = m.group(1);
			boolean ignoreThisOne = false;
			for (Pattern p : ignore) {
				if (p.matcher(arg).matches()) {
					ignoreThisOne = true;
					break;
				}
			}
			
			if (ignoreThisOne) {
				continue;
			}
			
			// strip leading and trailing quotes
			if (arg.startsWith("\"")) 
				arg = arg.substring(1, arg.length());
			if (arg.endsWith("\""))
				arg = arg.substring(0, arg.length() - 1);
			
			args.add(arg);
		}
		return args;
	}
}
