package mas.bezkoder.parser;

import mas.bezkoder.model.Tutorial;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;

import static mas.bezkoder.parser.ParseCSS.parseCSS;
import static mas.bezkoder.parser.ParseHTML.parseHtml;
import static mas.bezkoder.parser.ParseHTML.parseJs;

public class ParseMain {
    /**
     * overall file parsing function
     * @param input string input of file
     * @param tutorial file information
     * @return String of parsed file
     * @throws URISyntaxException if helper take incorrect urls
     * @throws IOException if information is missing
     */

    public static String parseFile(String input, Tutorial tutorial) throws URISyntaxException, IOException, JSONException {
        switch (tutorial.getFiletype()) {
            case "html":
                return parseHtml(input, tutorial);
            case "css":
                return parseCSS(input, tutorial);
            case "js":
                return parseJs(input, tutorial);
        }
        return input;
    }
}
