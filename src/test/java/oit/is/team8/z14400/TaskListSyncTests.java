package oit.is.team8.z14400;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
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

import oit.is.team8.z14400.model.Task;
import oit.is.team8.z14400.model.TaskMapper;

@SpringBootTest
@AutoConfigureMockMvc
class TaskListSyncTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TaskMapper taskMapper;

  @Test
  void 別ユーザーの一覧に登録したタスクが反映される() throws Exception {
    mockMvc.perform(post("/api/tasks")
        .with(user("creator"))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "content": "同期登録テスト",
              "assignee": "creator",
              "status": "未着手",
              "startAt": "2026-11-15T09:00:00",
              "endAt": "2026-11-15T12:00:00"
            }
            """))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/tasks").with(user("viewer")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].content", hasItem("同期登録テスト")));
  }

  @Test
  void 別ユーザーの一覧に更新したタスクが反映される() throws Exception {
    Task task = new Task();
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
              "content": "同期更新後",
              "assignee": "updated-editor",
              "status": "進行中",
              "startAt": "2026-11-15T10:00:00",
              "endAt": "2026-11-15T11:00:00"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", is("同期更新後")));

    mockMvc.perform(get("/api/tasks").with(user("viewer")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].content", hasItem("同期更新後")))
        .andExpect(jsonPath("$[*].status", hasItem("進行中")));
  }
}
