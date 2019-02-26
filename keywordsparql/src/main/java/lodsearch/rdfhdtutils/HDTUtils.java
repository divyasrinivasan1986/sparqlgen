package lodsearch.rdfhdtutils;


import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;

public class HDTUtils {
	
	public static String removeStopWords(String query) {
		String output = null;
		try {
			output = ExudeData.getInstance().filterStoppings(query);
		} catch (InvalidDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}
	public static HashMap<String, String[]> removestopWordEntriesFromMap(String stopWordFiltered, HashMap<String, String[]> keywordVecs) {
		Iterator<Entry<String, String[]>> keywords = keywordVecs.entrySet().iterator();
		// Iterate over all the elements
		while (keywords.hasNext()) {
			Entry<String, String[]> entry = keywords.next();
			if(!stopWordFiltered.matches(".*\\b" + Pattern.quote(entry.getKey()) + "\\b.*"))
				keywords.remove();
		}
		return keywordVecs;
	}
	public static HashMap<String,String[]> cleanUIInputMap(HashMap<String,Object[][]> wordMap){
		HashMap<String,String[]> cleanedWordMap = new HashMap<String,String[]>();
    	wordMap.forEach((k, v) -> {
    		String[] terms = new String[10];
    		int i=0;
    		for(Object[] word:v) {
    			terms[i]=(String) word[0];
    			i++;
    		}
    		cleanedWordMap.put(k, terms);
    	});
    	return cleanedWordMap;
	}
}
