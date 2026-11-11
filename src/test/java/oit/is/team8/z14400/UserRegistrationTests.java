package oit.is.team8.z14400;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationTests {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserMapper userMapper;

  @Test
  void 未登録のユーザー名で登録すると完了表示になる() throws Exception {
    mockMvc.perform(post("/register")
        .param("username", "new-user")
        .param("password", "pass1234"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"))
        .andExpect(content().string(containsString("登録が完了しました")));

    User registeredUser = userMapper.findByUsername("new-user");
    org.junit.jupiter.api.Assertions.assertNotNull(registeredUser);
    org.junit.jupiter.api.Assertions.assertEquals("pass1234", registeredUser.getPassword());
  }

  @Test
  void 登録済みのユーザー名では登録せず重複表示になる() throws Exception {
    String username = "existing-user";
    userMapper.insert(new User(username, "old-password"));

    mockMvc.perform(post("/register")
        .param("username", username)
        .param("password", "new-password"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"))
        .andExpect(content().string(containsString("ユーザー名が既に登録されています")));

    User registeredUser = userMapper.findByUsername(username);
    org.junit.jupiter.api.Assertions.assertEquals("old-password", registeredUser.getPassword());
  }

  @Test
  void 登録画面を表示できる() throws Exception {
    mockMvc.perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("register"))
        .andExpect(content().string(containsString("ユーザー登録")));
  }
}
