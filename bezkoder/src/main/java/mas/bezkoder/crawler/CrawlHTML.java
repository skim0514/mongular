package mas.bezkoder.crawler;

import mas.bezkoder.LinkExtractor.HTMLExtractor;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;

import static mas.bezkoder.crawler.CrawlCSS.crawlCSS;

public class CrawlHTML extends HTMLExtractor {
    private static final int MAX_DEPTH = 3;
    private static final Proxy webProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8123));
    private String domain;

    public CrawlHTML(String domain, int depth) {
        super(domain, new HashSet<>(), depth);
        this.domain = domain;
    }

    public CrawlHTML(String url, String domain, int depth) {
        super(url, new HashSet<>(), depth);
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

    public static HashSet<String> getPageLinks(String URL, String domain, int depth) {
        if (depth == MAX_DEPTH) return null;
        CrawlHTML crawler = new CrawlHTML(URL, domain, depth);
        HashSet<String> hs = crawler.getUrls();
        hs.add(URL);
        crawler.setUrls(hs);
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "8123");
        try {
            Document document = Jsoup.connect(URL).get();
            crawler.setDocument(document);
            crawler.extractHtml();
            return crawler.getUrls();
        } catch (Exception ex) {
            System.err.println("For '" + URL + "': " + ex.getMessage());
            return null;
        }
    }




}


