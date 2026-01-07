package com.coedit.controller;

import com.coedit.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "*")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "File is empty");
        }

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String filename = UUID.randomUUID().toString() + suffix;

        try {
            file.transferTo(new File(dir.getAbsolutePath() + File.separator + filename));
            // Return URL relative to server root. 
            // Assuming we map /uploads/** to file system
            return Result.success("/uploads/" + filename);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "Upload failed");
        }
    }
}
