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

  private User createUser(String username) {
    User user = new User(username, "encoded-password");
    userMapper.insert(user);
    return user;
  }
}
