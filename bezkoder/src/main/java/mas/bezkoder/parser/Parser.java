package mas.bezkoder.parser;

import mas.bezkoder.model.Tutorial;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final String client = "http://118.67.133.84:8085/api/websites?web=";
    private static final String href = "href=\"([^\"]*)\"";
    private static final String src = "src=\"([^\"]*)\"";
    private static final String CSSRegex = "url\\((.*?)\\)";

    public static String parseHtml(String input, Tutorial tutorial) throws URISyntaxException, IOException {
        Pattern pattern;
        Matcher matcher;
        //src files
        pattern = Pattern.compile(src);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            String newUrl = replaceUrl(matcher.group(1), tutorial);
            input = input.replaceAll("\"" + group + "\"", "\"" + newUrl + "\"");
        }

        //href links
        pattern = Pattern.compile(href);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            String newUrl = replaceUrl(group, tutorial);
            input = input.replaceAll("\"" + group + "\"", "\"" + newUrl + "\"");
        }
        return input;
    }

    public static String parseCss(String input, Tutorial tutorial) throws IOException, URISyntaxException {
        Pattern pattern = Pattern.compile(CSSRegex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            group = group.replace("\"", "");
            String newUrl = replaceUrl(group, tutorial);
            System.out.println(group + " " + newUrl);
            input = input.replaceAll(group, newUrl);
        }
        return input;

    }

    public static String parseJs(String input, Tutorial tutorial) {
        return input;
    }

    public static String parseFile(String input, Tutorial tutorial) throws URISyntaxException, IOException {
        System.out.println(tutorial.getFiletype());
        if (tutorial.getFiletype().equals("html")) return parseHtml(input, tutorial);
        else if (tutorial.getFiletype().equals("css")) return parseCss(input, tutorial);
        else if (tutorial.getFiletype().equals("js")) return parseJs(input, tutorial);
        return input;
    }

    public static String replaceUrl(String url, Tutorial tutorial) throws URISyntaxException, UnsupportedEncodingException {
        if (url.startsWith("http://localhost")) return url;
        URI uri = new URI(url);
        String strFind = "../";
        int count = 0, fromIndex = 0;
        while ((fromIndex = url.indexOf(strFind, fromIndex)) != -1 ){
            System.out.println("Found at index: " + fromIndex);
            count++;
            fromIndex++;
        }
        String newUrl;

        //fixing paths
        newUrl = getUrlFromPath(url, tutorial, uri, count);
        return client + newUrl;
    }

    private static String getUrlFromPath(String url, Tutorial tutorial, URI uri, int count) throws URISyntaxException {
        String newUrl;
        if (url.startsWith("http")) {
            newUrl = url;
        } else if (url.startsWith("#")) {
            newUrl = tutorial.getTitle() + url;
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
