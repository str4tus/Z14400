package oit.is.team8.z14400;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TaskMapper {

  @Insert("""
      INSERT INTO tasks (creator, content, assignee, status, start_at, end_at)
      VALUES (#{creator}, #{content}, #{assignee}, #{status}, #{startAt}, #{endAt})
      """)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(Task task);

  @Select("""
      SELECT id, creator, content, assignee, status, start_at AS startAt, end_at AS endAt
      FROM tasks
      ORDER BY id
      """)
  List<Task> findAll();

  @Update("""
      UPDATE tasks
      SET content = #{content}, assignee = #{assignee}, status = #{status},
          start_at = #{startAt}, end_at = #{endAt}
      WHERE id = #{id}
      """)
  int update(Task task);

  @Select("SELECT id, creator, content, assignee, status, start_at AS startAt, end_at AS endAt FROM tasks WHERE id = #{id}")
  Task findById(Long id);

  @Delete("DELETE FROM tasks WHERE id = #{id}")
  int deleteById(Long id);
}
