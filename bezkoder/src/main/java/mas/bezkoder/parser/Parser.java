package mas.bezkoder.parser;

import mas.bezkoder.crawler.Crawler;
import mas.bezkoder.model.Tutorial;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final String client = "http://localhost:8085/api/websites?web=";
    private static final String CSSRegex = "url\\((.*?)\\)";
    private static final String htmlTag = "<(?!!)(?!/)\\s*([a-zA-Z0-9]+)(.*?)>";
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";
    private static final String style ="<style([\\s\\S]+?)</style>";

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

        Elements srcsets = document.select("[srcset]");
        for (Element srcset : srcsets) {
            String hold = srcset.attr("srcset");
            String replace = parseSrcset(hold, tutorial);
            srcset.attr("srcset", replace);
        }
        //src and href files


        Elements srcs = document.select("[src]");
        for (Element src : srcs) {
            String hold = src.attr("src");
            if (!hold.startsWith("data:image") && !hold.startsWith(client))
                src.attr("src", client + replaceUrl(hold, tutorial.getTitle()));
        }

        Elements hrefs = document.select("[href]");
        for (Element href : hrefs) {
            String hold = href.attr("href");
            href.attr("href", client + replaceUrl(hold, tutorial.getTitle()));
        }

        //css styling within attributes
        Elements elements = document.select("[style]");
        for (Element css: elements) {
            String hold = css.attr("style");
            String replace = parseCss(hold, tutorial);
            css.attr("style", replace);
        }

        input = document.toString();

        //css styling as a separate attribute
        Pattern pattern = Pattern.compile(style);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            group = parseCss(group, tutorial);
            input = input.replace(matcher.group(0), group);
        }

        pattern = Pattern.compile(htmlTag);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            group = otherRegex(group, tutorial);
            input = input.replace(matcher.group(0), group);
        }
        //all other urls starting with https?


        return input;
    }

    private static String parseSrcset(String hold, Tutorial tutorial) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException {
        String rs = hold;
        String[] strings;
        strings = hold.split(", ");
        for (String string: strings) {
            String[] urls = string.split(" ");
            String url = urls[0];
            rs = rs.replace(url, client + replaceUrl(url, tutorial.getTitle()));
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
    private static String otherRegex(String input, Tutorial tutorial) throws URISyntaxException, UnsupportedEncodingException, MalformedURLException {
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(otherRegex);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            if (group.startsWith(client)) continue;
            if (group.startsWith("data:")) continue;
            String newUrl = client + replaceUrl(group, tutorial.getTitle());
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
            if (group.contains("localhost")) continue;
            String newUrl = client + replaceUrl(group, tutorial.getTitle());
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

    public static String parseFile(String input, Tutorial tutorial) throws URISyntaxException, IOException {
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
     * @throws UnsupportedEncodingException if encoding of string is non functional
     */

    public static String replaceUrl(String url, String string) throws URISyntaxException, UnsupportedEncodingException, MalformedURLException {
        if (url.contains("localhost")) return url;
        String strFind = "../";
        int count = 0, fromIndex = 0;
        while ((fromIndex = url.indexOf(strFind, fromIndex)) != -1 ){
            count++;
            fromIndex++;
        }
        String newUrl;

        //fixing paths
        newUrl = getUrlFromPath(url, string, count);
        newUrl = java.net.URLEncoder.encode(newUrl, StandardCharsets.UTF_8.name());
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
            newUrl = title + url;
        } else if (url.startsWith("//")) {
            newUrl = "https:" + url;
        }
        else if (url.startsWith("/")) {
            URI link = new URI(title);
            newUrl = link.getScheme() + "://" + domain + url;
        } else if (url.startsWith("./")) {
            URI parent = new URI(title);
            parent = parent.getPath().endsWith("/") ? parent.resolve("..") : parent.resolve(".");
            newUrl = parent.toString() + url.substring(2);
        } else if (url.startsWith("../")) {
            URI link = new URI(title);
            for (int i = 0; i <= count; i++) {
                URI parent = link.getPath().endsWith("/") ? link.resolve("..") : link.resolve(".");
                link = parent;
            }
            newUrl = link.toString() + url.substring(3 * count);
        } else {
            URI parent = new URI(title);
            parent = parent.getPath().endsWith("/") ? parent.resolve("..") : parent.resolve(".");
            newUrl = parent.toString() + url;
        }
        return newUrl;
    }

    public static void main (String[] args) throws IOException {
        URL weburl = new URL("http://wiki22djp7zzzv4x3nimnl6jnghbfddac4e4dce5jkb6544h7e7kzfad.onion/#!index.md");

        Proxy webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.41.228.165", 8123));


//

//
//        System.out.println(webProxyConnection.getContentType());
//        System.out.println(webProxyConnection.getResponseCode());
//        ReadableByteChannel readChannel;
//        readChannel = Channels.newChannel(webProxyConnection.getInputStream());
//        FileOutputStream fileOS = new FileOutputStream("/Users/skim/s2w/mongular/bezkoder/cde");
//        FileChannel writeChannel = fileOS.getChannel();
//        writeChannel
//                .transferFrom(readChannel, 0, Long.MAX_VALUE);

//        System.out.println(Jsoup.connect("https://finance.naver.com/news/news_read.nhn?mode=mainnews&office_id=008&article_id=0004550654").get().toString());
    }
}
