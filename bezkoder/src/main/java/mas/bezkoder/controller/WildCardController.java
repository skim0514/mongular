package mas.bezkoder.controller;

import mas.bezkoder.repository.TutorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class WildCardController {
    @Autowired
    TutorialRepository tutorialRepository;

    @GetMapping("**")
    public ResponseEntity<?> getRandom(HttpServletRequest request) {
        System.out.println(request.getRequestURI());
        System.out.println(request.getServletPath());
        System.out.println(request.getRequestURL());
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);

    }
}
