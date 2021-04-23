package mas.bezkoder.controller;

import mas.bezkoder.LinkExtractor.HTMLExtractor;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class CustomInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("start prehandle");
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer == null) return true;
        String requestURI = request.getRequestURI();
        System.out.println("request is" + requestURI);
        System.out.println("referer is " + referer);
        if (referer.contains("http://118.67.133.84:8085/api/websites")) {
            if (requestURI.equals("/api/websites")) return true;
            else {
                HttpResponse proxyResponse = getRandom(request);
                if (proxyResponse == null) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return false;
                }
                System.out.println("response");
                HttpEntity entity = proxyResponse.getEntity();
                if (entity == null) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return false;
                }
                for (Header header : proxyResponse.getAllHeaders()) {
                    response.setHeader(header.getName(), header.getValue());
                }
                System.out.println("entity");
                InputStream result = entity.getContent();
                StatusLine sl = proxyResponse.getStatusLine();
                response.setStatus(sl.getStatusCode());
                ServletOutputStream out = response.getOutputStream();
                IOUtils.copy(result, out);
                out.flush();
                return false;
            }
        }
        return true;
    }



    public HttpResponse getRandom(HttpServletRequest request) throws URISyntaxException, IOException {
        System.out.println("random");
        String requestURL = request.getRequestURI();
        String referer = request.getHeader(HttpHeaders.REFERER);
        final List<String> headerNames = Collections.list(request.getHeaderNames());
        if (requestURL.startsWith("/api")) requestURL = requestURL.replace("/api/", "");
        if (referer == null) return null;
        System.out.println("getReferer");
        URI url = new URI(referer);
        List<NameValuePair> params = URLEncodedUtils.parse(url, StandardCharsets.UTF_8);
        String base = null;
        String date = null;
        for (NameValuePair param : params) {
            if (param.getName().equals("date")) date = param.getValue();
            if (param.getName().equals("web")) base = param.getValue();
        }
        if (base == null || date == null) return null;
        String link = HTMLExtractor.replaceUrl(requestURL, base);
        if (link == null) return null;
        String requestString = "http://localhost:8085/api/websites?date=" + date + "&web=" + java.net.URLEncoder.encode(link, StandardCharsets.UTF_8.name());
        System.out.println("getURL " + requestString);
        HttpGet httpGet = new HttpGet(requestString);
        for (String headerName : headerNames) {
            if (headerName.equals(HttpHeaders.REFERER)) httpGet.addHeader(HttpHeaders.REFERER, referer);
            else httpGet.addHeader(headerName, request.getHeader(headerName));
        }
        HttpClient client = HttpClients.createDefault();
        System.out.println("client");
        HttpResponse response = client.execute(httpGet);
        return response;
    }
}
