package oit.is.team8.z14400;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
  private final UserMapper userMapper;

  public AuthController(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @GetMapping("/register")
  public String registerPage() {
    return "register";
  }

  @PostMapping("/register")
  public String register(
      @RequestParam String username,
      @RequestParam String password,
      ModelMap model) {
    if (userMapper.findByUsername(username) != null) {
      model.addAttribute("error", "ユーザー名が既に登録されています");
      return "register";
    }

    userMapper.insert(new User(username, password));
    model.addAttribute("success", "登録が完了しました");
    return "register";
  }
}
