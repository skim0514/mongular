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
    private String contentType;
    private String contentEncoding;
    private LocalDateTime dateTime;

    /**
     * Tutorial Constructor
     * @param title url
     * @param description file information
     * @param domain file domain
     * @param filetype file type
     * @param contentType http request content-type
     * @param contentEncoding http request encoding
     */

    public Tutorial(String title, String description, String domain, String filetype, String contentType, String contentEncoding) {
        this.title = title;
        this.description = description;
        this.filetype = filetype;
        this.domain = domain;
        this.dateTime = LocalDateTime.now();
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }
}