package com.coedit.controller;

import com.coedit.common.Result;
import com.coedit.common.UserSessionManager;
import com.coedit.dto.DocumentDto;
import com.coedit.dto.UserDto;
import com.coedit.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/document")
@CrossOrigin(origins = "*")
public class DocumentController extends BaseController {

    @Autowired
    private DocumentService documentService;


    @PostMapping("/create")
    public Result<DocumentDto> create(@RequestBody DocumentDto documentDto, HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        return Result.success(documentService.createDocument(documentDto, user.getId()));
    }

    @GetMapping("/{id}")
    public Result<DocumentDto> get(@PathVariable Long id, HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        return Result.success(documentService.getDocument(id, user.getId()));
    }

    @PutMapping("/{id}")
    public Result<DocumentDto> update(@PathVariable Long id, @RequestBody DocumentDto documentDto, HttpServletRequest request) {
        UserDto user = getUser(request);
        Long userId = (user != null) ? user.getId() : 0L;
        return Result.success(documentService.updateDocument(id, documentDto, userId));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        documentService.deleteDocument(id, user.getId());
        return Result.success("Deleted successfully");
    }

    @GetMapping("/list")
    public Result<List<DocumentDto>> list(HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        return Result.success(documentService.getUserDocuments(user.getId()));
    }

    @GetMapping("/search")
    public Result<List<DocumentDto>> search(@RequestParam String keyword) {
        return Result.success(documentService.searchDocuments(keyword));
    }

    @PostMapping("/move")
    public Result<String> move(@RequestBody Map<String, Long> payload, HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        Long docId = payload.get("docId");
        Long folderId = payload.get("folderId");
        documentService.moveDocumentToFolder(docId, folderId, user.getId());
        return Result.success("Moved successfully");
    }

    @PostMapping("/search/advanced")
    public Result<List<DocumentDto>> searchAdvanced(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        
        params.put("userId", user.getId());
        return Result.success(documentService.searchDocumentsAdvanced(params));
    }
}
