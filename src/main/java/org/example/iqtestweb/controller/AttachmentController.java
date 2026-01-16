package org.example.iqtestweb.controller;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.Attachment;
import org.example.iqtestweb.service.AttachmentService;
import org.example.iqtestweb.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Attachment> uploadFile(@RequestParam("file") MultipartFile file) {
        Attachment attachment = attachmentService.saveAttachment(file);
        return ResponseEntity.ok(attachment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getAttachment(@PathVariable Long id, HttpServletRequest request) {
        Attachment attachment = attachmentService.getAttachment(id);
        Resource resource = fileStorageService.loadFileAsResource(attachment.getName());

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // logger.info("Could not determine file type.");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
