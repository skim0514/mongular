package mas.bezkoder.controller;

import mas.bezkoder.LinkExtractor.HTMLExtractor;
import mas.bezkoder.repository.TutorialRepository;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;

import static org.apache.http.protocol.HTTP.USER_AGENT;

@RestController
public class WildCardController {
    @Autowired
    TutorialRepository tutorialRepository;

    @GetMapping("**")
    public ResponseEntity<?> getRandom(HttpServletRequest request) throws URISyntaxException, IOException {
        String requestURL = request.getRequestURI();
        String referer = request.getHeader(HttpHeaders.REFERER);

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
        String requestString = "http://118.67.133.84:8085/api/websites?date=" + date + "&web=" + link;
        System.out.println("getURL");
        HttpURLConnection con = (HttpURLConnection) new URL(requestString).openConnection();
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        if (responseCode != 200) return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            response.append("\n");
        }
        in.close();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
