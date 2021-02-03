package mas.bezkoder.crawler;

import mas.bezkoder.controller.TutorialController;
import mas.bezkoder.controller.TutorialController.*;
import mas.bezkoder.model.Tutorial;
import mas.bezkoder.repository.TutorialRepository;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Crawler {


    public void postTutorial(Tutorial tutorial) throws IOException {
        URL url = null;
        try {
            url = new URL("http://localhost:8080/api/tutorials/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
    }

}
