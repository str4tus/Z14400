package oit.is.team8.z14400;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

  private Group createGroup(String username) {
    User user = new User(username, "encoded-password");
    userMapper.insert(user);
    Group group = groupMapper.save(new Group("テストグループ", user.getId()));
    groupMapper.insertMembership(group.getId(), user.getId());
    return group;
  }
}
