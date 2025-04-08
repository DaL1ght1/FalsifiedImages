package com.pcd.imagemanagment.controller;

import com.pcd.imagemanagment.model.Image;
import com.pcd.imagemanagment.repository.ImageRepository;
import com.pcd.imagemanagment.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService imageStorageService;
    private final ImageRepository imageRepository;
    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    // --- Get Client IP (Helper) ---
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || remoteAddr.isEmpty()) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    // --- Upload Endpoint ---
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("caseId") String caseId,
            @RequestParam(value = "userId", defaultValue = "SYSTEM_UPLOAD") String userId,
            @RequestParam(value = "userRole", defaultValue = "UPLOADER") String userRole,
            HttpServletRequest request) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File cannot be empty");
        }

        try {
            String imageId = imageStorageService.storeImage(
                    file,
                    caseId,
                    userId,
                    userRole,
                    getClientIp(request)
            );
            log.info("API: Image uploaded successfully with ID: {}", imageId);
            // Return the ID of the stored image metadata document
            return ResponseEntity.status(HttpStatus.CREATED).body(imageId);
        } catch (Exception e) {
            log.error("API: Image upload failed for file: {}", file.getOriginalFilename(), e);
            // Use ResponseStatusException for cleaner error responses
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image upload failed", e);
        }
    }

    // --- Download Endpoint ---
    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadImage(
            @PathVariable String id,
            // These would typically come from SecurityContext / Authentication Principal
            @RequestParam(value = "userId", defaultValue = "SYSTEM_DOWNLOAD") String userId,
            @RequestParam(value = "userRole", defaultValue = "VIEWER") String userRole,
            @RequestParam(value = "reason", defaultValue = "Viewing") String reason,
            HttpServletRequest request) {

        try {
            Optional<ImageStorageService.ImageDownload> downloadDataOpt = imageStorageService.retrieveImage(
                    id,
                    userId,
                    userRole,
                    reason,
                    getClientIp(request)
            );

            if (downloadDataOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found with ID: " + id);
            }

            ImageStorageService.ImageDownload downloadData = downloadDataOpt.get();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(downloadData.contentType));
            headers.setContentLength(downloadData.length);
            // Suggest download with original filename
            headers.setContentDispositionFormData("attachment", downloadData.filename);

            log.info("API: Image ID: {} downloaded by User ID: {}", id, userId);
            return new ResponseEntity<>(new InputStreamResource(downloadData.inputStream), headers, HttpStatus.OK);

        } catch (IllegalStateException e) {
            // Handle cases like file missing from GridFS after metadata found
            log.error("API: Critical error downloading image ID: {}. {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve image file data", e);
        } catch (Exception e) {
            log.error("API: Error downloading image ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download image", e);
        }
    }

    // --- Get Metadata Endpoint ---
    @GetMapping("/{id}")
    public ResponseEntity<Image> getImageMetadata(@PathVariable String id) {
        return imageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image metadata not found for ID: " + id));
    }

    // --- List Images by Case ID (Example Query) ---
    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Image>> getImagesByCase(@PathVariable String caseId) {
        List<Image> images = imageRepository.findByCaseId(caseId);
        return ResponseEntity.ok(images);
    }

    // --- Delete Endpoint ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable String id,
            // These would typically come from SecurityContext / Authentication Principal
            @RequestParam(value = "userId", defaultValue = "SYSTEM_DELETE") String userId,
            @RequestParam(value = "userRole", defaultValue = "ADMIN") String userRole,
            @RequestParam(value = "reason") String reason, // Make reason mandatory
            HttpServletRequest request) {
        try {
            boolean deleted = imageStorageService.deleteImage(
                    id,
                    userId,
                    userRole,
                    reason,
                    getClientIp(request)
            );
            if (deleted) {
                log.info("API: Image ID: {} marked for deletion by User ID: {}", id, userId);
                return ResponseEntity.noContent().build(); // Standard 204 No Content for successful DELETE
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found for deletion with ID: " + id);
            }
        } catch (Exception e) {
            log.error("API: Failed to delete image ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image", e);
        }
    }



    @PutMapping("/{id}/analysis-status")
    public ResponseEntity<Void> updateAnalysisStatus(
            @PathVariable String id,
            @RequestParam("status") String status,
            // These should ideally identify the calling service or system user
            @RequestParam(value = "userId", defaultValue = "SYSTEM_UPDATE") String userId,
            @RequestParam(value = "userRole", defaultValue = "SERVICE") String userRole,
            HttpServletRequest request) {

        log.info("API: Received request to update analysis status for image ID: {} to status: {} by User ID: {}", id, status, userId);

        try {
            boolean updated = imageStorageService.updateImageAnalysisStatus(
                    id,
                    status,
                    userId,
                    userRole,
                    getClientIp(request) // Pass client IP for auditing
            );

            if (updated) {
                log.info("API: Successfully updated analysis status for image ID: {} to {}", id, status);
                return ResponseEntity.ok().build(); // 200 OK is suitable for successful update
            } else {
                // If service returns false, it means the image was not found
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found with ID: " + id);
            }
        } catch (IllegalArgumentException e) {
            log.warn("API: Invalid status update request for image ID: {}. Error: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); // 400 for bad input status
        }
        catch (Exception e) {
            log.error("API: Failed to update analysis status for image ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update analysis status", e);
        }
    }
}