package oit.is.team8.z14400;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
class TaskListSyncTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TaskMapper taskMapper;

  @Autowired
  private GroupMapper groupMapper;

  @Autowired
  private UserMapper userMapper;

  @Test
  void 別ユーザーの一覧に登録したタスクが反映される() throws Exception {
    Group group = createGroup("creator", "viewer");
    mockMvc.perform(post("/api/tasks")
        .with(user("creator"))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "groupId": %d,
              "content": "同期登録テスト",
              "assignee": "creator",
              "status": "未着手",
              "startAt": "2026-11-15T09:00:00",
              "endAt": "2026-11-15T12:00:00"
            }
            """.formatted(group.getId())))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/tasks").param("groupId", group.getId().toString()).with(user("viewer")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].content", hasItem("同期登録テスト")));
  }

  @Test
  void 別ユーザーの一覧に更新したタスクが反映される() throws Exception {
    Group group = createGroup("editor", "viewer");
    Task task = new Task();
    task.setGroupId(group.getId());
    task.setCreator("editor");
    task.setContent("同期更新前");
    task.setAssignee("editor");
    task.setStatus("未着手");
    task.setStartAt(java.time.LocalDateTime.of(2026, 11, 15, 9, 0));
    task.setEndAt(java.time.LocalDateTime.of(2026, 11, 15, 10, 0));
    taskMapper.insert(task);

    mockMvc.perform(put("/api/tasks/" + task.getId())
        .with(user("editor"))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "groupId": %d,
              "content": "同期更新後",
              "assignee": "updated-editor",
              "status": "進行中",
              "startAt": "2026-11-15T10:00:00",
              "endAt": "2026-11-15T11:00:00"
            }
            """.formatted(group.getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", is("同期更新後")));

    mockMvc.perform(get("/api/tasks").param("groupId", group.getId().toString()).with(user("viewer")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].content", hasItem("同期更新後")))
        .andExpect(jsonPath("$[*].status", hasItem("進行中")));
  }

  @Test
  void 複数グループでは選択したグループのタスクだけ表示される() throws Exception {
    User user = createUser("multi-group-user");
    Group firstGroup = createGroupForUser(user);
    Group secondGroup = createGroupForUser(user);

    createTask(firstGroup, "first-group-task");
    createTask(secondGroup, "second-group-task");

    mockMvc.perform(get("/api/tasks").param("groupId", firstGroup.getId().toString())
        .with(user("multi-group-user")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].content", hasItem("first-group-task")))
        .andExpect(jsonPath("$[*].content", not(hasItem("second-group-task"))));

    mockMvc.perform(get("/api/tasks").param("groupId", secondGroup.getId().toString())
        .with(user("multi-group-user")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].content", hasItem("second-group-task")))
        .andExpect(jsonPath("$[*].content", not(hasItem("first-group-task"))));
  }

  private void createTask(Group group, String content) {
    Task task = new Task();
    task.setGroupId(group.getId());
    task.setCreator("multi-group-user");
    task.setContent(content);
    task.setAssignee("multi-group-user");
    task.setStatus("未着手");
    task.setStartAt(java.time.LocalDateTime.of(2026, 11, 15, 9, 0));
    task.setEndAt(java.time.LocalDateTime.of(2026, 11, 15, 10, 0));
    taskMapper.insert(task);
  }

  private Group createGroup(String... usernames) {
    User admin = new User(usernames[0], "encoded-password");
    userMapper.insert(admin);
    Group group = groupMapper.save(new Group("同期テストグループ", admin.getId()));
    for (String username : usernames) {
      User user = username.equals(admin.getUsername()) ? admin : createUser(username);
      groupMapper.insertMembership(group.getId(), user.getId());
    }
    return group;
  }

  private User createUser(String username) {
    User user = new User(username, "encoded-password");
    userMapper.insert(user);
    return user;
  }

  private Group createGroupForUser(User user) {
    Group group = groupMapper.save(new Group("複数グループテスト", user.getId()));
    groupMapper.insertMembership(group.getId(), user.getId());
    return group;
  }
}
