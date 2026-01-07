package com.coedit.mapper;

import com.coedit.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    int insert(User user);
    int update(User user);
    User findById(Long id);
    User findByAccount(String account);
    User findByEmail(String email);
    List<User> searchUsers(@Param("keyword") String keyword);
}
