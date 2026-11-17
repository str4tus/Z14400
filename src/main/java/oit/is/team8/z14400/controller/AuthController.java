package oit.is.team8.z14400.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import oit.is.team8.z14400.model.User;
import oit.is.team8.z14400.model.UserMapper;

@Controller
public class AuthController {
  private static final MediaType HTML_UTF8 = new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8);

  private final UserMapper userMapper;

  public AuthController(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @GetMapping("/")
  public ResponseEntity<?> entryPage(Authentication authentication) {
    if (authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken)) {
      return ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create("/home"))
          .build();
    }
    return ResponseEntity.ok()
        .contentType(HTML_UTF8)
        .body(new ClassPathResource("static/index.html"));
  }

  @GetMapping("/home")
  public ResponseEntity<Resource> homePage() {
    return ResponseEntity.ok()
        .contentType(HTML_UTF8)
        .body(new ClassPathResource("static/index.html"));
  }

  @GetMapping("/login")
  public String loginPage() {
    return "login";
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
