package mas.bezkoder.controller;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;

import mas.bezkoder.model.Tutorial;
import mas.bezkoder.repository.TutorialRepository;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {


  @Autowired
  static
  TutorialRepository tutorialRepository;

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

  @GetMapping("/tutorials/domain/{domain}")
  public ResponseEntity<List<Tutorial>> getTutorialByDomain(@PathVariable("domain") String domain) {
    try {
      List<Tutorial> tutorials = new ArrayList<Tutorial>();

      if (domain != null)
        tutorialRepository.findByDomain(domain).forEach(tutorials::add);

      if (tutorials.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(tutorials, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
//
//  @GetMapping("/tutorials/website/{domain}")
//  public ResponseEntity<List<Tutorial>> getTutorialByWebsite(@PathVariable("website") String website) {
//    try {
//      List<Tutorial> tutorials = new ArrayList<Tutorial>();
//
//      if (domain != null)
//        tutorialRepository.findByDomain(domain).forEach(tutorials::add);
//
//      if (tutorials.isEmpty()) {
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//      }
//      return new ResponseEntity<>(tutorials, HttpStatus.OK);
//    } catch (Exception e) {
//      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//  }

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

  //

  @GetMapping("/websites/{website}")
  public ResponseEntity<String> getFileFromWebsite(@PathVariable("website") String website) throws IOException {
    String url = "";
    try {
      url = java.net.URLDecoder.decode(website, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      // not going to happen - value came from JDK's own StandardCharsets
    }
    Tutorial tutorial;
    try {
      tutorial = getTutorial(url);

      if (tutorial == null) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    //getting file
    String file = getFile(tutorial);
    try {
      file = Parser.parseFile(file, tutorial);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return new ResponseEntity<>(file, HttpStatus.OK);
  }

  public static Tutorial getTutorial(String website) {
    Tutorial tutorial;

    List<Tutorial> tutorials = new ArrayList<Tutorial>();

    if (website != null)
      tutorialRepository.findByTitleContaining(website).forEach(tutorials::add);
    return tutorials.get(-1);
  }

  public String getFile(Tutorial tutorial) throws IOException {
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

//  @GetMapping(
//          name = "/tutorials/{id}",
//          produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
//  )
//  public @ResponseBody byte[] getContent(@RequestBody Tutorial tutorial) throws IOException {
//    InputStream in = URL.getFile("http://localhost:8082/" + tutorial.getId() + "." + tutorial.getFiletype());
//    if (tutorial.getFiletype() == "html") {
//      in = Parser.parseHtml(in);
//    } else if (tutorial.getFiletype() == "css") {
//      in = Parser.parseCss(in);
//    } else if (tutorial.getFiletype() == "js") {
//      in = Parser.parseJs(in);
//    }
//    return IOUtils.toByteArray(in);
//  }





}