/**
 * 
 */
package lodsearch.request;

import java.util.LinkedHashMap;

public class RequestContent {

	private String text;
	private LinkedHashMap<String, Object[][]> wordMap;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public LinkedHashMap<String, Object[][]> getWordMap() {
		return wordMap;
	}

	public void setWordMap(LinkedHashMap<String, Object[][]> wordMap) {
		this.wordMap = wordMap;
	}

	
	private String payload;

	public RequestContent() {
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String content) {
		payload = content;
	}

}
