package mas.bezkoder.parser;

import mas.bezkoder.LinkExtractor.CSSExtractor;
import mas.bezkoder.model.Tutorial;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

import static mas.bezkoder.LinkExtractor.HTMLExtractor.replaceUrl;

public class ParseCSS extends CSSExtractor {
    protected static final String clientStart = "http://118.67.133.84:8085/api/websites?";
//    private static final String client = "http://localhost:8085/api/websites?web=";

    public ParseCSS(String input, Tutorial tutorial, String date) {
        super(input, tutorial, date);
    }

    public static String parseCSS(String input, Tutorial tutorial, String date) throws JSONException, IOException, URISyntaxException {
        ParseCSS css = new ParseCSS(input, tutorial, date);
        css.extractCSS();
        return css.getInput();
    }

    @Override
    public void parseURL(Matcher matcher) throws IOException {
        String input = getInput();
        Tutorial tutorial = getTutorial();
        while (matcher.find()) {
            String group = matcher.group(1);
            group = group.replaceAll("\"", "");
            group = group.replaceAll("'", "");
            if (group.startsWith("data:")) continue;
            if (group.contains(clientStart)) continue;
            String newUrl = this.client + java.net.URLEncoder.encode(replaceUrl(group, tutorial.getTitle()), StandardCharsets.UTF_8.name());
            input = input.replace("'" + group + "'", "'" + newUrl + "'");
            input = input.replace("\"" + group + "\"", "\"" + newUrl + "\"");
        }
        setInput(input);
    }

    @Override
    public void parseOther(Matcher matcher) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        String input = getInput();
        Tutorial tutorial = getTutorial();
        while (matcher.find()) {
            String group = matcher.group(0);
            if (group.startsWith(clientStart)) continue;
            if (group.startsWith("data:")) continue;
            String newUrl = this.client + java.net.URLEncoder.encode(replaceUrl(group, tutorial.getTitle()), StandardCharsets.UTF_8.name());
            try {
                input = input.replace("'" + group + "'", "'" + newUrl + "'");
                input = input.replace("\"" + group + "\"", "\"" + newUrl + "\"");
            } catch (Exception ignored) {
            }
        }
        setInput(input);
    }
}
