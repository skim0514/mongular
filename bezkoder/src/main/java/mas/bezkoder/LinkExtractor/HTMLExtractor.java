package mas.bezkoder.LinkExtractor;

import mas.bezkoder.model.Tutorial;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HTMLExtractor {
    protected static final String clientStart = "http://118.67.133.84:8085/api/websites?";
    protected String client = "http://118.67.133.84:8085/api/websites?web=";
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";
    private static final String style ="<style([\\s\\S]+?)</style>";
    private String url;
    private HashSet<String> urls;
    private HashSet<String> visited;
    private HashSet<String> checksums;
    private Document document;
    private Tutorial tutorial;
    private String input;
    protected String date;
    private int depth;

    public HTMLExtractor(String url, HashSet<String> hs, int depth, HashSet<String> visited, HashSet<String> checksums) {
        this.url = url;
        this.urls = hs;
        this.depth = depth;
        this.visited = visited;
        this.checksums = checksums;
    }

    public HTMLExtractor(Document document, Tutorial tutorial, String date) {
        this.tutorial = tutorial;
        this.document = document;
        this.date = date;
        if (date != null) {
            client = "http://118.67.133.84:8085/api/websites?date=" + date + "&web=";
        }
    }

    public void extractHtml() throws IOException, URISyntaxException, JSONException {

        Elements srcsets = document.select("[srcset]");
        parseSrcSet(srcsets);

        Elements srcs = document.select("[src]");
        parseSrc(srcs);

        Elements dsrc = document.select("[data-src]");
        parsedsrc(dsrc);

        Elements background = document.select("[background]");
        parseBackground(background);

        Elements hrefs = document.select("link[href]");
        parseLinkLink(hrefs);

        Elements styles = document.select("[style]");
        parseStyle(styles);

        Elements links = document.select("a[href]");
        parseALink(links);

        this.input = document.toString();

        //css styling as a separate attribute
        Pattern pattern = Pattern.compile(style);
        Matcher matcher = pattern.matcher(this.input);
        parseOtherStyle(matcher);

        pattern = Pattern.compile(otherRegex);
        matcher = pattern.matcher(this.input);
        parseOther(matcher);
    }

    public abstract void parsedsrc(Elements dsrc) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;

    public abstract void parseBackground(Elements background) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;
    public abstract void parseSrcSet(Elements srcSets) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;
    public abstract void parseSrc(Elements srcs) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;
    public abstract void parseLinkLink(Elements hrefs) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;
    public abstract void parseALink(Elements hrefs) throws IOException, URISyntaxException, JSONException;
    public abstract void parseStyle(Elements style) throws JSONException, IOException, URISyntaxException;
    public abstract void parseOtherStyle(Matcher matcher) throws JSONException, IOException, URISyntaxException;
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
    public int getDepth() {
        return depth;
    }
    public void setDepth(int depth) {
        this.depth = depth;
    }
    public Document getDocument() {
        return document;
    }
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * helper function to take url and accordingly change based on file info
     * @param url input url
     * @param string full url
     * @return new url
     */

    public static String replaceUrl(String url, String string) {
        if (url.contains(clientStart)) return url;
        System.out.println(url);
        System.out.println(string);
        return getAbsoluteURL(url, string);
    }

    private static String removeDots(String url) {
        if (url == null) return null;
        while (url.contains("/../")) {
            url = url.replace("/../", "/");
        }
        while (url.contains("/./")) {
            url = url.replace("/./", "/");
        }
        if (url.endsWith("/.")) url = url.substring(0, url.length()-1);

        return url;
    }

    public static String getAbsoluteURL(String baseurl, String relurl) {
        try {
            URL base = new URL(baseurl);
            return removeDots(new URL(base, relurl).toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
//            logger.error(String.format("%s, %s", baseurl, relurl));

        }
        return null;
    }

    public HashSet<String> getVisited() {
        return visited;
    }

    public void setVisited(HashSet<String> visited) {
        this.visited = visited;
    }

    public HashSet<String> getChecksums() {
        return checksums;
    }

    public void setChecksums(HashSet<String> checksums) {
        this.checksums = checksums;
    }
}
