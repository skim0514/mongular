package mas.bezkoder.controller;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

import javax.script.ScriptException;

import static java.lang.Math.abs;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.outerj.daisy.diff.Main.compareStreams;


/**
 * Controller for Spring Backend
 * Command: mvn spring-boot:run
 * Dependencies in pom.xml
 * dbconfig in application.properties
 */
@CrossOrigin(origins = {"http://118.67.133.84:4200", "http://localhost:4200", "http://0.0.0.0:4200"})
@RestController
@RequestMapping("/api")
public class TutorialController {


  /**
   * use setBlackListArray to edit blacklist array
   */
  private String[] blacklistArray  = {"www.youtube.com"};
  private final HashSet<String> blacklist = new HashSet<>(Arrays.asList(blacklistArray));


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

  /**
   * Comparison function comparing website prev from current or next
   * @param website website to compare two dates
   * @param prev date we are comparing to
   * @param next Optional date we are comparing with
   * @return response entity containing bits and header of html file already parsed
   */
  @GetMapping("/comparison")
  public ResponseEntity<?> getComparison(@RequestParam("web") String website, @RequestParam("prev") String prev,
                                         @RequestParam(name = "next", required = false) String next) throws JSONException, IOException, URISyntaxException, ScriptException, NoSuchMethodException {
    try {
      website = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      // not going to happen - value came from JDK's own StandardCharsets
    }
    Tutorial tutorial1;
    Tutorial tutorial2;
    try {
      tutorial1 = getTutorial(website, prev);
      tutorial2 = getTutorial(website, next);
      if (tutorial1 == null || tutorial2 == null) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    HttpHeaders headers = new HttpHeaders();
    String filetype = tutorial1.getContentType().split(";")[0];
    headers.set("content-type", filetype + "; charset=UTF-8");

    InputStream is1 = getInputStream(tutorial1);
    String file1 = getTextFile(is1, tutorial1);
    InputStream is2 = getInputStream(tutorial2);
    String file2 = getTextFile(is2, tutorial1);

    InputStream target1 = new ByteArrayInputStream(file1.getBytes());
    InputStream target2 = new ByteArrayInputStream(file2.getBytes());
    InputStream is3 = getInputStream(tutorial1);

    String result = compareStreams(target1, target2);
    Document rep = Jsoup.parse(result);
    Element rephead = rep.head();

    String tf1 = getTextFile(is3, tutorial1);
    tf1 = ParseMain.parseFile(tf1, tutorial1, prev);
    result = ParseMain.parseFile(result, tutorial1, prev);

    Document doc = Jsoup.parse(tf1);
    rep = Jsoup.parse(result);
    doc.body().replaceWith(rep.body());
//

    Elements css = rephead.select("link[href]");
    for (Element cssHold: css) {
      String diff = css.attr("href");
      cssHold.attr("href", "http://localhost:8082/" + diff);
      cssHold.appendTo(doc.head());
    }
    byte[] byteArray = doc.toString().getBytes();
    return new ResponseEntity<>(byteArray, headers, HttpStatus.OK);
  }


  /**
   * Main API call to retrieve a parsed website.
   * @param website a given absolute path
   * @param date date of retrieval, will give closest date - optional
   * @return a response entity containing a body and header for browser to interpret
   */
  @GetMapping("/websites")
  public ResponseEntity<?> getFileFromWebsite(@RequestParam("web") String website, @RequestParam(name = "date",
          required = false) String date) throws IOException, URISyntaxException, JSONException, ScriptException, NoSuchMethodException {
    try {
        website = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      // not going to happen - value came from JDK's own StandardCharsets
    }
    URI uri = new URI(website);
    String domain = uri.getHost();
    if (blacklist.contains(domain)) {
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header(HttpHeaders.LOCATION, website).build();
    }
    Tutorial tutorial;
    try {
      tutorial = getTutorial(website, date);
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

    if (tutorial.getFiletype().equals("html") || tutorial.getFiletype().equals("css") || tutorial.getFiletype().equals("js") || tutorial.getContentType().contains("text")) {
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

  /**
   * From a given metadata structure, retrieves an input stream from another api call
   * @param tutorial information about website
   * @return an input stream for the given metadata
   */
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

  /**
   * retrieves metadata about website
   * @param website website full absolute path
   * @param dt date is optional
   * @return a full tutorial - metadata structure.
   */
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

    return IOUtils.toString(inputstream, tutorial.getContentEncoding().toUpperCase());
  }

  /**
   * Function to help crawling - by calling post this will crawl the given website from the link
   * @param website - gives website link.
   * @return HTTP OK if works
   */
  @PostMapping("/websites")
  public ResponseEntity<?> postFileFromWebsite(@RequestParam("web") String website, @RequestParam(name = "date",
          required = false) String date) throws IOException, JSONException, URISyntaxException {
    CrawlMain.run(website);
    System.out.println("done");
    return new ResponseEntity<>(HttpStatus.OK);
  }


  /**
   * gets dates from db for a given website.
   * @param website given link for a website
   * @return a response entity containing an arraylist of all of the dates for this website.
   */
  @GetMapping("/websites/dates")
  public ResponseEntity<?> getDatesFromWebsite(@RequestParam("web") String website) {
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
      website = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
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

  public void setBlacklistArray(String[] blacklistArray) {
    this.blacklistArray = blacklistArray;
  }
}





