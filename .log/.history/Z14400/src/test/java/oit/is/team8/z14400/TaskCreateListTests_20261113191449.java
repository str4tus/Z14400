package oit.is.team8.z14400;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TaskCreateListTests {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockUser(username = "mames1dev-sub")
  void 登録したタスクを一覧で確認できる() throws Exception {
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .with(csrf())
        .content("""
            {
              "content": "仕様書を作成する",
              "assignee": "mames1dev-sub",
              "status": "未着手",
              "startAt": "2026-11-13T09:00:00",
              "endAt": "2026-11-13T12:00:00"
            }
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content", is("仕様書を作成する")))
        .andExpect(jsonPath("$.assignee", is("mames1dev-sub")))
        .andExpect(jsonPath("$.status", is("未着手")))
        .andExpect(jsonPath("$.startAt", is("2026-11-13T09:00:00")))
        .andExpect(jsonPath("$.endAt", is("2026-11-13T12:00:00")));

    mockMvc.perform(get("/api/tasks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].content", is("仕様書を作成する")))
        .andExpect(jsonPath("$[0].assignee", is("mames1dev-sub")))
        .andExpect(jsonPath("$[0].status", is("未着手")))
        .andExpect(jsonPath("$[0].startAt", is("2026-11-13T09:00:00")))
        .andExpect(jsonPath("$[0].endAt", is("2026-11-13T12:00:00")));
  }
}
