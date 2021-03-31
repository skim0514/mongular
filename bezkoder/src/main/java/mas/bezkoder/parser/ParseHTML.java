package mas.bezkoder.parser;

import mas.bezkoder.LinkExtractor.HTMLExtractor;
import mas.bezkoder.model.Tutorial;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mas.bezkoder.parser.ParseCSS.parseCSS;

public class ParseHTML extends HTMLExtractor {

//    private static final String client = "http://localhost:8085/api/websites?web=";
    private static final String client = "http://118.67.133.84:8085/api/websites?web=";
    private static final String CSSRegex = "url\\((.*?)\\)";
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";

    public ParseHTML(Document document, Tutorial tutorial) {
        super(document, tutorial);
    }

    /**
     * function to parse if document is html
     * @param input String input of entire document
     * @param tutorial gives document information
     * @return parsed string input of html
     * @throws IOException if something is missing
     * @throws URISyntaxException if parsed url is unusual
     */
    public static String parseHtml(String input, Tutorial tutorial) throws IOException, URISyntaxException, JSONException {

        Document document = Jsoup.parse(input);
        ParseHTML parser = new ParseHTML(document, tutorial);
        parser.extractHtml();
        return parser.getInput();
    }

    public static String setHelp(String hold, Tutorial tutorial) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException {
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
     * @throws URISyntaxException if urls found have issues
     * @throws UnsupportedEncodingException if encoding of text is unusual
     */
    public static String otherRegex(String input, Tutorial tutorial) throws URISyntaxException, UnsupportedEncodingException, MalformedURLException {
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(otherRegex);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            if (group.startsWith(client)) continue;
            if (group.startsWith("data:")) continue;
            String newUrl = client + java.net.URLEncoder.encode(replaceUrl(group, tutorial.getTitle()), StandardCharsets.UTF_8.name());
            try {
                input = input.replace("'" + group, "'" + newUrl);
                input = input.replace("\"" + group, "\"" + newUrl);
            } catch (Exception ignored) {
            }
        }
        return input;
    }

    /**
     * parser if file is javascript
     * @param input full string input document
     * @param tutorial document information
     * @return parsed string javascript document
     * @throws UnsupportedEncodingException if encoding is not functional
     * @throws URISyntaxException if urls to parse is unusual
     */
    public static String parseJs(String input, Tutorial tutorial) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException {
        return otherRegex(input, tutorial);
    }

    /**
     * implementation of LinkExtractor abstract func
     * @param srcSets elements to replace
     * @throws UnsupportedEncodingException if encoding is unusual
     * @throws MalformedURLException if url is unusual
     * @throws URISyntaxException incorrectly built url
     */
    public void parseSrcSet(Elements srcSets) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        if (srcSets == null) return;
        for (Element srcset : srcSets) {
            String hold = srcset.attr("srcset");
            String replace = setHelp(hold, getTutorial());
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
            if (!hold.startsWith("data:image") && !hold.startsWith(client))
                src.attr("src", client + java.net.URLEncoder.encode(replaceUrl(hold, getTutorial().getTitle()), StandardCharsets.UTF_8.name()));
        }
    }

    public void parseBackground(Elements background) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        if (background == null) return;
        for (Element b : background) {
            String hold = b.attr("background");
            if (!hold.startsWith("data:image") && !hold.startsWith(client))
                b.attr("background", client + java.net.URLEncoder.encode(replaceUrl(hold, getTutorial().getTitle()), StandardCharsets.UTF_8.name()));
        }
    }

    /**
     * implementation of parseHref for LinkExtractor
     * @param hrefs href elements to replace
     * @throws UnsupportedEncodingException if encoding is unusual
     * @throws MalformedURLException if url is unusual
     * @throws URISyntaxException incorrectly built url
     */
    public void parseLinkLink(Elements hrefs) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        if (hrefs == null) return;
        for (Element href : hrefs) {
            String hold = href.attr("href");
            href.attr("href", client + java.net.URLEncoder.encode(replaceUrl(hold, getTutorial().getTitle()), StandardCharsets.UTF_8.name()));
        }
    }

    public void parseALink(Elements hrefs) throws IOException, URISyntaxException {
        if (hrefs == null) return;
        for (Element href : hrefs) {
            String hold = href.attr("href");
            href.attr("href", client + java.net.URLEncoder.encode(replaceUrl(hold, getTutorial().getTitle()), StandardCharsets.UTF_8.name()));
        }
    }
    public void parseStyle(Elements style) throws JSONException, IOException, URISyntaxException {
        if (style == null) return;
        for (Element css: style) {
            String hold = css.attr("style");
            String replace = parseCSS(hold, getTutorial());
            css.attr("style", replace);
        }
    }
    public void parseOtherStyle(Matcher matcher) throws IOException, URISyntaxException, JSONException {
        if (matcher == null) return;
        String input = getInput();
        while (matcher.find()) {
            String group = matcher.group(0);
            group = parseCSS(group, getTutorial());
            input = input.replace(matcher.group(0), group);
        }
        setInput(input);
    }

    public void parseOther(Matcher matcher) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        if (matcher == null) return;
        String input = getInput();
        while (matcher.find()) {
            String group = matcher.group(0);
            group = otherRegex(group, getTutorial());
            input = input.replace(matcher.group(0), group);
        }
        setInput(input);
    }

    public static void main (String[] args) throws IOException, URISyntaxException, JSONException {
        Proxy webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8123));
        String website = "http%3A%2F%2F3dell3phmthpcqw3w4lw5fbabrqpxh4ur5pnopspwx4ifeynufaynxid.onion%2Fcommunity%2F%EC%84%9C%EB%B9%84%EC%8A%A4-%EC%9E%A5%EC%95%A0%EC%8B%A0%EA%B3%A0-%EC%84%9C%EB%B9%84%EC%8A%A4-%EC%9E%A5%EC%95%A0%EC%8B%A0%EA%B3%A0%2F";
        String url = "";
        while (true) {
            url = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
            if (url.equals(website)) break;
            else website = url;
        }
        System.out.println(website);
//        Document document = Jsoup.connect(url).proxy(webProxy).get();
//        System.out.println(document.toString());

    }
}
