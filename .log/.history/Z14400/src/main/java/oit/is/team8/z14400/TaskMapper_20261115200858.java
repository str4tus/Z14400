package oit.is.team8.z14400;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskMapper {

  @Insert("""
      INSERT INTO tasks (content, assignee, status, start_at, end_at)
      VALUES (#{content}, #{assignee}, #{status}, #{startAt}, #{endAt})
      """)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(Task task);

  @Select("""
      SELECT id, content, assignee, status, start_at AS startAt, end_at AS endAt
      FROM tasks
      ORDER BY id
      """)
  List<Task> findAll();
}