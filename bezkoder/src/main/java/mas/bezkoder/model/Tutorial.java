package mas.bezkoder.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime; // import the LocalDateTime class


@Document(collection = "tutorials")
public class Tutorial {
    @Id
    private String id;
    private String title;
    private String description;
    private String domain;
    private String filetype;
    private LocalDateTime dateTime;

    public Tutorial() {

    }

    public Tutorial(String title, String description, String domain, String filetype) {
        this.title = title;
        this.description = description;
        this.filetype = filetype;
        this.domain = domain;
        this.dateTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
      }
    
      public String getTitle() {
        return title;
      }
    
      public void setTitle(String title) {
        this.title = title;
      }
    
      public String getDescription() {
        return description;
      }
    
      public void setDescription(String description) {
        this.description = description;
      }

      public String getDomain() {
          return domain;
      }

      public void setDomain(String domain) {
          this.domain = domain;
      }

      @Override
      public String toString() {
        return "Tutorial [id=" + id + ", title=" + title + ", desc=" + description + "]";
      }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}