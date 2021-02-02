package mas.bezkoder.controller;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mas.bezkoder.parser.Parser;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mas.bezkoder.model.Tutorial;
import mas.bezkoder.repository.TutorialRepository;

//http://localhost:8080/api/websites?web=https%253A%252F%252Fwww.s2wlab.com%252Fproducts.html

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {


  @Autowired
  TutorialRepository tutorialRepository;
  private static String files = "http://localhost:8082/";
  private static String client = "http://localhost:8080/api/websites?web=";
  private static String href = "href=\"([^\"]*)\"";
  private static String src = "src=\"([^\"]*)\"";
  private static String scriptsrc = "<script.*?src=\"(.*?)\"";

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
      Tutorial _tutorial = tutorialRepository.save(new Tutorial(tutorial.getTitle(), tutorial.getDescription(),
              tutorial.getDomain(), tutorial.getFiletype()));
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
  public ResponseEntity<?> getFileFromWebsite(@RequestParam("web") String website) throws IOException, URISyntaxException {
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
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    String fileUrl = "http://localhost:8082/" + tutorial.getId() + "." + tutorial.getFiletype();


    if (Filetype.getDescription(tutorial.getFiletype()) == "string") {
      String file = getTextFile(tutorial);
      file = Parser.parseFile(file, tutorial);
      return new ResponseEntity<>(file, HttpStatus.OK);
    } else if (Filetype.getDescription(tutorial.getFiletype()) == "image") {
      byte[] image;
      HttpHeaders headers = new HttpHeaders();
      image = getImageFile(tutorial);
      return new ResponseEntity<>(image, headers, HttpStatus.OK);
    } else {
      String file = getTextFile(tutorial);
      return new ResponseEntity<>(file, HttpStatus.OK);
    }

  }

  private byte[] getImageFile(Tutorial tutorial) throws IOException {
    URL url = null;
    try {
      url = new URL("http://localhost:8082/" + tutorial.getId() + "." + tutorial.getFiletype());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    InputStream in = con.getInputStream();
    return IOUtils.toByteArray(in);
  }

  public Tutorial getTutorial(String website) throws IOException {
    Tutorial tutorial;
//    System.out.println("reached here" + website);
    website = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());

    List<Tutorial> tutorials = new ArrayList<>();

    if (website != null) {
      tutorialRepository.findByTitleContaining(website).forEach(tutorials::add);
    }
    if (tutorials.size() != 0) return tutorials.get(tutorials.size() - 1);
    else return null;
  }

  public static String getTextFile(Tutorial tutorial) throws IOException {
    URL url = null;
    try {
      url = new URL("http://localhost:8082/" + tutorial.getId() + "." + tutorial.getFiletype());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    int status = con.getResponseCode();
    BufferedReader in = new BufferedReader(
            new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer content = new StringBuffer();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }
    in.close();
    con.disconnect();
    String rv = content.toString();
    return rv;
  }
}





