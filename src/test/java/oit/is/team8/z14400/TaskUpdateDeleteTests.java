package oit.is.team8.z14400;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import oit.is.team8.z14400.model.Task;
import oit.is.team8.z14400.model.TaskMapper;

@SpringBootTest
@AutoConfigureMockMvc
class TaskUpdateDeleteTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TaskMapper taskMapper;

  @Test
  @WithMockUser(username = "task-owner")
  void ログインユーザー名を取得できる() throws Exception {
    mockMvc.perform(get("/api/tasks/current-user"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username", is("task-owner")));
  }

  @Test
  @WithMockUser(username = "task-owner")
  void 作成者のタスク一覧だけ削除可能になる() throws Exception {
    createTask("task-owner", "自分のタスク", "未着手");
    createTask("different-user", "他人のタスク", "未着手");

    mockMvc.perform(get("/api/tasks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.creator == 'task-owner')].canDelete", everyItem(is(true))))
        .andExpect(jsonPath("$[?(@.creator == 'different-user')].canDelete", everyItem(is(false))));
  }

  @Test
  @WithMockUser(username = "task-owner")
  void 作成者はタスク内容と状態を更新できる() throws Exception {
    Task task = createTask("task-owner", "更新前", "未着手");

    mockMvc.perform(put("/api/tasks/" + task.getId())
        .with(csrf())
        .contentType("application/json")
        .content("""
            {
              "content": "更新後",
              "assignee": "updated-assignee",
              "status": "進行中",
              "startAt": "2026-11-15T10:00:00",
              "endAt": "2026-11-15T11:00:00"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", is("更新後")))
        .andExpect(jsonPath("$.assignee", is("updated-assignee")))
        .andExpect(jsonPath("$.status", is("進行中")))
        .andExpect(jsonPath("$.startAt", is("2026-11-15T10:00:00")))
        .andExpect(jsonPath("$.endAt", is("2026-11-15T11:00:00")));
  }

  @Test
  @WithMockUser(username = "task-owner")
  void 作成者は自分のタスクを削除できる() throws Exception {
    Task task = createTask("task-owner", "削除対象", "完了");

    mockMvc.perform(delete("/api/tasks/" + task.getId()).with(csrf()))
        .andExpect(status().isNoContent());

    org.junit.jupiter.api.Assertions.assertNull(taskMapper.findById(task.getId()));
  }

  @Test
  @WithMockUser(username = "different-user")
  void 作成者以外はタスクを削除できない() throws Exception {
    Task task = createTask("task-owner", "他人のタスク", "未着手");

    mockMvc.perform(delete("/api/tasks/" + task.getId()).with(csrf()))
        .andExpect(status().isForbidden());

    org.junit.jupiter.api.Assertions.assertNotNull(taskMapper.findById(task.getId()));
  }

  private Task createTask(String creator, String content, String status) {
    Task task = new Task();
    task.setCreator(creator);
    task.setContent(content);
    task.setAssignee(creator);
    task.setStatus(status);
    task.setStartAt(LocalDateTime.of(2026, 11, 15, 9, 0));
    task.setEndAt(LocalDateTime.of(2026, 11, 15, 10, 0));
    taskMapper.insert(task);
    return task;
  }
}
