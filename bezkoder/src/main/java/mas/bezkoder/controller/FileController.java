package mas.bezkoder.controller;

import com.mongodb.MongoWriteException;
import mas.bezkoder.model.Storage;
import mas.bezkoder.repository.FileService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class FileController {
    @Autowired
    FileService fileService;

//    @Autowired
//    public FileController(FileService fileService) {
//        this.fileService = fileService;
//    }

    @GetMapping("/file/{id}")
    public byte[] getFile(@PathVariable String id, Model model) throws Exception {
        Storage storage = fileService.getFile(id);
        model.addAttribute("title", storage.getTitle());
        model.addAttribute("url", "/file/stream/" + id);
        return IOUtils.toByteArray(storage.getStream());
    }

    @PostMapping("/file/add")
    public String addVideo(@RequestParam("title") String title,
                           @RequestParam("file") MultipartFile file, Model model) throws IOException, MongoWriteException {

        String id = fileService.addFile(title, file);
        return id;
    }

}
