package oit.is.team8.z14400.controller;

import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import oit.is.team8.z14400.model.Task;
import oit.is.team8.z14400.model.TaskMapper;
import oit.is.team8.z14400.model.Group;
import oit.is.team8.z14400.model.GroupMapper;
import oit.is.team8.z14400.model.User;
import oit.is.team8.z14400.model.UserMapper;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskMapper taskMapper;
  private final GroupMapper groupMapper;
  private final UserMapper userMapper;

  public TaskController(TaskMapper taskMapper, GroupMapper groupMapper, UserMapper userMapper) {
    this.taskMapper = taskMapper;
    this.groupMapper = groupMapper;
    this.userMapper = userMapper;
  }

  @GetMapping
  public List<Task> list(@RequestParam Long groupId, @AuthenticationPrincipal UserDetails user) {
    User currentUser = requireMembership(groupId, user);
    Group group = groupMapper.findById(groupId);
    List<Task> tasks = taskMapper.findAllByGroupId(groupId);
    tasks.forEach(task -> task.setCanDelete(canDelete(task, group, currentUser)));
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
    requireMembership(task.getGroupId(), user);
    normalizeTaskDateTime(task);
    task.setCreator(user.getUsername());
    taskMapper.insert(task);
    return task;
  }

  @PutMapping("/{id}")
  public Task update(@PathVariable Long id, @RequestBody Task task,
      @AuthenticationPrincipal UserDetails user) {
    requireMembership(task.getGroupId(), user);
    normalizeTaskDateTime(task);
    task.setId(id);
    if (taskMapper.update(task) == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return taskMapper.findById(id);
  }

  private void normalizeTaskDateTime(Task task) {
    LocalDateTime startAt = task.getStartAt();
    LocalDateTime endAt = task.getEndAt();

    if (startAt == null && endAt == null) {
      return;
    }
    if (startAt == null) {
      task.setStartAt(LocalDateTime.now());
      startAt = task.getStartAt();
    }
    if (endAt == null) {
      return;
    }
    if (endAt.isBefore(startAt)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "終了日時は開始日時以降にしてください");
    }
  }

  private User requireMembership(Long groupId, UserDetails userDetails) {
    if (groupId == null || userDetails == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "グループが必要です");
    }
    User user = userMapper.findByUsername(userDetails.getUsername());
    if (user == null || !groupMapper.isMember(groupId, user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "グループに所属していません");
    }
    return user;
  }

  private boolean canDelete(Task task, Group group, User user) {
    return user.getUsername().equals(task.getCreator())
        || user.getId().equals(group.getAdminUserId());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
    Task task = taskMapper.findById(id);
    if (task == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    User currentUser = requireMembership(task.getGroupId(), user);
    Group group = groupMapper.findById(task.getGroupId());
    if (!canDelete(task, group, currentUser)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    taskMapper.deleteById(id, task.getGroupId());
  }
}
