package mas.bezkoder.crawler;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.asynchttpclient.*;

public class Crawler {

    private static final int MAX_DEPTH = 2;
    private HashSet<String> links;

    public Crawler() {
        links = new HashSet<String>();
    }

    public void getPageLinks(String URL, int depth) {
        if ((!links.contains(URL) && (depth < MAX_DEPTH))) {
            System.out.println(">> Depth: " + depth + " [" + URL + "]");
            try {
                links.add(URL);

                Document document = Jsoup.connect(URL).get();
                Elements linksOnPage = document.select("a[href]");

                depth++;
                for (Element page : linksOnPage) {
                    getPageLinks(page.attr("abs:href"), depth);
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }

    public HashSet<String> getLinks() {
        return this.links;
    }

    public static void downloadFile(String url, String fileName) throws FileNotFoundException {
        AsyncHttpClient client = Dsl.asyncHttpClient();
        FileOutputStream stream = new FileOutputStream(fileName);
        client.prepareGet(url).execute(new AsyncCompletionHandler<FileOutputStream>() {
            @Override
            public State onBodyPartReceived(HttpResponseBodyPart bodyPart)
                    throws Exception {
                stream.getChannel().write(bodyPart.getBodyByteBuffer());
                return State.CONTINUE;
            }

            @Override
            public FileOutputStream onCompleted(Response response)
                    throws Exception {
                return stream;
            }
        });
    }

    public static String addTutorial(String link, String filetype, String description) throws IOException, JSONException {
        URL url = new URL("http://localhost:8085/api/tutorials");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);
        Map<String,String> arguments = new HashMap<>();
        arguments.put("title", link);
        arguments.put("domain", new URL(link).getHost().replace("www.", ""));
        arguments.put("filetype", filetype);
        arguments.put("description", description);
        JSONObject js = new JSONObject(arguments);
        byte[] out = js.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(http.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        String rv = content.toString();
        JSONObject json = new JSONObject(rv);
        String id = json.getString("id");
        http.disconnect();

        return id;
    }

    public static void main(String[] args) throws IOException, JSONException {
        Crawler crawler = new Crawler();
        crawler.getPageLinks("https://mkyong.com/java/jsoup-basic-web-crawler-example/", 0);
        HashSet<String> hs = crawler.getLinks();
        for (String string: hs) {
            String id = addTutorial(string, "html", "html");
            downloadFile(string, id + ".html");
        }
    }
}


