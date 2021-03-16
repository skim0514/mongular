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
    private static final String client = "http://118.67.133.84:8085/api/websites?web=";
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";
    private static final String style ="<style([\\s\\S]+?)</style>";
    private String url;
    private HashSet<String> urls;
    private Document document;
    private Tutorial tutorial;
    private String input;
    private int depth;

    public HTMLExtractor(String url, HashSet<String> hs, int depth) {
        this.url = url;
        this.urls = hs;
        this.depth = depth;
    }

    public HTMLExtractor(Document document, Tutorial tutorial) {
        this.tutorial = tutorial;
        this.document = document;
    }

    public void extractHtml() throws IOException, URISyntaxException, JSONException {

        Elements srcsets = document.select("[srcset]");
        parseSrcSet(srcsets);

        Elements srcs = document.select("[src]");
        parseSrc(srcs);

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
     * @throws URISyntaxException if input string is incorrectly built
     */

    public static String replaceUrl(String url, String string) throws URISyntaxException, MalformedURLException {
        if (url.contains(client)) return url;
        String strFind = "../";
        int count = 0, fromIndex = 0;
        while ((fromIndex = url.indexOf(strFind, fromIndex)) != -1 ){
            count++;
            fromIndex++;
        }
        String newUrl;

        //fixing paths
        newUrl = getUrlFromPath(url, string, count);
        return newUrl;
    }

    /**
     * analyze and change url based on relative path
     * @param url input path
     * @param title information to adjust url
     * @param count number of backtracks
     * @return full absolute path
     * @throws URISyntaxException for incorrectly built urls
     */

    public static String getUrlFromPath(String url, String title, int count) throws URISyntaxException, MalformedURLException {
        String newUrl;
        String domain = new URL(title).getHost();
        if (url.startsWith("http")) {
            newUrl = url;
        } else if (url.startsWith("#")) {
            URL store = new URL(title);
            if (store.getRef() == null) newUrl = title + url;
            else newUrl = title.replace("#" + store.getRef(), "") + url;
        } else if (url.startsWith("//")) {
            newUrl = "http:" + url;
        } else if (url.startsWith("/")) {
            URI link = new URI(title);
            newUrl = link.getScheme() + "://" + domain + url;
        } else if (url.startsWith("./")) {
            URI parent = new URI(title);
            parent = parent.resolve(".");
            newUrl = parent.toString().endsWith("/") ? parent.toString() + url.substring(2) :
                    parent.toString() + "/" + url.substring(2);
        } else if (url.startsWith("../")) {
            int back = count;
            if (title.endsWith("/")) back --;
            URI link = new URI(title);
            for (int i = 0; i <= back; i++) {
                if (link.getPath().length() <= 1) break;
                link = link.getPath().endsWith("/") ? link.resolve("..") : link.resolve(".");
            }
            newUrl = link.toString() + url.substring(3 * count);
        } else {
            URI parent = new URI(title);
            parent = parent.resolve(".");
            newUrl = parent.toString().endsWith("/") ? parent.toString() + url :
                    parent.toString() + "/" + url;
        }
        return newUrl;
    }
}
