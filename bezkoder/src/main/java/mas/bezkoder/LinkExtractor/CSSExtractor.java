package mas.bezkoder.LinkExtractor;

import mas.bezkoder.model.Tutorial;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CSSExtractor {
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";
    private static final String CSSRegex = "url\\((.*?)\\)";
    private String url;
    protected String date;
    protected String client = "http://118.67.133.84:8085/api/websites?web=";
    private HashSet<String> urls;
    private Tutorial tutorial;
    private String input;

    public CSSExtractor(String input, String url, HashSet<String> hs) {
        this.url = url;
        this.input = input;
        this.urls = hs;
    }

    public CSSExtractor(String input, Tutorial tutorial, String date) {
        this.tutorial = tutorial;
        this.input = input;
        this.date = date;
        if (date != null) {
            client = "http://118.67.133.84:8085/api/websites?date=" + date + "&web=";
        }
    }

    public void extractCSS() throws IOException, URISyntaxException, JSONException {
        Pattern pattern = Pattern.compile(CSSRegex);
        Matcher matcher = pattern.matcher(this.input);
        parseURL(matcher);

        pattern = Pattern.compile(otherRegex);
        matcher = pattern.matcher(this.input);
        parseOther(matcher);
    }

    public abstract void parseURL (Matcher matcher) throws JSONException, IOException, URISyntaxException;
    public abstract void parseOther(Matcher matcher) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getInput() {
        return input;
    }
    public void setInput(String input) {
        this.input = input;
    }
    public HashSet<String> getUrls() {
        return urls;
    }
    public void setUrls(HashSet<String> urls) {
        this.urls = urls;
    }
    public Tutorial getTutorial() {
        return tutorial;
    }
    public void setTutorial(Tutorial tutorial) {
        this.tutorial = tutorial;
    }

}
