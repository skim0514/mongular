package mas.bezkoder.repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoWriteException;
import com.mongodb.client.gridfs.model.GridFSFile;
import mas.bezkoder.model.Storage;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFsOperations operations;

    public String addFile(String sha256, MultipartFile file) throws IOException {
        DBObject metaData = new BasicDBObject();
        metaData.put("type", "file");
        metaData.put("title", sha256);
        ObjectId id;
        try {
            id = gridFsTemplate.store(
                    file.getInputStream(), file.getName(), file.getContentType(), metaData);
        } catch (MongoWriteException ex) {
            System.out.println("catch error");
            return null;
        }
        return id.toString();
    }

    public Storage getFile(String sha256) throws IllegalStateException, IOException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("metadata.title").is(sha256)));
        Storage storage = new Storage();
        assert file != null;
        assert file.getMetadata() != null;
        storage.setTitle(file.getMetadata().get("title").toString());
        storage.setStream(operations.getResource(file).getInputStream());
        return storage;
    }

}
