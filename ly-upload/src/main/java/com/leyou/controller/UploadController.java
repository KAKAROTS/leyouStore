package com.leyou.controller;

import com.leyou.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
public class UploadController {
    @Autowired
    private UploadService uploadService;
    @PostMapping("image")
    public ResponseEntity<String> upload(@RequestParam("file")MultipartFile file) throws IOException {
        String url=uploadService.upload(file);
        return ResponseEntity.ok(url);
    }
}
