package mas.bezkoder.linkExtract;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;

public interface CSSExtractor {
    void extractCSS() throws IOException, URISyntaxException, JSONException;
    void parseCSS(Matcher matcher) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException;
}
