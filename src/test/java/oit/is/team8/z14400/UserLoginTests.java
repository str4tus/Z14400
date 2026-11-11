package oit.is.team8.z14400;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import oit.is.team8.z14400.model.User;
import oit.is.team8.z14400.model.UserMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserLoginTests {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserMapper userMapper;

  @Test
  void ログイン画面を表示できる() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("login"))
        .andExpect(content().string(containsString("ログイン")));
  }

  @Test
  void 登録済みユーザーでログインすると認証されタスク一覧へリダイレクトされる() throws Exception {
    String username = "login-success-user";
    String password = "loginpass123";
    userMapper.insert(new User(username, password));

    mockMvc.perform(formLogin("/login").user(username).password(password))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(authenticated().withUsername(username));
  }

  @Test
  void 未登録ユーザーでログインすると認証失敗しログイン画面へリダイレクトされる() throws Exception {
    mockMvc.perform(formLogin("/login").user("non-existent-user").password("dummy"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?error"))
        .andExpect(unauthenticated());

    mockMvc.perform(get("/login?error"))
        .andExpect(status().isOk())
        .andExpect(view().name("login"))
        .andExpect(content().string(containsString("ユーザー名またはパスワードが正しくありません")));
  }

  @Test
  void パスワード不一致でログインすると認証失敗しエラー表示を確認できる() throws Exception {
    String username = "login-fail-user";
    userMapper.insert(new User(username, "correct-password"));

    mockMvc.perform(formLogin("/login").user(username).password("wrong-password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?error"))
        .andExpect(unauthenticated());

    mockMvc.perform(get("/login?error"))
        .andExpect(status().isOk())
        .andExpect(view().name("login"))
        .andExpect(content().string(containsString("ユーザー名またはパスワードが正しくありません")));
  }
}
