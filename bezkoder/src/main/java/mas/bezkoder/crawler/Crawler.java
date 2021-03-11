package mas.bezkoder.crawler;

import mas.bezkoder.linkExtract.LinkExtractor;
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

public class Crawler extends LinkExtractor {

    private static final int MAX_DEPTH = 2;
    private static final String CSSRegex = "url\\((.*?)\\)";
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";
    private static final String htmlTag = "<(?!!)(?!/)\\s*([a-zA-Z0-9]+)(.*?)>";
    private static final Proxy webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8123));
    private static final String style ="<style([\\s\\S]+?)</style>";
    private String domain;

    public Crawler(String domain, int depth) {
        super(domain, new HashSet<>(), depth);
        this.domain = domain;
    }

    public Crawler(String url, String domain, int depth) {
        super(url, new HashSet<>(), depth);
        this.domain = domain;
    }

    public Crawler(String input, String url, HashSet<String> urls) {
        super(input, url, urls);
    }


    public void parseSrcSet(Elements srcsets) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element srcset : srcsets) {
            String hold = srcset.attr("srcset");
            String[] strings = hold.split(", ");
            for (String s: strings) {
                String[] urls = s.split(" ");
                String newurl = replaceUrl(urls[0], current);
                links.add(newurl);
                System.out.println(">> Depth: " + getDepth() + " [" + newurl + "]");
            }
        }
        setUrls(links);
    }

    public void parseSrc(Elements src) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element s : src) {
            String slink = s.attr("src");
            if (!slink.startsWith("data:image")) {
                slink = replaceUrl(slink, current);
                links.add(slink);
                System.out.println(">> Depth: " + getDepth() + " [" + slink + "]");
            }
        }
        setUrls(links);
    }

    public void parseBackground(Elements background) throws MalformedURLException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element b : background) {
            String blink = b.attr("background");
            if (!blink.startsWith("data:image")) {
                blink = replaceUrl(blink, current);
                links.add(blink);
                System.out.println(">> Depth: " + getDepth() + " [" + blink + "]");
            }
        }
        setUrls(links);
    }

    public void parseLinkLink(Elements link) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element l : link) {
            String llink = l.attr("href");
            llink = replaceUrl(llink, current);
            links.add(llink);
            System.out.println(">> Depth: " + getDepth() + " [" + llink + "]");
        }
        setUrls(links);
    }

    public void parseStyle(Elements style) throws JSONException, IOException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element css: style) {
            String hold = css.attr("style");
            links.addAll(searchCss(hold, current));
        }
        setUrls(links);
    }

    public void parseOtherStyle(Matcher matcher) throws JSONException, IOException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        while (matcher.find()) {
            String group = matcher.group(0);
            links.addAll(searchCss(group, current));
        }
        setUrls(links);
    }

    public void parseOther(Matcher matcher) {
        HashSet<String> links = getUrls();
        while (matcher.find()) {
            String group = matcher.group(0);
            links.add(group);
        }
        setUrls(links);
    }

    public void parseCSS(Matcher matcher) throws MalformedURLException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        while (matcher.find()) {
            String group = matcher.group(1);
            group = group.replaceAll("\"", "");
            group = group.replaceAll("'", "");
            if (group.startsWith("data:")) continue;
            String newUrl = replaceUrl(group, current);
            links.add(newUrl);
        }
        setUrls(links);
    }

    public void parseALink(Elements hrefs) throws IOException, URISyntaxException, JSONException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element page : hrefs) {
            String alink = page.attr("href");
            alink = replaceUrl(alink, current);
            if(!alink.contains(this.domain)) continue;
            if(links.contains(alink)) continue;
            HashSet<String> hold = getPageLinks(alink, this.domain, getDepth() + 1);
            if (hold != null) links.addAll(hold);
        }
        setUrls(links);
    }

    public static HashSet<String> getPageLinks(String URL, String domain, int depth) throws JSONException, IOException, URISyntaxException {
        if (depth == MAX_DEPTH) return null;
        Crawler crawler = new Crawler(URL, domain, depth);
        HashSet<String> hs = crawler.getUrls();
        hs.add(URL);
        crawler.setUrls(hs);
        try {
            Document document = Jsoup.connect(URL).proxy(webProxy).get();
            crawler.setDocument(document);
            crawler.extractHtml();
            return crawler.getUrls();
        } catch (Exception ex) {
            System.err.println("For '" + URL + "': " + ex.getMessage());
            return null;
        }
    }

    /**
     * searches css for urls
     * @param input input css string
     * @param string our given domain for our website
     * @return hashset of urls that are in the css
     * @throws IOException issues with our url information.
     * @throws URISyntaxException badly built url
     * @throws JSONException issues building tutorial of website link
     */
    public static HashSet<String> searchCss(String input, String string) throws IOException, URISyntaxException, JSONException {
        Crawler cssCrawler = new Crawler(input, string, new HashSet<>());
        cssCrawler.extractCSS();
        return cssCrawler.getUrls();
    }

    /**
     * downloads file with given URL
     * @param url url of file to download
     * @param fileName filename with location to download to
     * @return success or not success
     * @throws IOException for issues in url or fileName
     */
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
        } catch (IOException | RuntimeException e) {
            return false;
        }

        FileOutputStream fileOS = new FileOutputStream(fileName);
        FileChannel writeChannel = fileOS.getChannel();
        writeChannel
                .transferFrom(readChannel, 0, Long.MAX_VALUE);
        return true;
    }

    /**
     * add website information step of crawling
     * @param link url for our website that we are adding to db
     * @param filetype filetype of our website to add to db
     * @param description ""
     * @param contentType content type of our website
     * @return id number for file download
     * @throws IOException issues with links
     * @throws JSONException issues with building our db entry
     */
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

    /**
     * get content type by connecting to url
     * @param link link of file to get contentType for
     * @return content type information
     * @throws IOException issues with our URLr
     */
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

    /**
     * get file encoding from contentType information
     * @param contentType content type string which sometimes keeps encoding information
     * @return returns file encoding information
     */
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

    /**
     * helper function to decode strings
     * @param url url to decode
     * @return decoded url
     * @throws IOException issue with url
     * @throws IllegalArgumentException illegally built url
     */
    public static String decode(String url) throws IOException, IllegalArgumentException{
        String newlink;
        while (true) {
            newlink = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
            if (newlink.equals(url)) break;
            else url = newlink;
        }
        url = newlink;
        return url;
    }

    /**
     * check if website is a redirected website
     * @param statusCode inputs status code of link connection
     * @return bool if our website is a redirect
     */
    public static boolean isRedirect(int statusCode) {
        if (statusCode != HttpURLConnection.HTTP_OK) {
            return (statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || statusCode == HttpURLConnection.HTTP_MOVED_PERM
                    || statusCode == HttpURLConnection.HTTP_SEE_OTHER);
        }
        return false;
    }

    /**
     * main caller function
     * @param url url of website to start at
     * @throws JSONException issues building tutorials
     * @throws URISyntaxException badly built url
     * @throws IOException issues with url
     */
    public static void crawlSite(String url) throws JSONException, URISyntaxException, IOException {
        String start = url;
        String startDomain = new URL(start).getHost();
        HashSet<String> hs = getPageLinks(start, startDomain, 0);
        HashSet<String> otherLinks = new HashSet<>();
        int count = 1;
        if (hs == null) return;
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

    public static HashSet<String> searchJs(String content, String string) throws IOException, URISyntaxException {
        HashSet<String> hs = new HashSet<>();
        Pattern pattern = Pattern.compile(otherRegex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String group = matcher.group(0);
            String newUrl = replaceUrl(group, string);
            hs.add(newUrl);
        }
        return hs;
    }

    public static void main(String args[]) throws IOException, JSONException, URISyntaxException {
//        HashSet<String> links = getPageLinks("http://crdclub4wraumez4.onion/", "crdclub4wraumez4.onion", 0);
//        System.out.println(links.size());


        crawlSite(args[0]);
    }


}


