package mas.bezkoder.crawler;

import com.mongodb.MongoWriteException;
import mas.bezkoder.controller.TutorialController;
import mas.bezkoder.model.Tutorial;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mas.bezkoder.LinkExtractor.HTMLExtractor.replaceUrl;
import static mas.bezkoder.controller.TutorialController.getTextFile;
import static mas.bezkoder.crawler.CrawlCSS.crawlCSS;
import static mas.bezkoder.crawler.CrawlHTML.getPageLinks;

public class CrawlMain {
    private static final String otherRegex = "https?://([^{}<>\"'\\s)]*)";

    public static void run(String link) throws IOException, JSONException, URISyntaxException {
        try {
            crawlSite(link);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * main caller function
     * @param website url of website to start at
     * @throws JSONException issues building tutorials
     * @throws URISyntaxException badly built url
     * @throws IOException issues with url
     */
    public static void crawlSite(String website) throws JSONException, URISyntaxException, IOException, NoSuchAlgorithmException {
        String url;
        while (true) {
            url = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
            if (url.equals(website)) break;
            else website = url;
        }
        String startDomain = new URL(url).getHost();
        HashSet<String> visited = new HashSet<>();
        HashSet<String> checksums = new HashSet<>();
        HashSet<String> hs = getPageLinks(url, startDomain, 0, visited, checksums);
        HashSet<String> otherLinks = new HashSet<>();
        int count = 1;
        if (hs == null) return;

        //write urls to file to compare

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
            Tutorial tut;
            String success;
            try {
                success = downloadFile(string);
                if (success == null) continue;

            } catch (IOException | IllegalArgumentException e) {
                continue;
            }
            tut = addTutorial(string, extension, success, extension, contentType);
            if (count % 10 == 0) System.out.println("Done with " + count + "/" + hs.size());
            count++;
            String content;
            try {
                InputStream is = TutorialController.getInputStream(tut);
                content = getTextFile(is, tut);
            } catch (Exception ex) {
                ex.printStackTrace();
                continue;
            }

            if (extension.equals("css")) {
                try {
                    otherLinks.addAll(crawlCSS(content, string));
                } catch (IOException | URISyntaxException ignored) {
                }
            } else if (extension.equals("js")) {
                try {
                    HashSet<String> jsLinks = searchJs(content, string);
                    otherLinks.addAll(jsLinks);
                } catch (IOException ignored) {
                }
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

            try {
                String success = downloadFile(string);
                if (success == null) {
                    System.out.println("fail " + string);
                    continue;
                }
                addTutorial(string, extension, success, extension, contentType);
            } catch (Exception e) {
                continue;
            }
            if (count % 10 == 0) System.out.println("Done with " + count + "/" + otherLinks.size());
            count++;


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

    /**
     * downloads file with given URL
     * @param url url of file to download
     * @return success or not success
     * @throws IOException for issues in url or fileName
     */
    public static String downloadFile(String url) throws IOException, NoSuchAlgorithmException {
        if (url.startsWith("https://href.li/?")) url = url.replace("https://href.li/?", "");
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "8123");
        HttpURLConnection webProxyConnection
                = (HttpURLConnection) new URL(url).openConnection();

        InputStream is = null;
        try {
            is = webProxyConnection.getInputStream();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return null;
        }

        byte[] bytes = IOUtils.toByteArray(is);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] sha256Hash = digest.digest(bytes);
        String checksum = bytesToHex(sha256Hash);
        try {
            addDownload(checksum, bytes);
        } catch (Throwable ex) {
            System.out.println("File Already Exists");
            return checksum;
        }
        return checksum;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
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
    public static Tutorial addTutorial(String link, String filetype, String sha256, String description, String contentType) throws IOException, JSONException {
        URL url = new URL("http://localhost:8085/api/tutorials");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);
        Map<String,String> arguments = new HashMap<>();
        String domain = new URL(link).getHost();
        String encoding = getEncoding(contentType);
        arguments.put("title", link);
        arguments.put("domain", domain);
        arguments.put("sha256", sha256);
        arguments.put("filetype", filetype);
        arguments.put("description", description);
        arguments.put("contentType", contentType);
        arguments.put("contentEncoding", encoding);
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
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        String rv = content.toString();
        JSONObject json = new JSONObject(rv);
        String id = json.getString("id");
        http.disconnect();
        System.out.println("New Tutorial " + link);
        return new Tutorial(link, description, sha256, domain, filetype, contentType, encoding);
    }

    public static void addDownload(String sha256, byte[] bytes) throws IOException, MongoWriteException {
        HttpPost httppost = new HttpPost("http://localhost:8085/api/file/add");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addTextBody("title", sha256)
                .addPart("file", new ByteArrayBody(bytes, ""));
        httppost.setEntity(multipartEntity.build());
        CloseableHttpResponse response = httpClient.execute(httppost);
        HttpEntity entity = response.getEntity();
        httpClient.close();
        response.close();
    }

    /**
     * get content type by connecting to url
     * @param link link of file to get contentType for
     * @return content type information
     */
    public static String getContentType(String link) {
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "8123");

        if (link.startsWith("https://href.li/?")) link = link.replace("https://href.li/?", "");
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
            connection = (HttpURLConnection) url.openConnection();
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


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, URISyntaxException {
        URI url = new URI("http://118.67.133.84:8085/api/websites?date=12345456576&web=https%3A%2F%2Fnsnw.ca%2F");
        List<NameValuePair> params = URLEncodedUtils.parse(url, StandardCharsets.UTF_8);
        for (NameValuePair param : params) {
            System.out.println(param.getName() + " : " + param.getValue());
        }



//        String second = "http://abasdfasdfw2eerqweg";
//        System.out.println(replaceUrl(website, second));
//        URL url = new URL(website);

//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("HEAD");
//        if (isRedirect(connection.getResponseCode())) {
//            String newUrl = connection.getHeaderField("Location"); // get redirect url from "location" header field
////            logger.warn("Original request URL: '{}' redirected to: '{}'", urlString, newUrl);
//            System.out.println(getContentType(newUrl));
//        }
//        String contentType = connection.getContentType();
//        System.out.println(contentType);

//        System.out.println(iterate.size());

    }

}
