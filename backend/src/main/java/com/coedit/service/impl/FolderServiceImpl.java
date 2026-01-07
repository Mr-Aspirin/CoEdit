package com.coedit.service.impl;

import com.coedit.entity.Folder;
import com.coedit.mapper.FolderMapper;
import com.coedit.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FolderServiceImpl implements FolderService {
    @Autowired
    private FolderMapper folderMapper;

    @Override
    public Folder createFolder(String name, Long userId) {
        Folder folder = new Folder();
        folder.setName(name);
        folder.setUserId(userId);
        folderMapper.insert(folder);
        return folder;
    }

    @Override
    public void deleteFolder(Long folderId, Long userId) {
        Folder folder = folderMapper.findById(folderId);
        if (folder != null && folder.getUserId().equals(userId)) {
            folderMapper.deleteById(folderId);
        }
    }

    @Override
    public void updateFolder(Long folderId, String name, Long userId) {
        Folder folder = folderMapper.findById(folderId);
        if (folder != null && folder.getUserId().equals(userId)) {
            folder.setName(name);
            folderMapper.update(folder);
        }
    }

    @Override
    public List<Folder> getUserFolders(Long userId) {
        return folderMapper.findByUserId(userId);
    }
}
