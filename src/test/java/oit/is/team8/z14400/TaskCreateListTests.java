package oit.is.team8.z14400;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class TaskCreateListTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private GroupMapper groupMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private TaskMapper taskMapper;

  @Test
  @WithMockUser(username = "task-user")
  void 登録したタスクを一覧で確認できる() throws Exception {
    Group group = createGroup("task-user");
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "仕様書を作成する",
              "assignee": "task-user",
              "status": "未着手",
              "startAt": "2026-11-15T09:00:00",
              "endAt": "2026-11-15T12:00:00"
            }
            """.formatted(group.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content", is("仕様書を作成する")))
        .andExpect(jsonPath("$.assignee", is("task-user")))
        .andExpect(jsonPath("$.status", is("未着手")))
        .andExpect(jsonPath("$.startAt", is("2026-11-15T09:00:00")))
        .andExpect(jsonPath("$.endAt", is("2026-11-15T12:00:00")));

    mockMvc.perform(get("/api/tasks").param("groupId", group.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].content", is("仕様書を作成する")))
        .andExpect(jsonPath("$[0].assignee", is("task-user")))
        .andExpect(jsonPath("$[0].status", is("未着手")))
        .andExpect(jsonPath("$[0].startAt", is("2026-11-15T09:00:00")))
        .andExpect(jsonPath("$[0].endAt", is("2026-11-15T12:00:00")));
  }

  @Test
  @WithMockUser(username = "task-user")
  void 日時を任意に入力してタスクを登録できる() throws Exception {
    Group group = createGroup("task-user");
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "タスクA",
              "assignee": "task-user",
              "status": "未着手",
              "startAt": "2026-11-15T09:00:00",
              "endAt": "2026-11-15T12:00:00"
            }
            """.formatted(group.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content", is("タスクA")))
        .andExpect(jsonPath("$.assignee", is("task-user")))
        .andExpect(jsonPath("$.status", is("未着手")))
        .andExpect(jsonPath("$.startAt", is("2026-11-15T09:00:00")))
        .andExpect(jsonPath("$.endAt", is("2026-11-15T12:00:00")));

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "タスクB",
              "assignee": "task-user",
              "status": "未着手",
              "startAt": null,
              "endAt": null
            }
            """.formatted(group.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content", is("タスクB")))
        .andExpect(jsonPath("$.assignee", is("task-user")))
        .andExpect(jsonPath("$.status", is("未着手")))
        .andExpect(jsonPath("$.startAt", nullValue()))
        .andExpect(jsonPath("$.endAt", nullValue()));

    mockMvc.perform(get("/api/tasks").param("groupId", group.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[1].content", is("タスクB")))
        .andExpect(jsonPath("$[1].assignee", is("task-user")))
        .andExpect(jsonPath("$[1].status", is("未着手")))
        .andExpect(jsonPath("$[1].startAt", nullValue()))
        .andExpect(jsonPath("$[1].endAt", nullValue()));
  }

  @Test
  @WithMockUser(username = "task-user")
  void タスクの編集と更新ができる() throws Exception {
    Group group = createGroup("task-user");
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "タスクA",
              "assignee": "task-user",
              "status": "未着手",
              "startAt": "2026-11-15T09:00:00",
              "endAt": "2026-11-15T12:00:00"
            }
            """.formatted(group.getId())))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/tasks").param("groupId", group.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].content", is("タスクA")))
        .andExpect(jsonPath("$[0].assignee", is("task-user")))
        .andExpect(jsonPath("$[0].status", is("未着手")))
        .andExpect(jsonPath("$[0].startAt", is("2026-11-15T09:00:00")))
        .andExpect(jsonPath("$[0].endAt", is("2026-11-15T12:00:00")));

    Task createdTask = taskMapper.findAllByGroupId(group.getId()).get(0);

    mockMvc.perform(put("/api/tasks/" + createdTask.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "タスクA-更新",
              "assignee": "task-user",
              "status": "進行中",
              "startAt": "2026-11-15T10:00:00",
              "endAt": "2026-11-15T11:00:00"
            }
            """.formatted(group.getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", is("タスクA-更新")))
        .andExpect(jsonPath("$.assignee", is("task-user")))
        .andExpect(jsonPath("$.status", is("進行中")))
        .andExpect(jsonPath("$.startAt", is("2026-11-15T10:00:00")))
        .andExpect(jsonPath("$.endAt", is("2026-11-15T11:00:00")));

    mockMvc.perform(get("/api/tasks").param("groupId", group.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].content", is("タスクA-更新")))
        .andExpect(jsonPath("$[0].assignee", is("task-user")))
        .andExpect(jsonPath("$[0].status", is("進行中")))
        .andExpect(jsonPath("$[0].startAt", is("2026-11-15T10:00:00")))
        .andExpect(jsonPath("$[0].endAt", is("2026-11-15T11:00:00")));
  }

  @Test
  @WithMockUser(username = "task-user")
  void 日時未入力でタスクを登録できる() throws Exception {
    Group group = createGroup("task-user");
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "期限なしタスク",
              "assignee": "task-user",
              "status": "未着手",
              "startAt": null,
              "endAt": null
            }
            """.formatted(group.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.startAt", nullValue()))
        .andExpect(jsonPath("$.endAt", nullValue()));
  }

  @Test
  @WithMockUser(username = "task-user")
  void 開始日時だけを登録すると終了日時は期限なしになる() throws Exception {
    Group group = createGroup("task-user");
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "開始日のみ",
              "assignee": "task-user",
              "status": "未着手",
              "startAt": "2026-11-15T09:00:00",
              "endAt": null
            }
            """.formatted(group.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.startAt", is("2026-11-15T09:00:00")))
        .andExpect(jsonPath("$.endAt", nullValue()));
  }

  @Test
  @WithMockUser(username = "task-user")
  void 終了日時だけを登録すると開始日時に登録時刻が設定される() throws Exception {
    Group group = createGroup("task-user");
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "終了日のみ",
              "assignee": "task-user",
              "status": "未着手",
              "startAt": null,
              "endAt": "2099-11-15T12:00:00"
            }
            """.formatted(group.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.startAt", notNullValue()))
        .andExpect(jsonPath("$.endAt", is("2099-11-15T12:00:00")));
  }

  @Test
  @WithMockUser(username = "task-user")
  void 開始日時より前の終了日時は登録できない() throws Exception {
    Group group = createGroup("task-user");
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "逆順日時",
              "assignee": "task-user",
              "status": "未着手",
              "startAt": "2026-11-15T12:00:00",
              "endAt": "2026-11-15T09:00:00"
            }
            """.formatted(group.getId())))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "task-user")
  void 既存タスクの日時を未入力へ更新できる() throws Exception {
    Group group = createGroup("task-user");
    Task task = new Task();
    task.setGroupId(group.getId());
    task.setCreator("task-user");
    task.setContent("更新前");
    task.setAssignee("task-user");
    task.setStatus("未着手");
    task.setStartAt(java.time.LocalDateTime.of(2026, 11, 15, 9, 0));
    task.setEndAt(java.time.LocalDateTime.of(2026, 11, 15, 12, 0));
    taskMapper.insert(task);

    mockMvc.perform(put("/api/tasks/" + task.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "groupId": %d,
              "content": "更新後",
              "assignee": "task-user",
              "status": "完了",
              "startAt": null,
              "endAt": null
            }
            """.formatted(group.getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.startAt", nullValue()))
        .andExpect(jsonPath("$.endAt", nullValue()));
  }

  private Group createGroup(String username) {
    User user = new User(username, "encoded-password");
    userMapper.insert(user);
    Group group = groupMapper.save(new Group("テストグループ", user.getId()));
    groupMapper.insertMembership(group.getId(), user.getId());
    return group;
  }
}
