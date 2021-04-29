package mas.bezkoder.parser;

import mas.bezkoder.model.Tutorial;
import org.json.JSONException;

import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;

import static mas.bezkoder.parser.ParseCSS.parseCSS;
import static mas.bezkoder.parser.ParseHTML.*;

public class ParseMain {
    /**
     * overall file parsing function
     * @param input string input of file
     * @param tutorial file information
     * @param date date to search for
     * @return String of parsed file
     * @throws URISyntaxException if helper take incorrect urls
     * @throws IOException if information is missing
     */

    public static String parseFile(String input, Tutorial tutorial, String date) throws URISyntaxException, IOException, JSONException, ScriptException, NoSuchMethodException {
        switch (tutorial.getFiletype()) {
            case "html":
                return parseHtml(input, tutorial, date);
            case "css":
                return parseCSS(input, tutorial, date);
            default:
                return parseJs(input, tutorial, date);

        }
    }
}
