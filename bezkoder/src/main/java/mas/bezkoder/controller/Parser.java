package mas.bezkoder.controller;

import mas.bezkoder.model.Tutorial;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static String files = "http://localhost:8082/";
    private static String db = "http://localhost:72017/";
    private static String spring = "http://localhost:8080/";
    private static String client = "http://localhost:8081/websites/";
    private static String ahref = "<a [^>]*href=\"[^\"]*\"[^>]*>";
    private static String lhref = "<link [^>]*href=\"[^\"]*\"[^>]*>";
    private static String href = "href=\"([^\"]*)\"";
    private static String src = "src=\"([^\"]*)\"";

    public static String parseHtml(String input, Tutorial tutorial) throws URISyntaxException {
        //src files
        Pattern pattern = Pattern.compile(src);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            String newUrl = replaceUrlSrc(group, tutorial);
            input = input.replaceAll(group, files + group);
        }

        //href links
        pattern = Pattern.compile(ahref);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            String line = replaceLine(group);
            input = input.replaceAll(group, line);
        }
        pattern = Pattern.compile(lhref);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            Pattern phref = Pattern.compile(href);
            Matcher m2 = phref.matcher(group);
            if (m2.find()) {
                group = m2.group(1);
                if (group.startsWith("http")) continue;
                else input = input.replaceAll(group, files + group);
            }
        }

        return input;
    }

    public static String parseCss(String input, Tutorial tutorial) {
        return input;
    }

    public static String parseJs(String input, Tutorial tutorial) {
        return input;
    }

    public static String parseFile(String input, Tutorial tutorial) throws URISyntaxException {
        if (tutorial.getFiletype() == "html") return parseHtml(input, tutorial);
        else if (tutorial.getFiletype() == "css") return parseCss(input, tutorial);
        else if (tutorial.getFiletype() == "js") return parseJs(input, tutorial);
        return input;
    }

    public static String replaceLine(String line) {
        Pattern h = Pattern.compile(href);
        Matcher m = h.matcher(line);
        m.find();
        String g = m.group(1);
        if (g.startsWith("http")) return line;
        String domain = "www.s2wlab.com_";
        String replace = domain + g;
        line = line.replaceAll(g, client + replace);
        return line;
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

        String newUrl = "";

        if (url.startsWith("http")) {
            urlTut = TutorialController.getTutorial(url);
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

        urlTut = TutorialController.getTutorial(newUrl);
        return files + urlTut.getId() + urlTut.getFiletype();
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
