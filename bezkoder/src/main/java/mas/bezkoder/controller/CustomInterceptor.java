package mas.bezkoder.controller;

import mas.bezkoder.LinkExtractor.HTMLExtractor;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class CustomInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        System.out.println("prehandle");


        System.out.println(request.getRequestURI());
        return true;
    }

    public static ResponseEntity<?> getRandom(HttpServletRequest request) throws URISyntaxException, IOException {
        System.out.println("random");
        String requestURL = request.getRequestURI();
        String referer = request.getHeader(HttpHeaders.REFERER);
        final List<String> headerNames = Collections.list(request.getHeaderNames());
        if (requestURL.startsWith("/api")) requestURL = requestURL.replace("/api/", "");
        if (referer == null) return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        System.out.println("getReferer");
        URI url = new URI(referer);
        List<NameValuePair> params = URLEncodedUtils.parse(url, StandardCharsets.UTF_8);
        String base = null;
        String date = null;
        for (NameValuePair param : params) {
            if (param.getName().equals("date")) date = param.getValue();
            if (param.getName().equals("web")) base = param.getValue();
        }
        if (base == null || date == null) return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        String link = HTMLExtractor.replaceUrl(requestURL, base);
        if (link == null) return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        String requestString = "http://localhost:8085/api/websites?date=" + date + "&web=" + java.net.URLEncoder.encode(link, StandardCharsets.UTF_8.name());
        System.out.println("getURL " + requestString);
        HttpGet httpGet = new HttpGet(requestString);
        for (String headerName : headerNames) {
            if (headerName.equals(HttpHeaders.REFERER)) httpGet.addHeader(HttpHeaders.REFERER, referer);
            else httpGet.addHeader(headerName, request.getHeader(headerName));
        }
        CloseableHttpClient client = HttpClients.createDefault();
        System.out.println("client");
        CloseableHttpResponse response = client.execute(httpGet);
        System.out.println("response");
        HttpEntity entity = response.getEntity();
        if (entity == null) return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);;
        HttpHeaders httpheaders = new HttpHeaders();
        for(Header header : response.getAllHeaders()) {
            httpheaders.add(header.getName(), header.getValue());
        }
        System.out.println("entity");
        String result = EntityUtils.toString(entity);
        StatusLine sl = response.getStatusLine();
        if (sl.getStatusCode() > 299) return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        client.close();
        return new ResponseEntity<>(result, httpheaders, HttpStatus.OK);
    }
}
