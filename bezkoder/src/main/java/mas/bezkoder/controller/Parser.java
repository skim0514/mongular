package mas.bezkoder.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;

public class Parser {
    public static String parseHtml(String input) {
//        var a_pattern = /<a [^>]*href="[^"]*"[^>]*>/gm
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
