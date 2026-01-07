package com.coedit.service;

import com.coedit.entity.Folder;
import java.util.List;

public interface FolderService {
    Folder createFolder(String name, Long userId);
    void deleteFolder(Long folderId, Long userId);
    void updateFolder(Long folderId, String name, Long userId);
    List<Folder> getUserFolders(Long userId);
}
