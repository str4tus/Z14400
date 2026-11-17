package oit.is.team8.z14400.model;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.dao.DuplicateKeyException;

@Mapper
public interface GroupMapper {

  @Insert("""
      INSERT INTO task_groups (name, join_id, admin_user_id)
      VALUES (#{name}, #{joinId}, #{adminUserId})
      """)
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  int insert(Group group);

  default Group save(Group group) {
    if (group == null || group.getName() == null || group.getName().isBlank()) {
      throw new IllegalArgumentException("グループ名は必須です");
    }
    if (group.getAdminUserId() == null) {
      throw new IllegalArgumentException("管理者は必須です");
    }
    if (group.getJoinId() != null && !group.getJoinId().isBlank()) {
      insert(group);
      return group;
    }

    // 既存グループと重複しない6桁の英数字参加IDを割り当てて保存する
    for (int attempt = 0; attempt < 10; attempt++) {
      String candidate = Group.generateJoinId();
      if (findByJoinId(candidate) == null) {
        group.setJoinId(candidate);
        try {
          insert(group);
          return group;
        } catch (DuplicateKeyException e) {
          group.setJoinId(null);
        }
      }
    }
    throw new IllegalStateException("一意な参加IDの生成に失敗しました");
  }

  @Select("""
      SELECT id, name, join_id AS joinId, admin_user_id AS adminUserId
      FROM task_groups
      WHERE id = #{id}
      """)
  Group findById(Long id);

  @Select("""
      SELECT id, name, join_id AS joinId, admin_user_id AS adminUserId
      FROM task_groups
      WHERE join_id = #{joinId}
      """)
  Group findByJoinId(String joinId);

  @Select("""
      SELECT id, name, join_id AS joinId, admin_user_id AS adminUserId
      FROM task_groups
      ORDER BY id
      """)
  List<Group> findAll();

  @Insert("""
      INSERT INTO group_memberships (group_id, user_id)
      VALUES (#{groupId}, #{userId})
      """)
  int insertMembership(@Param("groupId") Long groupId, @Param("userId") Long userId);

  @Select("""
      SELECT COUNT(*)
      FROM group_memberships
      WHERE group_id = #{groupId} AND user_id = #{userId}
      """)
  int countMembership(@Param("groupId") Long groupId, @Param("userId") Long userId);

  default boolean isMember(Long groupId, Long userId) {
    return countMembership(groupId, userId) > 0;
  }

  @Select("""
      SELECT u.id, u.username
      FROM users u
      INNER JOIN group_memberships gm ON u.id = gm.user_id
      WHERE gm.group_id = #{groupId}
      ORDER BY u.id
      """)
  List<User> findMembersByGroupId(Long groupId);

  @Select("""
      SELECT g.id, g.name, g.join_id AS joinId, g.admin_user_id AS adminUserId
      FROM task_groups g
      INNER JOIN group_memberships gm ON g.id = gm.group_id
      WHERE gm.user_id = #{userId}
      ORDER BY g.id
      """)
  List<Group> findGroupsByUserId(Long userId);

  default Group findByIdWithMembers(Long groupId) {
    Group group = findById(groupId);
    if (group != null) {
      group.setMembers(findMembersByGroupId(groupId));
    }
    return group;
  }
}
