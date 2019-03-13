/**
 * 
 */
package lodsearch.response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base Response class. 
 * Due to "What can you do" which returns various Buttons , an Array List of this class is to be returned.
 * Due to What can you do returning various options for each card, an array list internally of URL's have to be maintained.
 */
public class Response {
	Set<String> answers = new HashSet<String>();
	private Set<EntryInformation> entryList = new HashSet<EntryInformation>();
	/*
	 private List<EntryInformation> entryList = new ArrayList<EntryInformation>();
	 private String title;
	 private String content;
	 private String image;
	 public Response() {
		 title="";
		 content="";
		 image="";
	 }
	 public void addEntry(EntryInformation entry) {
	   entryList.add(entry);
	 }
	 public List<EntryInformation> getEntryList() {
	   return entryList;
	 }
	 public void setEntryList(List<EntryInformation> entry) {
       entryList = entry;
	 }
	 public String getTitle() {
       return title;
	 }
	 public void setTitle(String title) {
	   this.title = title;
	 }
	 public String getContent() {
	   return content;
	 }
	 public void setContent(String text) {
		 content = text;
	 }
	 public String getImage() {
	        return image;
	  }
     public void setImage(String imageName) {
        image = imageName;
     }

*/

	public Set<EntryInformation> getEntryList() {
		return entryList;
	}

	public void setEntryList(Set<EntryInformation> entryList) {
		this.entryList = entryList;
	}

	public Set<String> getAnswers() {
		return answers;
	}

	public void setAnswers(Set<String> answers) {
		this.answers = answers;
	}}
