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
import org.springframework.security.test.context.support.WithMockUser;
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
  void 未認証でトップページにアクセスするとログインとユーザー登録を選べる入口ページが表示される() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"/login\"")))
        .andExpect(content().string(containsString("ログイン")))
        .andExpect(content().string(containsString("href=\"/register\"")))
        .andExpect(content().string(containsString("ユーザー登録")))
        .andExpect(unauthenticated());
  }

  @Test
  void 未認証で保護された画面にアクセスすると入口ページへリダイレクトされる() throws Exception {
    mockMvc.perform(get("/home"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"))
        .andExpect(unauthenticated());
  }

  @Test
  void ログイン画面を表示できる() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("login"))
        .andExpect(content().string(containsString("ログイン")));
  }

  @Test
  void 登録済みユーザーでログインすると認証されホーム画面へリダイレクトされる() throws Exception {
    String username = "login-success-user";
    String password = "loginpass123";
    userMapper.insert(new User(username, password));

    mockMvc.perform(formLogin("/login").user(username).password(password))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/home"))
        .andExpect(authenticated().withUsername(username));
  }

  @Test
  @WithMockUser(username = "home-user")
  void 認証済みユーザーはホーム画面を表示できトップページからもホーム画面へリダイレクトされる() throws Exception {
    mockMvc.perform(get("/home"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("タスク一覧")));

    mockMvc.perform(get("/"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/home"));
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
