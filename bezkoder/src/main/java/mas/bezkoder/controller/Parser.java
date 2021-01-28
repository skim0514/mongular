package mas.bezkoder.controller;

import mas.bezkoder.model.Tutorial;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static String files = "http://localhost:8082/";
    private static String db = "http://localhost:72017/";
    private static String spring = "http://localhost:8080/";
    private static String client = "http://localhost:8081/api/websites/";
    private static String ahref = "<a [^>]*href=\"[^\"]*\"[^>]*>";
    private static String lhref = "<link [^>]*href=\"[^\"]*\"[^>]*>";
    private static String href = "href=\"([^\"]*)\"";
    private static String src = "src=\"([^\"]*)\"";

    public static String parseHtml(String input, Tutorial tutorial) throws URISyntaxException, UnsupportedEncodingException {
        //src files
        Pattern pattern = Pattern.compile(src);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            String newUrl = replaceUrlSrc(group, tutorial);
            group = "\"" + group + "\"";
            newUrl = "\"" + newUrl + "\"";
            input = input.replaceAll(group, newUrl);
        }

        //href links
        pattern = Pattern.compile(href);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            String newUrl = replaceUrlHref(group, tutorial);
            group = "\"" + group + "\"";
            newUrl = "\"" + newUrl + "\"";
            input = input.replaceAll(group, newUrl);
        }
        return input;
    }

    public static String parseCss(String input, Tutorial tutorial) {
        return input;
    }

    public static String parseJs(String input, Tutorial tutorial) {
        return input;
    }

    public static String parseFile(String input, Tutorial tutorial) throws URISyntaxException, UnsupportedEncodingException {
        if (tutorial.getFiletype() == "html") return parseHtml(input, tutorial);
        else if (tutorial.getFiletype() == "css") return parseCss(input, tutorial);
        else if (tutorial.getFiletype() == "js") return parseJs(input, tutorial);
        return input;
    }

    public static String replaceUrlSrc(String url, Tutorial tutorial) throws URISyntaxException {
        URI uri = new URI(url);
        //finding backtracks
        String strFind = "../";
        int count = 0, fromIndex = 0;
        while ((fromIndex = url.indexOf(strFind, fromIndex)) != -1 ){
            System.out.println("Found at index: " + fromIndex);
            count++;
            fromIndex++;
        }
        Tutorial urlTut = null;


        //fixing paths
        String newUrl = getUrlFromPath(url, tutorial, uri, count);

        urlTut = TutorialController.getTutorial(newUrl);
        return files + urlTut.getId() + urlTut.getFiletype();
    }

    public static String replaceUrlHref(String url, Tutorial tutorial) throws URISyntaxException, UnsupportedEncodingException {
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
        newUrl = java.net.URLEncoder.encode(newUrl, "UTF-8");
        return client + newUrl;
    }

    private static String getUrlFromPath(String url, Tutorial tutorial, URI uri, int count) {
        String newUrl;
        if (url.startsWith("http")) {
            newUrl = url;
        } else if (url.startsWith("/")) {
            newUrl = tutorial.getDomain() + url;
        } else if (url.startsWith("./")) {
            URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
            newUrl = parent.toString() + url.substring(2);
        } else if (url.startsWith("../")) {
            URI parent = uri;
            for (int i = 0; i <= count; i++) {
                parent = parent.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
            }
            newUrl = parent.toString() + url.substring(3 * count);
        } else {
            URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
            newUrl = parent.toString() + url.substring(2);
        }
        return newUrl;
    }


//    public void setTutorial(Tutorial tutorial) {
//        this.tutorial = tutorial;
//    }
//
//    public Tutorial getTutorial() {
//        return this.tutorial;
//    }
//
//    public static



//    public static String toAbsolute(String input) {
//        input = domain + input;
//
//
//    }
}
