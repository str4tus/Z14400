package oit.is.team8.z14400;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import oit.is.team8.z14400.model.Group;
import oit.is.team8.z14400.model.GroupMapper;
import oit.is.team8.z14400.model.Task;
import oit.is.team8.z14400.model.TaskMapper;
import oit.is.team8.z14400.model.User;
import oit.is.team8.z14400.model.UserMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GroupControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private GroupMapper groupMapper;

  @Autowired
  private TaskMapper taskMapper;

  @Test
  @WithMockUser(username = "group-admin-controller")
  void 管理者がグループを作成すると所属一覧と参加IDが確認できる() throws Exception {
    User admin = createUser("group-admin-controller");

    mockMvc.perform(post("/api/groups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"name\":\"テストチーム\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("テストチーム")))
        .andExpect(jsonPath("$.joinId").isString())
        .andExpect(jsonPath("$.adminUserId", is(admin.getId().intValue())));

    mockMvc.perform(get("/api/groups"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is("テストチーム")))
        .andExpect(jsonPath("$[0].joinId").isString());
  }

  @Test
  @WithMockUser(username = "group-member-controller")
  void 別ユーザーが有効な参加IDで参加すると所属一覧に追加される() throws Exception {
    User member = createUser("group-member-controller");
    User admin = createUser("group-admin-for-member");
    Group group = groupMapper.save(new Group("参加テストチーム", "MEMBER", admin.getId()));
    groupMapper.insertMembership(group.getId(), admin.getId());

    mockMvc.perform(post("/api/groups/join")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"joinId\":\"MEMBER\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(group.getId().intValue())))
        .andExpect(jsonPath("$.name", is("参加テストチーム")));

    mockMvc.perform(get("/api/groups"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].joinId", is("MEMBER")));

    org.assertj.core.api.Assertions.assertThat(groupMapper.isMember(group.getId(), member.getId())).isTrue();
  }

  @Test
  @WithMockUser(username = "invalid-join-controller")
  void 存在しない参加IDでは参加せずエラーになる() throws Exception {
    createUser("invalid-join-controller");

    mockMvc.perform(post("/api/groups/join")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"joinId\":\"NOID00\"}"))
        .andExpect(status().isNotFound());

    mockMvc.perform(get("/api/groups"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockUser(username = "invalid-format-controller")
  void 参加IDの形式が不正なら参加せずエラーになる() throws Exception {
    createUser("invalid-format-controller");

    mockMvc.perform(post("/api/groups/join")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"joinId\":\"BAD-ID\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "group-admin-controller")
  void 管理者がグループを削除できる() throws Exception {
    User admin = createUser("group-admin-controller");
    Group group = groupMapper.save(new Group("削除テストチーム", "DEL123", admin.getId()));
    groupMapper.insertMembership(group.getId(), admin.getId());

    mockMvc.perform(delete("/api/groups/" + group.getId()).with(csrf()))
        .andExpect(status().isNoContent());

    org.assertj.core.api.Assertions.assertThat(groupMapper.findById(group.getId())).isNull();
  }

  @Test
  @WithMockUser(username = "group-member-dissolve")
  void メンバーはグループを解散できない() throws Exception {
    User admin = createUser("group-admin-member-denied");
    User member = createUser("group-member-dissolve");
    Group group = groupMapper.save(new Group("メンバー拒否チーム", "MEM123", admin.getId()));
    groupMapper.insertMembership(group.getId(), admin.getId());
    groupMapper.insertMembership(group.getId(), member.getId());

    mockMvc.perform(delete("/api/groups/" + group.getId()).with(csrf()))
        .andExpect(status().isForbidden());

    org.assertj.core.api.Assertions.assertThat(groupMapper.findById(group.getId())).isNotNull();
  }

  @Test
  @WithMockUser(username = "reuse-admin")
  void 解散済みグループの参加IDでは再参加できない() throws Exception {
    User admin = createUser("reuse-admin");
    Group group = groupMapper.save(new Group("再利用拒否チーム", "OLD123", admin.getId()));
    groupMapper.insertMembership(group.getId(), admin.getId());

    mockMvc.perform(delete("/api/groups/" + group.getId()).with(csrf()))
        .andExpect(status().isNoContent());

    mockMvc.perform(post("/api/groups/join")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"joinId\":\"OLD123\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "isolation-admin")
  void 解散対象の全タスクだけ削除して他グループのタスクを残す() throws Exception {
    User admin = createUser("isolation-admin");
    User otherAdmin = createUser("isolation-other-admin");
    Group target = groupMapper.save(new Group("解散対象", "TAR123", admin.getId()));
    Group other = groupMapper.save(new Group("解散対象外", "OTH123", otherAdmin.getId()));
    groupMapper.insertMembership(target.getId(), admin.getId());
    groupMapper.insertMembership(other.getId(), otherAdmin.getId());

    Task targetTask = createTask(target, "isolation-admin", "対象タスク");
    Task otherTask = createTask(other, "isolation-other-admin", "残すタスク");

    mockMvc.perform(delete("/api/groups/" + target.getId()).with(csrf()))
        .andExpect(status().isNoContent());

    org.assertj.core.api.Assertions.assertThat(taskMapper.findById(targetTask.getId())).isNull();
    org.assertj.core.api.Assertions.assertThat(taskMapper.findById(otherTask.getId())).isNotNull();
  }

  @Test
  @WithMockUser(username = "membership-admin")
  void 解散後は所属していたメンバーからグループが消える() throws Exception {
    User admin = createUser("membership-admin");
    User member = createUser("membership-member");
    Group group = groupMapper.save(new Group("所属削除チーム", "MBS123", admin.getId()));
    groupMapper.insertMembership(group.getId(), admin.getId());
    groupMapper.insertMembership(group.getId(), member.getId());

    org.assertj.core.api.Assertions.assertThat(groupMapper.findGroupsByUserId(member.getId())).hasSize(1);

    mockMvc.perform(delete("/api/groups/" + group.getId()).with(csrf()))
        .andExpect(status().isNoContent());

    org.assertj.core.api.Assertions.assertThat(groupMapper.findGroupsByUserId(member.getId())).isEmpty();
  }

  private User createUser(String username) {
    User user = new User(username, "encoded-password");
    userMapper.insert(user);
    return user;
  }

  private Task createTask(Group group, String username, String content) {
    Task task = new Task();
    task.setGroupId(group.getId());
    task.setCreator(username);
    task.setContent(content);
    task.setAssignee(username);
    task.setStatus("未着手");
    task.setStartAt(LocalDateTime.of(2026, 11, 21, 10, 0));
    task.setEndAt(LocalDateTime.of(2026, 11, 21, 11, 0));
    taskMapper.insert(task);
    return task;
  }
}
