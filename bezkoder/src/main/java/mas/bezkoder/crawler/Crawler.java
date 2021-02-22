package mas.bezkoder.crawler;

import mas.bezkoder.parser.MimeTypes;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {

    private static final int MAX_DEPTH = 2;
    private static final String CSSRegex = "url\\((.*?)\\)";
    private static final String otherRegex = "https?://([^\"']*)";
    private String domain;
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
                Elements src = document.select("[src]");
                Elements link = document.select("link[href]");

                for (Element s : src) {
                    String slink = s.attr("abs:src");
                    links.add(slink);
                    Pattern pattern = Pattern.compile(otherRegex);
                    Matcher matcher = pattern.matcher(s.toString());
                    while (matcher.find()) {
                        String group = matcher.group(0);
                        if (group.startsWith("data:")) continue;
                        String newUrl = replaceUrl(group, s.toString());
                        links.add(newUrl);
                    }
                    System.out.println(">> Depth: " + depth + " [" + slink + "]");
                }
                for (Element l : link) {
                    String llink = l.attr("abs:href");
                    links.add(llink);
                    System.out.println(">> Depth: " + depth + " [" + llink + "]");
                }

                depth++;
                for (Element page : linksOnPage) {
                    String alink = page.attr("abs:href");
                    if(!alink.contains(this.domain)) continue;
                    getPageLinks(alink, depth);
                }
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            } catch (JSONException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public HashSet<String> getLinks() {
        return this.links;
    }

//    public static void downloadFile(String url, String fileName) throws IOException {
//        AsyncHttpClient client = Dsl.asyncHttpClient();
//        FileOutputStream stream = new FileOutputStream(fileName);
//        client.prepareGet(url).execute(new AsyncCompletionHandler<FileOutputStream>() {
//            @Override
//            public State onBodyPartReceived(HttpResponseBodyPart bodyPart)
//                    throws Exception {
//                stream.getChannel().write(bodyPart.getBodyByteBuffer());
//                return State.CONTINUE;
//            }
//
//            @Override
//            public FileOutputStream onCompleted(Response response)
//                    throws Exception {
//                return stream;
//            }
//        });
//        stream.close();
//        client.close();
//    }

    public static boolean downloadFile(String url, String fileName) throws IOException {
        ReadableByteChannel readChannel = null;
        try {
            readChannel = Channels.newChannel(new URL(url).openStream());
        } catch (IOException e) {
            return false;
        }
        FileOutputStream fileOS = new FileOutputStream(fileName);
        FileChannel writeChannel = fileOS.getChannel();
        writeChannel
                .transferFrom(readChannel, 0, Long.MAX_VALUE);
        return true;
    }

    public static String addTutorial(String link, String filetype, String description, String contentType) throws IOException, JSONException {

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
        arguments.put("contentType", contentType);
        arguments.put("contentEncoding", getEncoding(contentType));
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
        System.out.println("New Tutorial " + link);
        return id;
    }

    public static String getContentType(String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("HEAD");
//        boolean redirect;
//        try {
//            redirect = isRedirect(connection.getResponseCode());
//            System.out.println(redirect);
//        } catch (Exception e){
//            redirect = false;
//        }
//        if (redirect) {
//            String newUrl = connection.getHeaderField("Location"); // get redirect url from "location" header field
//            return getContentType(newUrl);
//        }
        String contentType = connection.getContentType();
        return contentType;
    }

    public static String getEncoding(String contentType) {
        if (contentType == null) return "UTF-8";
        String[] values = contentType.split(";");
        String charset = "";
        for (String value : values) {
            value = value.trim();
            if (value.toLowerCase().startsWith("charset=")) {
                charset = value.substring("charset=".length());
            }
        }

        if ("".equals(charset)) {
            charset = "UTF-8"; //Assumption
        }
        return charset;
    }

    protected static boolean isRedirect(int statusCode) {
        if (statusCode != HttpURLConnection.HTTP_OK) {
            if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || statusCode == HttpURLConnection.HTTP_MOVED_PERM
                    || statusCode == HttpURLConnection.HTTP_SEE_OTHER) {
                return true;
            }
        }
        return false;
    }

    public static HashSet<String> searchCss(String input, String string) throws IOException, URISyntaxException, JSONException {
        HashSet<String> hs = new HashSet<>();
        Pattern pattern = Pattern.compile(CSSRegex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(1);
            group = group.replaceAll("\"", "");
            group = group.replaceAll("\'", "");
            if (group.startsWith("data:")) continue;
            String newUrl = replaceUrl(group, string);
            if (newUrl != null) hs.add(newUrl);
        }
        pattern = Pattern.compile(otherRegex);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            if (group.startsWith("data:")) continue;
            String newUrl = replaceUrl(group, string);
            if (newUrl != null) hs.add(newUrl);
        }
        return hs;
    }

    public static String replaceUrl(String url, String string) throws URISyntaxException, IOException, JSONException {
        String strFind = "../";
        int count = 0, fromIndex = 0;
        while ((fromIndex = url.indexOf(strFind, fromIndex)) != -1 ){
//            System.out.println("Found at index: " + fromIndex);
            count++;
            fromIndex++;
        }
        String newUrl;
        newUrl = getUrlFromPath(url, string, count);
        return newUrl;
    }

    private static String getUrlFromPath(String url, String string, int count) throws URISyntaxException, MalformedURLException {
        String newUrl;
        String domain = new URL(string).getHost().replace("www.", "");
        if (url.startsWith("http")) {
            newUrl = url;
        } else if (url.startsWith("#")) {
            newUrl = string + url;
        } else if (url.startsWith("//")) {
            newUrl = "https:" + url;
        }
        else if (url.startsWith("/")) {
            URI link = new URI(string);
            newUrl = link.getScheme() + "://" + domain + url;
        } else if (url.startsWith("./")) {
            URI parent = new URI(string);
            parent = parent.getPath().endsWith("/") ? parent.resolve("..") : parent.resolve(".");
            newUrl = parent.toString() + url.substring(2);
        } else if (url.startsWith("../")) {
            URI link = new URI(string);
            for (int i = 0; i <= count; i++) {
                URI parent = link.getPath().endsWith("/") ? link.resolve("..") : link.resolve(".");
                link = parent;
            }
            newUrl = link.toString() + url.substring(3 * count);
        } else {
            URI parent = new URI(string);
            parent = parent.getPath().endsWith("/") ? parent.resolve("..") : parent.resolve(".");
            newUrl = parent.toString() + url;
        }
        return newUrl;
    }

    public static void main(String args[]) throws IOException, JSONException, URISyntaxException {
        Crawler crawler = new Crawler();
        String start = args[0];
        String startDomain = new URL(start).getHost().replace("www.", "");
        crawler.setDomain(startDomain);
        crawler.getPageLinks(start, 0);
        HashSet<String> hs = crawler.getLinks();
        HashSet<String> otherLinks = new HashSet<>();
        for (String string : hs) {
            if (!string.startsWith("http")) continue;
            String extension;
            String contentType = getContentType(string);
            String filetype;
            if (contentType == null) extension = "";
            else {
                filetype = contentType.split(";")[0];
                extension = MimeTypes.getDefaultExt(filetype);
            }
            if (extension.equals("css")) {
                try {
                    String id = addTutorial(string, extension, extension, contentType);
                    if (id == null) continue;
                    downloadFile(string, "files/" + id + "." + extension);
                    Path cssFile = Path.of("files/" + id + "." + extension);
                    String content = Files.readString(cssFile);
                    HashSet<String> cssLinks = searchCss(content, string);
                    otherLinks.addAll(cssLinks);
                } catch (IOException ex) {
                    continue;
                }
            } else if (extension.equals("js")) {
                try {
                    String id = addTutorial(string, extension, extension, contentType);
                    if (id == null) continue;
                    downloadFile(string, "files/" + id + "." + extension);
                    Path jsFile = Path.of("files/" + id + "." + extension);
                    String content = Files.readString(jsFile);
                    HashSet<String> jsLinks = searchJs(content, string);
                    otherLinks.addAll(jsLinks);
            } catch (IOException ex) {
                continue;
            }
            }
            else {
                String id = addTutorial(string, extension, extension, contentType);
                if (id == null) continue;
                if (extension.equals("") || extension.equals("unknown")) downloadFile(string, "files/" + id);
                else downloadFile(string, "files/" + id + "." + extension);
            }
        }
        for (String string : otherLinks) {
            if (!string.startsWith("http")) continue;
            String extension;
            String contentType;
            try {
               contentType = getContentType(string);
            } catch (Exception e) {
                continue;
            }
            String filetype;
            if (contentType == null) extension = "";
            else {
                filetype = contentType.split(";")[0];
                extension = MimeTypes.getDefaultExt(filetype);
            }
            String id = addTutorial(string, extension, extension, contentType);
            if (id == null) continue;
            if (extension.equals("") || extension.equals("unknown")) downloadFile(string, "files/" + id);
            else downloadFile(string, "files/" + id + "." + extension);
        }
    }

    private static HashSet<String> searchJs(String content, String string) throws JSONException, IOException, URISyntaxException {
        HashSet<String> hs = new HashSet<>();
        Pattern pattern = Pattern.compile(otherRegex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String group = matcher.group(0);
            String newUrl = replaceUrl(group, string);
            if (newUrl != null) hs.add(newUrl);
        }
        return hs;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}


