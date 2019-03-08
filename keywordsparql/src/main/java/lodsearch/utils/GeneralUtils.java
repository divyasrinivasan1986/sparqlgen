package lodsearch.utils;


import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;

public class GeneralUtils {
	
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
	public static LinkedHashMap<String, String[]> removeStopWordEntriesFromMap(String stopWordFiltered, LinkedHashMap<String, String[]> keywordVecs) {
		Iterator<Entry<String, String[]>> keywords = keywordVecs.entrySet().iterator();
		// Iterate over all the elements
		while (keywords.hasNext()) {
			Entry<String, String[]> entry = keywords.next();
			if(!stopWordFiltered.toLowerCase().matches(".*\\b" + Pattern.quote(entry.getKey().toLowerCase()) + "\\b.*"))
				keywords.remove();
		}
		return keywordVecs;
	}
	
	public static LinkedHashMap<String,String[]> cleanUIInputMap(LinkedHashMap<String,Object[][]> wordMap){
		LinkedHashMap<String,String[]> cleanedWordMap = new LinkedHashMap<String,String[]>();
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
