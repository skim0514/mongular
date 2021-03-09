package mas.bezkoder.parser;

import mas.bezkoder.model.Tutorial;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mas.bezkoder.parser.Parser.otherRegex;
import static mas.bezkoder.parser.Parser.parseCss;

public abstract class LinkExtractor {
    private static final String CSSRegex = "url\\((.*?)\\)";
    private static final String htmlTag = "<(?!!)(?!/)\\s*([a-zA-Z0-9]+)(.*?)>";
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";
    private static final String style ="<style([\\s\\S]+?)</style>";
    private String url;
    private HashSet<String> urls;
    private Document document;
    private Tutorial tutorial;
    private String input;
    private int depth;

    public LinkExtractor(String url, HashSet hs, int depth) {
        this.url = url;
        this.urls = hs;
        this.depth = depth;
    }

    public LinkExtractor(Document document, Tutorial tutorial) {
        this.tutorial = tutorial;
        this.document = document;
    }

    public void extractHtml() throws IOException, URISyntaxException, JSONException {

        Elements srcsets = document.select("[srcset]");
        parseSrcSet(srcsets);

        Elements srcs = document.select("[src]");
        parseSrc(srcs);

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
}
