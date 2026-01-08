package com.coedit.controller;

import com.coedit.common.Result;
import com.coedit.common.UserSessionManager;
import com.coedit.dto.UserDto;
import com.coedit.entity.Folder;
import com.coedit.service.FolderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/folder")
@CrossOrigin(origins = "*")
public class FolderController extends BaseController {
    @Autowired
    private FolderService folderService;

    @PostMapping("/create")
    public Result<Folder> create(@RequestBody Folder folder, HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        return Result.success(folderService.createFolder(folder.getName(), user.getId()));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        folderService.deleteFolder(id, user.getId());
        return Result.success("Deleted successfully");
    }
    
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Folder folder, HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        folderService.updateFolder(id, folder.getName(), user.getId());
        return Result.success("Updated successfully");
    }

    @GetMapping("/list")
    public Result<List<Folder>> list(HttpServletRequest request) {
        UserDto user = getUser(request);
        if (user == null) return Result.error(401, "Not logged in");
        return Result.success(folderService.getUserFolders(user.getId()));
    }
}
