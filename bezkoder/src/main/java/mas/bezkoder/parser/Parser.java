package mas.bezkoder.parser;

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
    private static final String style ="<style([\\s\\S]+?)</style>";

    public Parser(Document document, Tutorial tutorial) {
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
        Parser parser = new Parser(document, tutorial);
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
     * function to parse if document is css
     * @param input complete css string
     * @param tutorial information of css file
     * @return parsed css string
     * @throws IOException if file is missing
     * @throws URISyntaxException if url is unusual
     */

    public static String parseCss(String input, Tutorial tutorial) throws IOException, URISyntaxException {
        Pattern pattern = Pattern.compile(CSSRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            group = group.replaceAll("\"", "");
            group = group.replaceAll("'", "");
            if (group.startsWith("data:")) continue;
            if (group.contains(client)) continue;
            String newUrl = client + java.net.URLEncoder.encode(replaceUrl(group, tutorial.getTitle()), StandardCharsets.UTF_8.name());
            input = input.replace(group, newUrl);
        }

        input = otherRegex(input, tutorial);
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
//        input = input.replace("window.location", "");

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
        else if (tutorial.getFiletype().equals("css")) return parseCss(input, tutorial);
        else if (tutorial.getFiletype().equals("js")) return parseJs(input, tutorial);
        return input;
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
                URI parent = link.getPath().endsWith("/") ? link.resolve("..") : link.resolve(".");
                link = parent;
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

    /**
     * implementation of parseHref for A-links which redirect
     * @param hrefs
     * @throws IOException
     * @throws URISyntaxException
     * @throws JSONException
     */
    public void parseALink(Elements hrefs) throws IOException, URISyntaxException, JSONException {
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
            String replace = parseCss(hold, getTutorial());
            css.attr("style", replace);
        }
    }
    public void parseOtherStyle(Matcher matcher) throws JSONException, IOException, URISyntaxException {
        if (matcher == null) return;
        String input = getInput();
        while (matcher.find()) {
            String group = matcher.group(0);
            group = parseCss(group, getTutorial());
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
        String url = "http://bcbm4y7yusdxthg3.onion/index.php";
        Proxy webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8123));
        Document document = Jsoup.connect(url).proxy(webProxy).get();
        Elements elements = document.select("[background]");
        for (Element b : elements) {
            String slink = b.attr("background");
            if (!slink.startsWith("data:image")) {
                slink = Parser.replaceUrl(slink, url);
                System.out.println(slink);
            }
        }

    }
}
