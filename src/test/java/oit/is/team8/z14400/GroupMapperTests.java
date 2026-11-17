package oit.is.team8.z14400;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import oit.is.team8.z14400.model.Group;
import oit.is.team8.z14400.model.GroupMapper;
import oit.is.team8.z14400.model.User;
import oit.is.team8.z14400.model.UserMapper;

@SpringBootTest
@Transactional
class GroupMapperTests {

  @Autowired
  private GroupMapper groupMapper;

  @Autowired
  private UserMapper userMapper;

  @Test
  void グループ名と管理者を指定して保存すると6桁の英数字参加ID付きでDBに保存される() {
    User admin = createUser("group-admin-1");

    Group group = new Group("開発チームA", admin.getId());
    groupMapper.save(group);

    assertNotNull(group.getId());
    Group saved = groupMapper.findById(group.getId());
    assertNotNull(saved);
    assertEquals("開発チームA", saved.getName());
    assertEquals(admin.getId(), saved.getAdminUserId());
    assertNotNull(saved.getJoinId());
    assertTrue(saved.getJoinId().matches("^[A-Za-z0-9]{6}$"));
  }

  @Test
  void グループ名が空または存在しない管理者では保存できない() {
    User admin = createUser("group-admin-invalid");

    assertThrows(IllegalArgumentException.class,
        () -> groupMapper.save(new Group("   ", admin.getId())));
    assertThrows(IllegalArgumentException.class,
        () -> groupMapper.save(new Group("開発チームB", null)));
    assertThrows(DataIntegrityViolationException.class,
        () -> groupMapper.save(new Group("存在しない管理者", -1L)));
  }

  @Test
  void 既存ユーザーのグループ所属関係を保存するとDBから確認できる() {
    User admin = createUser("group-admin-2");
    User member = createUser("group-member-2");
    User outsider = createUser("group-outsider-2");

    Group group = groupMapper.save(new Group("開発チームC", admin.getId()));
    groupMapper.insertMembership(group.getId(), admin.getId());
    groupMapper.insertMembership(group.getId(), member.getId());

    assertTrue(groupMapper.isMember(group.getId(), admin.getId()));
    assertTrue(groupMapper.isMember(group.getId(), member.getId()));
    assertFalse(groupMapper.isMember(group.getId(), outsider.getId()));

    List<User> members = groupMapper.findMembersByGroupId(group.getId());
    assertEquals(List.of("group-admin-2", "group-member-2"),
        members.stream().map(User::getUsername).toList());

    List<Group> joinedGroups = groupMapper.findGroupsByUserId(member.getId());
    assertEquals(1, joinedGroups.size());
    assertEquals(group.getId(), joinedGroups.get(0).getId());
    assertEquals(group.getJoinId(), joinedGroups.get(0).getJoinId());

    Group groupWithMembers = groupMapper.findByIdWithMembers(group.getId());
    assertNotNull(groupWithMembers);
    assertEquals(2, groupWithMembers.getMembers().size());
  }

  @Test
  void 同じユーザーを同じグループに重複して所属保存できない() {
    User admin = createUser("group-admin-dup");
    Group group = groupMapper.save(new Group("重複所属確認チーム", admin.getId()));

    groupMapper.insertMembership(group.getId(), admin.getId());

    assertThrows(DuplicateKeyException.class,
        () -> groupMapper.insertMembership(group.getId(), admin.getId()));
  }

  @Test
  void 複数グループを保存するとそれぞれの参加IDが異なる値になる() {
    User admin = createUser("group-admin-multi");
    Set<String> joinIds = new HashSet<>();
    int groupCount = 20;

    for (int i = 0; i < groupCount; i++) {
      Group saved = groupMapper.save(new Group("チーム" + i, admin.getId()));
      assertTrue(saved.getJoinId().matches("^[A-Za-z0-9]{6}$"));
      joinIds.add(saved.getJoinId());
    }

    assertEquals(groupCount, joinIds.size());
  }

  @Test
  void 既存の参加IDと重複する参加IDでは直接保存できない() {
    User admin = createUser("group-admin-join-dup");
    Group first = groupMapper.save(new Group("先行グループ", "ABC123", admin.getId()));
    assertEquals("ABC123", first.getJoinId());

    assertThrows(DuplicateKeyException.class,
        () -> groupMapper.save(new Group("後続グループ", "ABC123", admin.getId())));
  }

  @Test
  void 指定済みの参加IDが6桁英数字でなければ保存できない() {
    User admin = createUser("group-admin-join-format");

    assertThrows(IllegalArgumentException.class,
        () -> groupMapper.save(new Group("ハイフン付き", "ABC-12", admin.getId())));
    assertThrows(IllegalArgumentException.class,
        () -> groupMapper.save(new Group("短い参加ID", "short", admin.getId())));
  }

  @Test
  void Mapperのinsertを直接呼び出しても6桁英数字でない参加IDはDB制約で保存できない() {
    User admin = createUser("group-admin-db-format");
    Group invalidGroup = new Group("DB制約確認", "ABC-12", admin.getId());

    assertThrows(DataIntegrityViolationException.class,
        () -> groupMapper.insert(invalidGroup));
  }

  private User createUser(String username) {
    User user = new User(username, "encoded-password");
    userMapper.insert(user);
    return user;
  }
}
