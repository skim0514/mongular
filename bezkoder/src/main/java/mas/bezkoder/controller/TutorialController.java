package mas.bezkoder.controller;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import mas.bezkoder.compare.CompareHTML;
import mas.bezkoder.crawler.CrawlMain;
import mas.bezkoder.parser.ParseMain;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mas.bezkoder.model.Tutorial;
import mas.bezkoder.repository.TutorialRepository;

import static java.lang.Math.abs;
import static java.nio.file.Files.deleteIfExists;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.outerj.daisy.diff.Main.main2;


@CrossOrigin(origins = {"http://118.67.133.84:4200", "http://localhost:4200", "http://0.0.0.0:4200"})
@RestController
@RequestMapping("/api")
public class TutorialController {

  @Autowired
  TutorialRepository tutorialRepository;

  @GetMapping("/tutorials")
  public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
    try {
      List<Tutorial> tutorials = new ArrayList<>();
      if (title == null)
        tutorialRepository.findAll().forEach(tutorials::add);
      else
        tutorialRepository.findByTitleContaining(title).forEach(tutorials::add);
      if (tutorials.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(tutorials, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/tutorials/id/{id}")
  public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") String id) {
    Optional<Tutorial> tutorialData = tutorialRepository.findById(id);
    return tutorialData.map(tutorial -> new ResponseEntity<>(tutorial, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping("/tutorials")
  public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
    try {
      Tutorial _tutorial = tutorialRepository.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(), tutorial.getSha(),
              tutorial.getDomain(), tutorial.getFiletype(), tutorial.getContentType(), tutorial.getContentEncoding()));
      return new ResponseEntity<>(_tutorial, HttpStatus.CREATED);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/tutorials/{id}")
  public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") String id, @RequestBody Tutorial tutorial) {
    Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

    if (tutorialData.isPresent()) {
      Tutorial _tutorial = tutorialData.get();
      _tutorial.setTitle(tutorial.getTitle());
      _tutorial.setDescription(tutorial.getDescription());
      _tutorial.setFiletype(tutorial.getFiletype());
      return new ResponseEntity<>(tutorialRepository.save(_tutorial), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/tutorials/{id}")
  public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") String id) {
    try {
      tutorialRepository.deleteById(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/websites")
  public ResponseEntity<HttpStatus> deleteWebsite(@RequestParam("web") String website) {
    try {
      String url;
      while (true) {
          url = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
          if (url.equals(website)) break;
          else website = url;
      }
      List<Tutorial> tutorials = new ArrayList<>();
      tutorialRepository.findByTitleContaining(website).forEach(tutorials::add);
      for (Tutorial tutorial: tutorials) {
        deleteTutorial(tutorial.getId());
      }
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/tutorials")
  public ResponseEntity<HttpStatus> deleteAllTutorials() {
    try {
      tutorialRepository.deleteAll();
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/comparison")
  public ResponseEntity<?> getComparison(@RequestParam("web") String website, @RequestParam("prev") String prev,
                                         @RequestParam(name = "next", required = false) String next) throws JSONException, IOException, URISyntaxException, InterruptedException {
    String url = "";
    try {
      while (true) {
        url = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
        if (url.equals(website)) break;
        else website = url;
      }
    } catch (UnsupportedEncodingException e) {
      // not going to happen - value came from JDK's own StandardCharsets
    }
    Tutorial tutorial1;
    Tutorial tutorial2;
    try {
      tutorial1 = getTutorial(url, prev);
      tutorial2 = getTutorial(url, next);
      if (tutorial1 == null || tutorial2 == null) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    ResponseEntity<?> re1 = getFileFromWebsite(website, prev);
    ResponseEntity<?> re2 = getFileFromWebsite(website, next);

    InputStream is1 = new ByteArrayInputStream((byte[]) Objects.requireNonNull(re1.getBody()));
    InputStream is2 = new ByteArrayInputStream((byte[]) Objects.requireNonNull(re2.getBody()));

    InputStream is3 = getInputStream(tutorial1);
    InputStream is4 = getInputStream(tutorial2);

    String result = main2(is1, is2);

    String tf1 = IOUtils.toString(is3, StandardCharsets.UTF_8.name());

    System.out.println(tf1);

    Document doc = Jsoup.parse(tf1);


    Document rep = Jsoup.parse(result);
    doc.body().replaceWith(rep.body());
//
    Element rephead = rep.head();
    Elements css = rephead.select("link[href]");
    for (Element cssHold: css) {
      String diff = css.attr("href");
      cssHold.attr("href", "http://localhost:8082/" + diff);
      cssHold.appendTo(doc.head());
    }

    return new ResponseEntity<>(doc.toString(), re1.getHeaders(), HttpStatus.OK);
  }

  @GetMapping("/websites")
  public ResponseEntity<?> getFileFromWebsite(@RequestParam("web") String website, @RequestParam(name = "date",
          required = false) String date) throws IOException, URISyntaxException, JSONException {
    String url = "";
    try {
      while (true) {
        url = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
        if (url.equals(website)) break;
        else website = url;
      }

    } catch (UnsupportedEncodingException e) {
      // not going to happen - value came from JDK's own StandardCharsets
    }
    Tutorial tutorial;
    try {
      tutorial = getTutorial(url, date);
      if (tutorial == null) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    HttpHeaders headers = new HttpHeaders();
    String filetype = tutorial.getContentType().split(";")[0];
    headers.set("content-type", filetype + "; charset=UTF-8");
    InputStream is = getInputStream(tutorial);
    byte[] byteArray;
    if (is == null) {
      return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    if (tutorial.getFiletype().equals("html") || tutorial.getFiletype().equals("css") || tutorial.getFiletype().equals("js")) {
      String file;
      try {
        file = getTextFile(is, tutorial);
      } catch (IOException e) {
        e.printStackTrace();
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
      }
      file = ParseMain.parseFile(file, tutorial, date);
      byteArray = file.getBytes();
    } else {
      byteArray = IOUtils.toByteArray(is);
    }
    return new ResponseEntity<>(byteArray, headers, HttpStatus.OK);
  }

  public static InputStream getInputStream (Tutorial tutorial) throws IOException {
    URL url;
    try {
      url = new URL("http://localhost:8085/api/file/" + tutorial.getSha());
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return null;
    }
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("content-type", tutorial.getContentType());
    return con.getInputStream();
  }

  public Tutorial getTutorial(String website, String dt) throws IOException {

    website = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());

    List<Tutorial> tutorials = new ArrayList<>();
    Tutorial toReturn = null;

    LocalDateTime time;
    int dif = Integer.MAX_VALUE;
    if (dt == null) {
      time = LocalDateTime.now();
    } else {
      LocalDate date = LocalDate.parse(dt, DateTimeFormatter.BASIC_ISO_DATE);
      time = LocalDateTime.of(date, LocalTime.MIN);
    }

    if (website != null) {
      tutorialRepository.findByTitleContaining(website).forEach(tutorials::add);
    } else return null;
    if (tutorials.size() != 0) {
      for (Tutorial t : tutorials) {
        if (t.getTitle().equals(website)) {
          if (toReturn == null) toReturn = t;
          else if (abs((int) t.getDateTime().until(time, HOURS)) < dif) {
            dif = abs((int) t.getDateTime().until(time, HOURS));
            toReturn = t;
          }
        }
      }
    }
    if (toReturn != null) {
      System.out.println(toReturn.getId());
      return toReturn;
    }
    return null;
  }


  /**
   * getting content of text file from the input stream and the tutorial
   * @param inputstream file content in InputStream format
   * @param tutorial metadata about incoming stream
   * @return decoded content of text style file
   * @throws IOException if badly build input stream or tutorial is inputed
   */
  public static String getTextFile(InputStream inputstream, Tutorial tutorial) throws IOException {

    BufferedReader in = new BufferedReader(
            new InputStreamReader(inputstream, tutorial.getContentEncoding().toUpperCase()));
    String inputLine;
    StringBuffer content = new StringBuffer();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }
    in.close();
    return content.toString();
  }

  @PostMapping("/websites")
  public ResponseEntity<?> postFileFromWebsite(@RequestParam("web") String website) throws IOException, JSONException, URISyntaxException {
    CrawlMain.run(website);
    System.out.println("done");
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/websites/dates")
  public ResponseEntity<?> getDatesFromWebsite(@RequestParam("web") String website) throws IOException, JSONException, URISyntaxException {
    List<String> tutorials = new ArrayList<>();
    if (website != null) {
      List<Tutorial> tuts = tutorialRepository.findByTitleContaining(website);
      if (tuts.size() == 0) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      for (Tutorial tut: tuts) {
        if (tut.getTitle().equals(website)) {
          tutorials.add(tut.getDateTime().toLocalDate().toString());
        }
      }
    }
    return new ResponseEntity<>(tutorials, HttpStatus.OK);
  }

  public static void main (String[] args) throws IOException, URISyntaxException, JSONException {
    LocalDate t = LocalDate.parse("20111203", DateTimeFormatter.BASIC_ISO_DATE);
    LocalDateTime ti = LocalDateTime.of(t, LocalTime.MIDNIGHT);
    LocalDateTime t2 = LocalDateTime.parse("2007-12-03T10:15:30");
    System.out.print(Math.abs((int) ti.until(t2, HOURS)));
  }
}





