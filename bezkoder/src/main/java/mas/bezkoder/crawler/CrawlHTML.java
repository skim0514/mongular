package mas.bezkoder.crawler;

import mas.bezkoder.LinkExtractor.HTMLExtractor;
import mas.bezkoder.model.Tutorial;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;

import static mas.bezkoder.controller.TutorialController.getTextFile;
import static mas.bezkoder.crawler.CrawlCSS.crawlCSS;
import static mas.bezkoder.crawler.CrawlMain.addTutorial;
import static mas.bezkoder.crawler.CrawlMain.downloadFile;

public class CrawlHTML extends HTMLExtractor {
    private static final int MAX_DEPTH = 2;
    private static final Proxy webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8123));
    private String domain;


    public CrawlHTML(String url, String domain, int depth, HashSet<String> visited, HashSet<String> checksums) {
        super(url, new HashSet<>(), depth, visited, checksums);
        this.domain = domain;
    }

    @Override
    public void parseSrcSet(Elements srcsets) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element srcset : srcsets) {
            String hold = srcset.attr("srcset");
            String[] strings = hold.split(", ");
            for (String s: strings) {
                String[] urls = s.split(" ");
                String newurl = replaceUrl(urls[0], current);
                if (newurl == null) continue;
                links.add(newurl);
                System.out.println(">> Depth: " + getDepth() + " [" + newurl + "]");
            }
        }
        setUrls(links);
    }

    @Override
    public void parseSrc(Elements src) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element s : src) {
            String slink = s.attr("src");
            if (!slink.startsWith("data:image")) {
                slink = replaceUrl(slink, current);
                if (slink == null) continue;
                links.add(slink);
                System.out.println(">> Depth: " + getDepth() + " [" + slink + "]");
            }
        }
        setUrls(links);
    }

    @Override
    public void parsedsrc(Elements dsrc) {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element s : dsrc) {
            String slink = s.attr("data-src");
            if (!slink.startsWith("data:image")) {
                slink = replaceUrl(slink, current);
                if (slink == null) continue;
                links.add(slink);
                System.out.println(">> Depth: " + getDepth() + " [" + slink + "]");
            }
        }
        setUrls(links);
    }

    @Override
    public void parseBackground(Elements background) throws MalformedURLException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element b : background) {
            String blink = b.attr("background");
            if (!blink.startsWith("data:image")) {
                blink = replaceUrl(blink, current);
                if (blink == null) continue;
                links.add(blink);
                System.out.println(">> Depth: " + getDepth() + " [" + blink + "]");
            }
        }
        setUrls(links);
    }

    @Override
    public void parseLinkLink(Elements link) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element l : link) {
            String llink = l.attr("href");
            llink = replaceUrl(llink, current);
            if (llink == null) continue;
            links.add(llink);
            System.out.println(">> Depth: " + getDepth() + " [" + llink + "]");
        }
        setUrls(links);
    }

    @Override
    public void parseStyle(Elements style) throws JSONException, IOException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        for (Element css: style) {
            String hold = css.attr("style");
            links.addAll(crawlCSS(hold, current));
        }
        setUrls(links);
    }

    @Override
    public void parseOtherStyle(Matcher matcher) throws JSONException, IOException, URISyntaxException {
        HashSet<String> links = getUrls();
        String current = getUrl();
        while (matcher.find()) {
            String group = matcher.group(0);
            links.addAll(crawlCSS(group, current));
        }
        setUrls(links);
    }

    @Override
    public void parseOther(Matcher matcher) {
        HashSet<String> links = getUrls();
        while (matcher.find()) {
            String group = matcher.group(0);
            links.add(group);
        }
        setUrls(links);
    }

    @Override
    public void parseALink(Elements hrefs) throws IOException, URISyntaxException{
        HashSet<String> links = getUrls();
        HashSet<String> visited = getVisited();
        HashSet<String> checksums = getChecksums();
        String current = getUrl();
        for (Element page : hrefs) {
            String alink = page.attr("href");
            alink = replaceUrl(alink, current);
            if (alink == null) continue;
            if(!alink.contains(this.domain)) continue;
            if(visited.contains(alink)) continue;
            HashSet<String> hold = getPageLinks(alink, this.domain, getDepth() + 1, visited, checksums);
            if (hold != null) links.addAll(hold);
        }
        setVisited(visited);
        setChecksums(checksums);
        setUrls(links);
    }

    public static HashSet<String> getPageLinks(String URL, String domain, int depth, HashSet<String> visited, HashSet<String> checksums) {
        if (depth == MAX_DEPTH) return null;
        CrawlHTML crawler = new CrawlHTML(URL, domain, depth, visited, checksums);
        HashSet<String> hs = crawler.getUrls();
        crawler.setUrls(hs);
        visited.add(URL);
        System.out.println(visited.size());
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "8123");
        try {
            URL = CrawlMain.decode(URL);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
        if (!URL.startsWith("http")) {
            System.out.println(URL + " fail");
            return null;
        }
        String extension;
        String contentType = CrawlMain.getContentType(URL);
        String filetype;
        if (contentType == null) return null;
        else {
            filetype = contentType.split(";")[0];
            extension = MimeTypes.getDefaultExt(filetype);
        }
        Tutorial tut;
        boolean added;
        try {
            String success = downloadFile(URL);
            if (success == null) return null;
            tut = addTutorial(URL, extension, success, extension, contentType);
            added = checksums.add(success);
        } catch (IOException | IllegalArgumentException | NoSuchAlgorithmException | JSONException e) {
            return null;
        }
        if (!added) {
            System.out.println("website already crawled");
            return null;
        }
        crawler.setChecksums(checksums);
        String content;
        try {
            content = getTextFile(tut);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        try {
            Document document = Jsoup.parse(content);
            crawler.setDocument(document);
            crawler.extractHtml();
            return crawler.getUrls();
        } catch (Exception ex) {
            System.err.println("For '" + URL + "': " + ex.getMessage());
            return null;
        }
    }




}


