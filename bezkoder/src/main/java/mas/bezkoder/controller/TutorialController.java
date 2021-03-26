package mas.bezkoder.controller;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import mas.bezkoder.crawler.CrawlMain;
import mas.bezkoder.parser.ParseMain;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mas.bezkoder.model.Tutorial;
import mas.bezkoder.repository.TutorialRepository;



@CrossOrigin(origins = "http://localhost:8085")
@RestController
@RequestMapping("/api")
public class TutorialController {

  @Autowired
  TutorialRepository tutorialRepository;
  private static String files = "http://localhost:8082/";
//  private static String client = "http://118.67.133.84:8085/api/websites?web=";
  private static String client = "http://localhost:8085/api/websites?web=";
//  private static String client = "http://localhost:8085/api/tutorials";
  @GetMapping("/tutorials")
  public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
    try {
      List<Tutorial> tutorials = new ArrayList<Tutorial>();
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
    if (tutorialData.isPresent()) {
      return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
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
      String url = "";
      while (true) {
          url = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
          if (url.equals(website)) break;
          else website = url;
      }
      List<Tutorial> tutorials = new ArrayList<>();
      if (website != null) {
        tutorialRepository.findByTitleContaining(website).forEach(tutorials::add);
      } else return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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

  @GetMapping("/websites")
  public ResponseEntity<?> getFileFromWebsite(@RequestParam("web") String website) throws IOException, URISyntaxException, JSONException {
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
      tutorial = getTutorial(url);
      if (tutorial == null) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    HttpHeaders headers = new HttpHeaders();
    if (Filetype.getDescription(tutorial.getFiletype()).equals("string")) {
      String file;
      try {
        file = getTextFile(tutorial);
      } catch (IOException e) {
        e.printStackTrace();
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
      }
      file = ParseMain.parseFile(file, tutorial);
      byte[] byteArray = file.getBytes(tutorial.getContentEncoding());
      headers.set("content-type", tutorial.getContentType());
      return new ResponseEntity<>(byteArray, headers, HttpStatus.OK);
    } else if (Filetype.getDescription(tutorial.getFiletype()).equals("image")) {
      byte[] image;
      headers.set("content-type", tutorial.getContentType());
      image = getImageFile(tutorial);
      return new ResponseEntity<>(image, headers, HttpStatus.OK);
    } else {
      String file;
      try {
        file = getTextFile(tutorial);
      } catch (IOException e) {
        e.printStackTrace();
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
      }
      headers.set("content-type", tutorial.getContentType());
      return new ResponseEntity<>(file, headers, HttpStatus.OK);
    }
  }

  private byte[] getImageFile(Tutorial tutorial) throws IOException {
    URL url = null;
    try {
      url = new URL("http://localhost:8085/api/file/" + tutorial.getSha());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("content-type", tutorial.getContentType());
    InputStream in = con.getInputStream();
    return IOUtils.toByteArray(in);
  }

  public Tutorial getTutorial(String website) throws IOException {
    Tutorial tutorial;
//    System.out.println("reached here" + website);
    website = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());

    List<Tutorial> tutorials = new ArrayList<>();
    Tutorial toReturn = null;

    if (website != null) {
      tutorialRepository.findByTitleContaining(website).forEach(tutorials::add);
    } else return null;
    if (tutorials.size() != 0) {
      for (Tutorial t : tutorials) {
        if (t.getTitle().equals(website)) {
          if (toReturn == null) toReturn = t;
          else if (toReturn.getDateTime().isBefore(t.getDateTime())) {
            toReturn = t;
          }
        }
      }
    }
    if (toReturn != null) return toReturn;
    tutorials = new ArrayList<>();
    URL url = new URL(website);
    String query = url.getQuery();
    if (query == null) return null;
    String begin = website.substring(0, website.indexOf(query) - 1);
    tutorialRepository.findByTitleContaining(begin).forEach(tutorials::add);
    int maxQ = -1;
    Tutorial maxTut = null;
    if (tutorials.size() == 1) return tutorials.get(0);
    else if (tutorials.size() == 0) return null;
    else {
      String[] queries = query.split("&");
      int count = 0;
      for (Tutorial t: tutorials) {
        Set<String> queriesHold = Set.of((new URL(t.getTitle())).getQuery().split("&"));
        for (String q: queries) {
          if (queriesHold.contains(q)) count++;
        }
        if (count > maxQ) {
          maxTut = t;
          maxQ = count;
        }
      }
      return maxTut;
    }
  }

  public static String getTextFile(Tutorial tutorial) throws IOException {
    URL url = null;
    try {
      url = new URL("http://localhost:8085/api/file/" + tutorial.getSha());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("content-type", tutorial.getContentType());
    int status = con.getResponseCode();
    BufferedReader in = new BufferedReader(
            new InputStreamReader(con.getInputStream(), tutorial.getContentEncoding().toUpperCase()));
    String inputLine;
    StringBuffer content = new StringBuffer();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }
    in.close();
    con.disconnect();
    String rv = content.toString();
    //currently decoded
    return rv;
  }

  @PostMapping("/websites")
  public ResponseEntity<?> postFileFromWebsite(@RequestParam("web") String website) throws IOException, JSONException, URISyntaxException {
    CrawlMain.run(website);
    System.out.println("done");
    return new ResponseEntity<>(HttpStatus.OK);
  }
}





