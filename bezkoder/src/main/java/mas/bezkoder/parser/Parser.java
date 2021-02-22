package mas.bezkoder.parser;

import mas.bezkoder.model.Tutorial;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Parser {

    private static final String client = "http://localhost:8085/api/websites?web=";
    private static final String CSSRegex = "url\\((.*?)\\)";
    private static final String href = "href=\"([^\"]*)\"";
    private static final String src = "src=\"([^\"]*)\"";
    private static final String href1 = "href='([^']*)'";
    private static final String src1 = "src='([^']*)'";
    private static final String otherRegex = "https?://([^\"'\\s)]*)";


    /**
     * function to parse if document is html
     * @param input String input of entire document
     * @param tutorial gives document information
     * @return parsed string input of html
     * @throws IOException if something is missing
     * @throws URISyntaxException if parsed url is unusual
     */
    public static String parseHtml(String input, Tutorial tutorial) throws IOException, URISyntaxException {
        Document document = Jsoup.parse(input);
        input = document.toString();

        Pattern pattern;
        Matcher matcher;
        //src files
        Elements srcs = document.select("[src]");

        for (Element src : srcs) {
            String hold = src.toString();
            String replace = parseSrc(hold, tutorial);
            input = input.replace(hold, replace);
        }

        //href links
        Elements hrefs = document.select("[href]");

        for (Element href : hrefs) {
            String hold = href.toString();
            String replace = parseHref(hold, tutorial);
            input = input.replace(hold, replace);
        }

        pattern = Pattern.compile(CSSRegex);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            group = group.replaceAll("\"", "");
            group = group.replaceAll("\'", "");
            if (group.startsWith("data:")) continue;
            if (group.contains("localhost")) continue;
            String newUrl = replaceUrl(group, tutorial);
            input = input.replace(group, newUrl);
        }

        return input;
    }

    private static String otherRegex(String input, Tutorial tutorial) throws URISyntaxException, UnsupportedEncodingException {
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(otherRegex);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            if (group.startsWith(client)) continue;
            if (group.startsWith("data:")) continue;
            String newUrl = replaceUrl(group, tutorial);
            try {
                input = input.replace("'" + group, "'" + newUrl);
                input = input.replace("\"" + group, "\"" + newUrl);
            } catch (Exception ignored) {
            }
        }
        return input;
    }

    public static String parseSrc(String input, Tutorial tutorial) throws UnsupportedEncodingException, URISyntaxException {
        Pattern pattern;
        Matcher matcher;
        String string = input;
        //src files
        pattern = Pattern.compile(src);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group.startsWith("data:")) continue;
            if (group.startsWith(client)) continue;
            String newUrl = replaceUrl(matcher.group(1), tutorial);
            string = input.replace("\"" + group + "\"", "\"" + newUrl + "\"");
        }

        pattern = Pattern.compile(src1);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group.startsWith("data:")) continue;
            if (group.startsWith(client)) continue;
            String newUrl = replaceUrl(matcher.group(1), tutorial);
            string = input.replace("'" + group + "'", "'" + newUrl + "'");
        }

        string = otherRegex(string, tutorial);

        return string;
    }

    public static String parseHref(String input, Tutorial tutorial) throws UnsupportedEncodingException, URISyntaxException {
        Pattern pattern = Pattern.compile(href);
        Matcher matcher = pattern.matcher(input);
        String string = input;
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group.startsWith("data:")) continue;
            if (group.startsWith(client)) continue;
            String newUrl = replaceUrl(group, tutorial);
            string = input.replace("\"" + group + "\"", "\"" + newUrl + "\"");
        }
        pattern = Pattern.compile(href1);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            if (group.startsWith(client)) continue;
            if (group.startsWith("data:")) continue;
            String newUrl = replaceUrl(matcher.group(1), tutorial);
            string = input.replace("'" + group + "'", "'" + newUrl + "\'");
        }
        return string;
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
        Pattern pattern = Pattern.compile(CSSRegex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            group = group.replaceAll("\"", "");
            group = group.replaceAll("\'", "");
            if (group.startsWith("data:")) continue;
            if (group.contains("localhost")) continue;
            String newUrl = replaceUrl(group, tutorial);
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
    public static String parseJs(String input, Tutorial tutorial) throws UnsupportedEncodingException, URISyntaxException {
        Pattern pattern = Pattern.compile(otherRegex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            if (group.contains("localhost")) continue;
            String newUrl = replaceUrl(group, tutorial);
            input = input.replace(group, newUrl);
        }

        return input;
    }

    /**
     * overall file parsing function
     * @param input string input of file
     * @param tutorial file information
     * @return String of parsed file
     * @throws URISyntaxException if helper take incorrect urls
     * @throws IOException if information is missing
     */

    public static String parseFile(String input, Tutorial tutorial) throws URISyntaxException, IOException {
        if (tutorial.getFiletype().equals("html")) {
            return parseHtml(input, tutorial);
        }
        else if (tutorial.getFiletype().equals("css")) return parseCss(input, tutorial);
        else if (tutorial.getFiletype().equals("js")) return parseJs(input, tutorial);
        return input;
    }

    /**
     * helper function to take url and accordingly change based on file info
     * @param url input url
     * @param tutorial information for changing url
     * @return new url
     * @throws URISyntaxException if input string is incorrectly built
     * @throws UnsupportedEncodingException if encoding of string is non functional
     */

    public static String replaceUrl(String url, Tutorial tutorial) throws URISyntaxException, UnsupportedEncodingException {
        if (url.contains("localhost")) return url;
        String strFind = "../";
        int count = 0, fromIndex = 0;
        while ((fromIndex = url.indexOf(strFind, fromIndex)) != -1 ){
            count++;
            fromIndex++;
        }
        String newUrl;

        //fixing paths
        newUrl = getUrlFromPath(url, tutorial, count);
        return client + newUrl;
    }

    /**
     * analyze and change url based on relative path
     * @param url input path
     * @param tutorial information to adjust url
     * @param count number of backtracks
     * @return full absolute path
     * @throws URISyntaxException
     */

    private static String getUrlFromPath(String url, Tutorial tutorial, int count) throws URISyntaxException {
        if (url.contains("localhost")) return url;
        String newUrl;
        if (url.startsWith("http")) {
            newUrl = url;
        } else if (url.startsWith("#")) {
            newUrl = tutorial.getTitle() + url;
        } else if (url.startsWith("//")) {
            newUrl = "https:" + url;
        }
        else if (url.startsWith("/")) {
            URI link = new URI(tutorial.getTitle());
            newUrl = link.getScheme() + "://" + tutorial.getDomain() + url;
        } else if (url.startsWith("./")) {
            URI parent = new URI(tutorial.getTitle());
            parent = parent.getPath().endsWith("/") ? parent.resolve("..") : parent.resolve(".");
            newUrl = parent.toString() + url.substring(2);
        } else if (url.startsWith("../")) {
            URI link = new URI(tutorial.getTitle());
            for (int i = 0; i <= count; i++) {
                URI parent = link.getPath().endsWith("/") ? link.resolve("..") : link.resolve(".");
                link = parent;
            }
            newUrl = link.toString() + url.substring(3 * count);
        } else {
            URI parent = new URI(tutorial.getTitle());
            parent = parent.getPath().endsWith("/") ? parent.resolve("..") : parent.resolve(".");
            newUrl = parent.toString() + url;
        }
        return newUrl;
    }

}
