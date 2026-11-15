package oit.is.team8.z14400;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskMapper taskMapper;

  public TaskController(TaskMapper taskMapper) {
    this.taskMapper = taskMapper;
  }

  @GetMapping
  public List<Task> list() {
    return taskMapper.findAll();
  }

  @GetMapping("/csrf")
  public Map<String, String> csrfToken(CsrfToken csrfToken) {
    return Map.of("token", csrfToken.getToken());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Task create(@RequestBody Task task) {
    taskMapper.insert(task);
    return task;
  }
}