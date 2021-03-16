package mas.bezkoder.crawler;

import mas.bezkoder.LinkExtractor.CSSExtractor;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.regex.Matcher;

import static mas.bezkoder.LinkExtractor.LinkExtractor.replaceUrl;

public class CrawlCSS extends CSSExtractor {

    public CrawlCSS(String url, String input, HashSet<String> hs) {
        super(url, input, hs);
    }

    @Override
    public void parseURL(Matcher matcher) throws JSONException, IOException, URISyntaxException {
        String url = getUrl();
        HashSet<String> hs = getUrls();
        while (matcher.find()) {
            String group = matcher.group(1);
            group = group.replaceAll("\"", "");
            group = group.replaceAll("'", "");
            if (group.startsWith("data:")) continue;
            String newUrl = replaceUrl(group, url);
            hs.add(newUrl);
        }
        setUrls(hs);
    }

    @Override
    public void parseOther(Matcher matcher) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        String url = getUrl();
        HashSet<String> hs = getUrls();
        while (matcher.find()) {
            String group = matcher.group(0);
            if (group.startsWith("data:")) continue;
            String newUrl = replaceUrl(group, url);
            hs.add(newUrl);
        }
        setUrls(hs);
    }
}
