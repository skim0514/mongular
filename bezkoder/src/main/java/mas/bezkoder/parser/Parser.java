package mas.bezkoder.parser;

import mas.bezkoder.linkExtract.LinkExtractor;
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

public class Parser extends LinkExtractor {

//    private static final String client = "http://localhost:8085/api/websites?web=";
    private static final String client = "http://118.67.133.84:8085/api/websites?web=";
    private static final String CSSRegex = "url\\((.*?)\\)";
    private static final String htmlTag = "<(?!!)(?!/)\\s*([a-zA-Z0-9]+)(.*?)>";
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";

    /**
     * constructor to build Parser object for each individual website
     * @param document current jsoup document to parse
     * @param tutorial information about document
     */
    public Parser(Document document, Tutorial tutorial) {
        super(document, tutorial);
    }

    public Parser(String input, Tutorial tutorial) {
        super(input, tutorial);
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
        Parser parser = new Parser(document, tutorial);
        parser.extractHtml();
        return parser.getInput();
    }

    /**
     * helper function for srcset Parser
     * @param hold URls holding all srcsets
     * @param tutorial information about current website
     * @return new srcset style html attribute
     * @throws UnsupportedEncodingException badly encoded url
     * @throws URISyntaxException incorrect url syntax
     * @throws MalformedURLException badly formed url
     */
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
     * function to parse if document is css
     * @param input complete css string
     * @param tutorial information of css file
     * @return parsed css string
     * @throws IOException if file is missing
     * @throws URISyntaxException if url is unusual
     */

    public static String parseCssDoc(String input, Tutorial tutorial) throws IOException, URISyntaxException {
        Parser cssParser = new Parser(input, tutorial);
        cssParser.extractCSS();
//        Pattern pattern = Pattern.compile(CSSRegex, Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(input);
//        while (matcher.find()) {
//            String group = matcher.group(1);
//            group = group.replaceAll("\"", "");
//            group = group.replaceAll("'", "");
//            if (group.startsWith("data:")) continue;
//            if (group.contains(client)) continue;
//            String newUrl = client + java.net.URLEncoder.encode(replaceUrl(group, tutorial.getTitle()), StandardCharsets.UTF_8.name());
//            input = input.replace(group, newUrl);
//        }
//
//        input = otherRegex(input, tutorial);
        return cssParser.getInput();
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
     * overall file parsing function
     * @param input string input of file
     * @param tutorial file information
     * @return String of parsed file
     * @throws URISyntaxException if helper take incorrect urls
     * @throws IOException if information is missing
     */

    public static String parseFile(String input, Tutorial tutorial) throws URISyntaxException, IOException, JSONException {
        if (tutorial.getFiletype().equals("html")) return parseHtml(input, tutorial);
        else if (tutorial.getFiletype().equals("css")) return parseCssDoc(input, tutorial);
        else if (tutorial.getFiletype().equals("js")) return parseJs(input, tutorial);
        return input;
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

    /**
     * implementation of parseBackground for LinkExtractor
     * @param background background elements to replace for old htmls
     * @throws UnsupportedEncodingException if encoding is unusual
     * @throws MalformedURLException if url is unusual
     * @throws URISyntaxException incorrectly built url
     */
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

    /**
     * implementation of parseHref for A-links which redirect
     * @param hrefs links that redirect to another page
     * @throws IOException errors happening while replacing url
     * @throws URISyntaxException issues in building url
     * @throws JSONException issues while retrieving file information
     */
    public void parseALink(Elements hrefs) throws IOException, URISyntaxException, JSONException {
        if (hrefs == null) return;
        for (Element href : hrefs) {
            String hold = href.attr("href");
            href.attr("href", client + java.net.URLEncoder.encode(replaceUrl(hold, getTutorial().getTitle()), StandardCharsets.UTF_8.name()));
        }
    }

    /**
     * implementation of style tags which having different sources connected
     * @param style links from html embedded css
     * @throws IOException errors happening while replacing url
     * @throws URISyntaxException issues in building url
     * @throws JSONException issues while retrieving file information
     */
    public void parseStyle(Elements style) throws JSONException, IOException, URISyntaxException {
        if (style == null) return;
        for (Element css: style) {
            String hold = css.attr("style");
            String replace = parseCssDoc(hold, getTutorial());
            css.attr("style", replace);
        }
    }

    /**
     * implementation of other format of css embedded in html
     * @param matcher urls retrieved using regex
     * @throws IOException errors happening while replacing url
     * @throws URISyntaxException issues in building url
     */
    public void parseOtherStyle(Matcher matcher) throws IOException, URISyntaxException {
        if (matcher == null) return;
        String input = getInput();
        while (matcher.find()) {
            String group = matcher.group(0);
            group = parseCssDoc(group, getTutorial());
            input = input.replace(matcher.group(0), group);
        }
        setInput(input);
    }

    /**
     * parser for other urls that can be placed in css, including js
     * @param matcher urls retrieved using regex
     * @throws UnsupportedEncodingException badly encoded url
     * @throws MalformedURLException badly formed urls
     * @throws URISyntaxException error parsing url
     */
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

    public void parseCSS(Matcher matcher) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
        String input = getInput();
        while (matcher.find()) {
            String group = matcher.group(1);
            group = group.replaceAll("\"", "");
            group = group.replaceAll("'", "");
            if (group.startsWith("data:")) continue;
            if (group.contains(client)) continue;
            String newUrl = client + java.net.URLEncoder.encode(replaceUrl(group, getTutorial().getTitle()), StandardCharsets.UTF_8.name());
            input = input.replace(group, newUrl);
        }
        setInput(input);
    }

    public static void main (String[] args) throws IOException, URISyntaxException, JSONException {
        String url = "http://bcbm4y7yusdxthg3.onion/";
        URI uri = new URI(url);
        System.out.println(replaceUrl("./okay", url));

    }
}
