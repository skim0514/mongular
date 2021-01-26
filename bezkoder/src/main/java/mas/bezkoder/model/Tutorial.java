package mas.bezkoder.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tutorials")
public class Tutorial {
    @Id
    private String id;
    private String title;
    private String description;
    private String domain;
    private String filetype;
    private boolean published;

    public Tutorial() {

    }

    public Tutorial(String title, String description, String domain, String filetype, boolean published) {
        this.title = title;
        this.description = description;
        this.filetype = filetype;
        this.published = published;
        this.domain = domain;
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
    
      public boolean isPublished() {
        return published;
      }
    
      public void setPublished(boolean isPublished) {
        this.published = isPublished;
      }
    
      @Override
      public String toString() {
        return "Tutorial [id=" + id + ", title=" + title + ", desc=" + description + ", published=" + published + "]";
      }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }
}