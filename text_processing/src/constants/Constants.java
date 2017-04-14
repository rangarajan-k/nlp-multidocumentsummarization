package constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
	public static Map<String, List<String>> catQuestions = new HashMap<String, List<String>>();
	
	public Constants(){
		catQuestions.put("Accidents and Natural Disasters", new ArrayList<String>(Arrays.asList("what", "when", 
				"where", "why", "who affected", "damages", "measures")));
		catQuestions.put("Attacks", new ArrayList<String>(Arrays.asList("what", "when", 
				"where", "perpetrartors", "why", "who affected", "damages", "measures")));
		catQuestions.put("Health and Safety", new ArrayList<String>(Arrays.asList("what", "how", 
				"why", "who affected", "measures")));
		catQuestions.put("Endangered Resources", new ArrayList<String>(Arrays.asList("what", "importance", 
				"threats", "measures")));
		catQuestions.put("Investigations and Trials", new ArrayList<String>(Arrays.asList("who", "investigators", 
				"why", "charges", "plead", "sentence")));
	}
}
