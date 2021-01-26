package mas.bezkoder.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static String parseHtml(String input) {
        String href = "href=\"([^\"]*)\"";
        String src = "src=\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(src);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            input = input.replaceAll(group, "http://localhost:8082/" + group);
        }
        pattern = Pattern.compile(href);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            input = input.replaceAll(group, "http://localhost:8082/" + group);
        }

        return input;
    }

    public static String parseCss(String input) {
        return input;
    }

    public static String parseJs(String input) {
        return input;
    }

    public static String parseFile(String input, String filetype) {
        if (filetype == "html") return parseHtml(input);
        else if (filetype == "css") return parseCss(input);
        else if (filetype == "js") return parseJs(input);
        return input;
    }
}
