package oit.is.team8.z14400.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import oit.is.team8.z14400.model.Task;
import oit.is.team8.z14400.model.TaskMapper;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskMapper taskMapper;

  public TaskController(TaskMapper taskMapper) {
    this.taskMapper = taskMapper;
  }

  @GetMapping
  public List<Task> list(@AuthenticationPrincipal UserDetails user) {
    List<Task> tasks = taskMapper.findAll();
    tasks.forEach(task -> task.setCanDelete(task.getCreator().equals(user.getUsername())));
    return tasks;
  }

  @GetMapping("/csrf")
  public Map<String, String> csrfToken(CsrfToken csrfToken) {
    return Map.of("token", csrfToken.getToken());
  }

  @GetMapping("/current-user")
  public Map<String, String> currentUser(@AuthenticationPrincipal UserDetails user) {
    return Map.of("username", user.getUsername());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Task create(@RequestBody Task task, @AuthenticationPrincipal UserDetails user) {
    task.setCreator(user.getUsername());
    taskMapper.insert(task);
    return task;
  }

  @PutMapping("/{id}")
  public Task update(@PathVariable Long id, @RequestBody Task task) {
    task.setId(id);
    if (taskMapper.update(task) == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return taskMapper.findById(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
    Task task = taskMapper.findById(id);
    if (task == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    if (!task.getCreator().equals(user.getUsername())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    taskMapper.deleteById(id);
  }
}
