package oit.is.team8.z14400.model;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TaskMapper {

  @Insert("""
      INSERT INTO tasks (group_id, creator, content, assignee, status, start_at, end_at)
      VALUES (#{groupId}, #{creator}, #{content}, #{assignee}, #{status}, #{startAt}, #{endAt})
      """)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(Task task);

  @Select("""
      SELECT id, group_id AS groupId, creator, content, assignee, status,
          start_at AS startAt, end_at AS endAt
      FROM tasks
      WHERE group_id = #{groupId}
      ORDER BY id
      """)
  List<Task> findAllByGroupId(Long groupId);

  @Update("""
      UPDATE tasks
      SET content = #{content}, assignee = #{assignee}, status = #{status},
          start_at = #{startAt}, end_at = #{endAt}
      WHERE id = #{id} AND group_id = #{groupId}
      """)
  int update(Task task);

  @Select("""
      SELECT id, group_id AS groupId, creator, content, assignee, status,
          start_at AS startAt, end_at AS endAt
      FROM tasks WHERE id = #{id}
      """)
  Task findById(Long id);

  @Delete("DELETE FROM tasks WHERE id = #{id} AND group_id = #{groupId}")
  int deleteById(@Param("id") Long id, @Param("groupId") Long groupId);
}
