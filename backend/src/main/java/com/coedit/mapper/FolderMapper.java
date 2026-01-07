package com.coedit.mapper;

import com.coedit.entity.Folder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FolderMapper {
    int insert(Folder folder);
    int deleteById(@Param("id") Long id);
    int update(Folder folder);
    Folder findById(@Param("id") Long id);
    List<Folder> findByUserId(@Param("userId") Long userId);
}
