package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.Attachment;
import org.example.iqtestweb.repository.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    public Attachment saveAttachment(MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        Attachment attachment = Attachment.builder()
                .name(fileName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .imageUrl("uploads/images/" + fileName)
                .build();
        return attachmentRepository.save(attachment);
    }

    public Attachment saveWebAttachment(String webUrl) {
        Attachment attachment = Attachment.builder()
                .name("web_image") // Generic name or extract from URL
                .contentType("image/jpeg") // Default or detect
                .size(0) // Unknown size
                .webUrl(webUrl)
                .build();
        return attachmentRepository.save(attachment);
    }

    public Attachment getAttachment(Long id) {
        return attachmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Attachment not found"));
    }
}
