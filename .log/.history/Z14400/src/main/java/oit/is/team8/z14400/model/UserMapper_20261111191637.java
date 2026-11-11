package oit.is.team8.z14400.model;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
  @Select("SELECT id, username, password FROM users WHERE username = #{username}")
  User findByUsername(String username);

  @Insert("INSERT INTO users (username, password) VALUES (#{username}, #{password})")
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  int insert(User user);
}
