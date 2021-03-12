package mas.bezkoder.linkExtract;

import org.json.JSONException;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;

public interface HTMLExtractor {
    void extractHtml() throws IOException, URISyntaxException, JSONException;
    void parseBackground(Elements background) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;
    void parseSrcSet(Elements srcSets) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;
    void parseSrc(Elements srcs) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;
    void parseLinkLink(Elements hrefs) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;
    void parseALink(Elements hrefs) throws IOException, URISyntaxException, JSONException;
    void parseStyle(Elements style) throws JSONException, IOException, URISyntaxException;
    void parseOtherStyle(Matcher matcher) throws JSONException, IOException, URISyntaxException;
    void parseOther(Matcher matcher) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;
}
