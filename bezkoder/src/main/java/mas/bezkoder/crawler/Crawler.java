package mas.bezkoder.crawler;

import mas.bezkoder.parser.Parser;
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
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";
    private static final String htmlTag = "<(?!!)(?!/)\\s*([a-zA-Z0-9]+)(.*?)>";
    private static final Proxy webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8123));
    private String domain;
    private HashSet<String> links;

    public Crawler(String domain) {
        this.domain = domain;
        this.links = new HashSet<>();
    }

    public void getPageLinks(String URL, int depth) {
        if ((!links.contains(URL) && (depth < MAX_DEPTH))) {
            System.out.println(">> Depth: " + depth + " [" + URL + "]");
            try {
                links.add(URL);
                Document document = Jsoup.connect(URL).proxy(webProxy).get();
                Elements linksOnPage = document.select("a[href]");
                Elements src = document.select("[src]");
                Elements link = document.select("link[href]");
                Elements srcsets = document.select("[srcset]");

                for (Element s : src) {
                    String slink = s.attr("abs:src");
                    links.add(slink);
                    System.out.println(">> Depth: " + depth + " [" + slink + "]");
                }
                for (Element l : link) {
                    String llink = l.attr("abs:href");
                    links.add(llink);
                    System.out.println(">> Depth: " + depth + " [" + llink + "]");
                }

                for (Element srcset : srcsets) {
                    String hold = srcset.attr("srcset");
                    String[] strings = hold.split(", ");
                    for (String s: strings) {
                        String[] urls = s.split(" ");
                        String newurl = Parser.replaceUrl(urls[0], URL);
                        links.add(newurl);
                        System.out.println(">> Depth: " + depth + " [" + newurl + "]");
                    }
                }

                Pattern pattern = Pattern.compile(otherRegex);
                Matcher matcher = pattern.matcher(document.toString());
                while (matcher.find()) {
                    String group = matcher.group(0);
                    links.add(group);
                }

                depth++;

                for (Element page : linksOnPage) {
                    String alink = page.attr("abs:href");
                    if(!alink.contains(this.domain)) continue;
                    getPageLinks(alink, depth);
                }



            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public HashSet<String> getLinks() {
        return this.links;
    }

    public static boolean downloadFile(String url, String fileName) throws IOException {
        HttpURLConnection webProxyConnection
                = (HttpURLConnection) new URL(url).openConnection(webProxy);

        ReadableByteChannel readChannel;
//        try {
//            readChannel = Channels.newChannel(new URL(url).openStream());
//        } catch (IOException e) {
//            return false;
//        }
        try {
            readChannel = Channels.newChannel(webProxyConnection.getInputStream());
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
        arguments.put("domain", new URL(link).getHost());
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
        URL url;


        try {
            url = new URL(link);
        } catch (MalformedURLException e){
            e.printStackTrace();
            System.out.println(link);
            return null;
        }

        HttpURLConnection connection;
        try {
//            connection = (HttpURLConnection) url.openConnection();
            connection = (HttpURLConnection) url.openConnection(webProxy);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            return connection.getContentType();
        } catch (Exception ex) {
            System.out.println(link);
            return null;
        }

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

    public static String decode(String url) throws IOException, IllegalArgumentException{
        String newlink = "";
        while (true) {
            newlink = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
            if (newlink.equals(url)) break;
            else url = newlink;
        }
        url = newlink;
        return url;
    }

    public static boolean isRedirect(int statusCode) {
        if (statusCode != HttpURLConnection.HTTP_OK) {
            return (statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || statusCode == HttpURLConnection.HTTP_MOVED_PERM
                    || statusCode == HttpURLConnection.HTTP_SEE_OTHER);
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
            group = group.replaceAll("'", "");
            if (group.startsWith("data:")) continue;
            String newUrl = Parser.replaceUrl(group, string);
            hs.add(newUrl);
        }
        pattern = Pattern.compile(otherRegex);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(0);
            if (group.startsWith("data:")) continue;
            String newUrl = Parser.replaceUrl(group, string);
            hs.add(newUrl);
        }
        return hs;
    }

    public static void crawlSite(String url) throws JSONException, URISyntaxException, IOException {
        String start = url;
        String startDomain = new URL(start).getHost();
        Crawler crawler = new Crawler(startDomain);
        crawler.getPageLinks(start, 0);
        HashSet<String> hs = crawler.getLinks();
        HashSet<String> otherLinks = new HashSet<>();
        int count = 1;
        for (String string : hs) {
            try {
                string = decode(string);
            } catch (IOException | IllegalArgumentException e) {
                continue;
            }
            if (!string.startsWith("http")) {
                System.out.println(string + " fail");
                continue;
            }
            String extension;
            String contentType = getContentType(string);
            String filetype;
            if (contentType == null) continue;
            else {
                filetype = contentType.split(";")[0];
                extension = MimeTypes.getDefaultExt(filetype);
            }
            String id;
            try {
                id = addTutorial(string, extension, extension, contentType);
            } catch (IOException | IllegalArgumentException e) {
                continue;
            }
            if (count % 10 == 0) System.out.println("Done with " + count + "/" + hs.size());
            count++;
            if (extension.equals("css")) {
                try {
                    downloadFile(string, "files/" + id);
                    Path cssFile = Path.of("files/" + id);
                    String content = Files.readString(cssFile);
                    HashSet<String> cssLinks = searchCss(content, string);
                    otherLinks.addAll(cssLinks);
                } catch (IOException | URISyntaxException ignored) {
                }
            } else if (extension.equals("js")) {
                try {
                    downloadFile(string, "files/" + id);
                    Path jsFile = Path.of("files/" + id);
                    String content = Files.readString(jsFile);
                    HashSet<String> jsLinks = searchJs(content, string);
                    otherLinks.addAll(jsLinks);
                } catch (IOException ignored) {
                }
            } else {
                downloadFile(string,"files/" + id);
            }
        }
        count = 1;
        for (String string : otherLinks) {
            try {
                string = decode(string);
            } catch (IOException | IllegalArgumentException e) {
                continue;
            }
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

            String id;
            try {
                id = addTutorial(string, extension, extension, contentType);
            } catch (Exception e) {
                continue;
            }
            if (count % 10 == 0) System.out.println("Done with " + count + "/" + otherLinks.size());
            count++;
            downloadFile(string, "files/" + id);
        }
    }

    public static void main(String args[]) throws IOException, JSONException, URISyntaxException {
        crawlSite(args[0]);
    }

    public static HashSet<String> searchJs(String content, String string) throws IOException, URISyntaxException {
        HashSet<String> hs = new HashSet<>();
        Pattern pattern = Pattern.compile(otherRegex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String group = matcher.group(0);
            String newUrl = Parser.replaceUrl(group, string);
            hs.add(newUrl);
        }
        return hs;
    }
}


