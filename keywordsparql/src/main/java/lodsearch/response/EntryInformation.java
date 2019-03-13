/**
 * 
 */
package lodsearch.response;


public class EntryInformation {
	public enum Type {
		TEXT, URL
	};
	private String displayText;
    private String uri;
    public EntryInformation() {
    		displayText="";
    		uri="";
    }
    public EntryInformation(String title, String url) {
    		displayText = title;
        uri = url;
    }
    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String title) {
    		displayText = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String url) {
        uri = url;
    }
}
