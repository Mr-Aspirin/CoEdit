package com.coedit.controller;

import com.coedit.common.Result;
import com.coedit.common.UserSessionManager;
import com.coedit.dto.CollaboratorDto;
import com.coedit.dto.DocumentDto;
import com.coedit.dto.UserDto;
import com.coedit.entity.User;
import com.coedit.service.CollaboratorService;
import com.coedit.service.DocumentService;
import com.coedit.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collaborator")
@CrossOrigin(origins = "*")
public class CollaboratorController extends BaseController {
    @Autowired
    private CollaboratorService collaboratorService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            UserDto currentUser = getUser(request);
            if (currentUser == null) return Result.error(401, "Not logged in");

            Long documentId = Long.valueOf(payload.get("documentId").toString());
            
            Object userIdObj = payload.get("userId");
            if (userIdObj == null || userIdObj.toString().trim().isEmpty()) {
                return Result.error(400, "User ID or Account is required");
            }
            
            String identifier = userIdObj.toString().trim();
            String permission = (String) payload.getOrDefault("permission", "EDITOR");

            collaboratorService.inviteCollaborator(documentId, identifier, permission, currentUser.getId());
            return Result.success("Added successfully");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/remove")
    public Result<String> remove(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        UserDto currentUser = getUser(request);
        if (currentUser == null) return Result.error(401, "Not logged in");

        try {
            Long documentId = Long.valueOf(payload.get("documentId").toString());
            Long userId = Long.valueOf(payload.get("userId").toString()); 

            collaboratorService.removeCollaborator(documentId, userId, currentUser.getId());
            
            if (userId.equals(currentUser.getId())) {
                return Result.success("Left successfully");
            }
            return Result.success("Removed successfully");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/update-permission")
    public Result<String> updatePermission(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        UserDto currentUser = getUser(request);
        if (currentUser == null) return Result.error(401, "Not logged in");

        try {
            Long documentId = Long.valueOf(payload.get("documentId").toString());
            Long userId = Long.valueOf(payload.get("userId").toString());
            String permission = (String) payload.get("permission");
            
            collaboratorService.updatePermission(documentId, userId, permission, currentUser.getId());
            return Result.success("Permission updated successfully");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }
    
    @PostMapping("/accept")
    public Result<String> accept(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        UserDto currentUser = getUser(request);
        if (currentUser == null) return Result.error(401, "Not logged in");

        Long documentId = Long.valueOf(payload.get("documentId").toString());
        collaboratorService.acceptInvitation(documentId, currentUser.getId());
        return Result.success("Invitation accepted");
    }

    @GetMapping("/list")
    public Result<List<CollaboratorDto>> list(@RequestParam Long documentId) {
        return Result.success(collaboratorService.getCollaborators(documentId));
    }
}
