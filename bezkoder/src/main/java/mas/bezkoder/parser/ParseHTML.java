package mas.bezkoder.parser;

import mas.bezkoder.LinkExtractor.HTMLExtractor;
import mas.bezkoder.model.Tutorial;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mas.bezkoder.parser.ParseCSS.parseCSS;

public class ParseHTML extends HTMLExtractor {
    private static final String regex = "https?://([^{}<>\"'\\s)]*)";

    /**
     * Add BlackList Urls to this array
     */
    private String[] blacklistArray  = {};
    private final HashSet<String> blacklist = new HashSet<>(Arrays.asList(blacklistArray));


    public ParseHTML(Document document, Tutorial tutorial, String date) {
        super(document, tutorial, date);
    }

    /**
     * function to parse if document is html
     * @param input String input of entire document
     * @param tutorial gives document information
     * @param date if date is contained, gives date in string format
     * @return parsed string input of html
     * @throws IOException if something is missing
     * @throws URISyntaxException if parsed url is unusual
     */
    public static String parseHtml(String input, Tutorial tutorial, String date) throws IOException, URISyntaxException, JSONException {

        Document document = Jsoup.parse(input);
        ParseHTML parser = new ParseHTML(document, tutorial, date);
        parser.extractHtml();
        return parser.getInput();
    }

    /**
     * Helper function for srcset - splits and parses
     * @param hold holds initial strings
     * @param client our client url
     * @param tutorial metadata for our code
     * @return new src set style html attribute
     */
    public static String setHelp(String hold, String client, Tutorial tutorial) throws UnsupportedEncodingException {
        String rs = hold;
        String[] strings;
        strings = hold.split(", ");
        for (String string: strings) {
            String[] urls = string.split(" ");
            String url = urls[0];
            rs = rs.replace(url, client + java.net.URLEncoder.encode(replaceUrl(url, tutorial.getTitle()), StandardCharsets.UTF_8.name()));
        }
        return rs;
    }

    /**
     * checks input with simplest regex to find link
     * @param input string input
     * @param tutorial information about link
     * @return parsed output
     * @throws UnsupportedEncodingException if encoding of text is unusual
     */
    public static String otherRegex(String input, String client, Tutorial tutorial) throws UnsupportedEncodingException {
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            if (group.startsWith(clientStart)) continue;
            if (group.startsWith("data:")) continue;
            String replacement = replaceUrl(group, tutorial.getTitle());
            if (replacement == null) continue;
            String newUrl = client + java.net.URLEncoder.encode(replacement, StandardCharsets.UTF_8.name());
            try {
                input = input.replace("'" + group + "'", "'" + newUrl + "'");
                input = input.replace("\"" + group + "\"", "\"" + newUrl + "\"");
            } catch (Exception ignored) {
            }
        }
        return input;
    }

    /**
     * parser if file is javascript
     * @param input full string input document
     * @param tutorial document information
     * @param date date to search for
     * @return parsed string javascript document
     * @throws UnsupportedEncodingException if encoding is not functional
     */
    public static String parseJs(String input, Tutorial tutorial, String date) throws UnsupportedEncodingException {
        String client = "http://118.67.133.84:8085/api/websites?web=";
        if (date != null) client = "http://118.67.133.84:8085/api/websites?date=" + date + "&web=";
        return otherRegex(input, client, tutorial);
    }

    /**
     * implementation of LinkExtractor abstract func
     * @param srcSets elements to replace
     * @throws UnsupportedEncodingException if encoding is unusual
     */
    public void parseSrcSet(Elements srcSets) throws UnsupportedEncodingException {
        if (srcSets == null) return;
        for (Element srcset : srcSets) {
            String hold = srcset.attr("srcset");
            String replace = setHelp(hold, this.client, getTutorial());
            srcset.attr("srcset", replace);
        }
    }

    /**
     * implementation of parseSrc for LinkExtractor
     * @param srcs src elements to replace
     * @throws UnsupportedEncodingException if encoding is unusual
     * @throws MalformedURLException if url is unusual
     * @throws URISyntaxException incorrectly built url
     */
    public void parseSrc(Elements srcs) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        if (srcs == null) return;
        for (Element src : srcs) {
            String hold = src.attr("src");
            if (!hold.startsWith("data:image") && !hold.startsWith(this.client)){
                String newUrl = replaceUrl(hold, getTutorial().getTitle());
                if (newUrl == null) continue;
                src.attr("src", this.client + java.net.URLEncoder.encode(newUrl, StandardCharsets.UTF_8.name()));
            }

        }
    }

    /**
     * implementation of parse data-src for LinkExtractor
     * @param srcs types of data-src elements to replace
     * @throws UnsupportedEncodingException will not be thrown
     * @throws MalformedURLException badly build url
     * @throws URISyntaxException problems in syntax
     */
    public void parsedsrc(Elements srcs) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        if (srcs == null) return;
        for (Element src : srcs) {
            String hold = src.attr("data-src");
            if (!hold.startsWith("data:image") && !hold.startsWith(this.client)) {
                String newUrl = replaceUrl(hold, getTutorial().getTitle());
                if (newUrl == null) continue;
                src.attr("data-src", this.client + java.net.URLEncoder.encode(newUrl, StandardCharsets.UTF_8.name()));
            }
        }
    }

    /**
     * implementation of parse data-src for LinkExtractor
     * @param data types of data- elements to replace
     * @throws UnsupportedEncodingException will not be thrown
     * @throws MalformedURLException badly build url
     * @throws URISyntaxException problems in syntax
     */
    public void parseData(Elements data) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        if (data == null) return;
        for (Element d : data) {
            for (Attribute att : d.attributes().asList()) {
                if (att.getKey().contains("data-") && !att.getKey().equals("data-src")) {
                    String curr = att.getValue();
                    if (Pattern.compile(regex).matcher(curr).find() && !curr.startsWith(this.client)) {
                        String newUrl = replaceUrl(curr, getTutorial().getTitle());
                        if (newUrl != null)
                            att.setValue(this.client + java.net.URLEncoder.encode(newUrl, StandardCharsets.UTF_8.name()));
                    }
                }
            }
        }
    }

    /**
     * implementation of parse background for LinkExtractor - needed for older links
     * @param background src of the background
     * @throws UnsupportedEncodingException will not be thrown
     */
    public void parseBackground(Elements background) throws UnsupportedEncodingException {
        if (background == null) return;
        for (Element b : background) {
            String hold = b.attr("background");
            if (!hold.startsWith("data:image") && !hold.startsWith(this.client)) {
                String newUrl = replaceUrl(hold, getTutorial().getTitle());
                if (newUrl == null) continue;
                b.attr("background", this.client + java.net.URLEncoder.encode(newUrl, StandardCharsets.UTF_8.name()));
            }
        }
    }

    /**
     * implementation of parseHref for LinkExtractor
     * @param hrefs href elements to replace
     * @throws UnsupportedEncodingException if encoding is unusual
     * @throws MalformedURLException if url is unusual
     */
    public void parseLinkLink(Elements hrefs) throws UnsupportedEncodingException, MalformedURLException {
        if (hrefs == null) return;
        for (Element href : hrefs) {
            String hold = href.attr("href");
            if (hold.startsWith(this.client)) continue;
            String newUrl = replaceUrl(hold, getTutorial().getTitle());
            if (newUrl == null) continue;
            href.attr("href", this.client + java.net.URLEncoder.encode(newUrl, StandardCharsets.UTF_8.name()));
        }
    }

    /**
     * implementation of replacement of A links
     * @param hrefs elements to replace
     * @throws UnsupportedEncodingException if encoding is unusal not thrown
     */
    public void parseALink(Elements hrefs) throws UnsupportedEncodingException, URISyntaxException {
        if (hrefs == null) return;
        for (Element href : hrefs) {
            String hold = href.attr("href");
            if (hold.startsWith(this.client)) continue;
            URI uri = new URI(hold);
            String domain = uri.getHost();
            if (blacklist.contains(domain)) continue;

            String newUrl = replaceUrl(hold, getTutorial().getTitle());
            if (newUrl == null) continue;
            href.attr("href", this.client + java.net.URLEncoder.encode(newUrl, StandardCharsets.UTF_8.name()));
        }
    }

    /**
     * Replaying style tags in html files
     * @param style elements to parse
     * @throws JSONException adding tutorial
     * @throws IOException issues with link
     * @throws URISyntaxException error in url
     */
    public void parseStyle(Elements style) throws JSONException, IOException, URISyntaxException {
        if (style == null) return;
        for (Element css: style) {
            String hold = css.attr("style");
            String replace = parseCSS(hold, getTutorial(), this.date);
            css.attr("style", replace);
        }
    }

    /**
     * Replacing other type of style tags
     * @param matcher elements to parse
     * @throws JSONException adding tutorial
     * @throws IOException issues with link
     * @throws URISyntaxException error in url
     */
    public void parseOtherStyle(Matcher matcher) throws IOException, URISyntaxException, JSONException {
        if (matcher == null) return;
        String input = getInput();
        while (matcher.find()) {
            String group = matcher.group(0);
            group = parseCSS(group, getTutorial(), this.date);
            input = input.replace("'" + matcher.group(0) + "'", "\"" + group + "\"");
        }
        setInput(input);
    }

    public void setBlacklistArray(String[] blacklistArray) {
        this.blacklistArray = blacklistArray;
    }
}
