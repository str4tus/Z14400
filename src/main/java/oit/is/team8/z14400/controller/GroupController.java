package oit.is.team8.z14400.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import oit.is.team8.z14400.model.Group;
import oit.is.team8.z14400.model.GroupMapper;
import oit.is.team8.z14400.model.User;
import oit.is.team8.z14400.model.UserMapper;
import oit.is.team8.z14400.model.TaskMapper;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

  private final GroupMapper groupMapper;
  private final UserMapper userMapper;
  private final TaskMapper taskMapper;

  public GroupController(GroupMapper groupMapper, UserMapper userMapper, TaskMapper taskMapper) {
    this.groupMapper = groupMapper;
    this.userMapper = userMapper;
    this.taskMapper = taskMapper;
  }

  @GetMapping("/csrf")
  public Map<String, String> csrfToken(CsrfToken csrfToken) {
    return Map.of("token", csrfToken.getToken());
  }

  @GetMapping
  public List<Group> list(@AuthenticationPrincipal UserDetails userDetails) {
    User user = requireUser(userDetails);
    List<Group> groups = groupMapper.findGroupsByUserId(user.getId());
    groups.forEach(group -> group.setCanDissolve(user.getId().equals(group.getAdminUserId())));
    return groups;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Group create(@RequestBody Map<String, String> body,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = requireUser(userDetails);
    String name = body.get("name");
    if (name == null || name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "グループ名は必須です");
    }

    Group group = groupMapper.save(new Group(name.trim(), user.getId()));
    if (groupMapper.countMembership(group.getId(), user.getId()) == 0) {
      groupMapper.insertMembership(group.getId(), user.getId());
    }
    return group;
  }

  @PostMapping("/join")
  public Group join(@RequestBody Map<String, String> body,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = requireUser(userDetails);
    String joinId = body.get("joinId");
    if (joinId == null || !joinId.matches(GroupMapper.JOIN_ID_PATTERN)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参加IDが無効です");
    }

    Group group = groupMapper.findByJoinId(joinId.toUpperCase());
    if (group == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "参加IDが無効です");
    }

    if (!groupMapper.isMember(group.getId(), user.getId())) {
      groupMapper.insertMembership(group.getId(), user.getId());
    }
    return group;
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Transactional
  public void dissolve(@PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    User user = requireUser(userDetails);
    Group group = groupMapper.findById(id);
    if (group == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    if (!group.getAdminUserId().equals(user.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    taskMapper.deleteByGroupId(id);
    groupMapper.deleteMembershipByGroupId(id);
    groupMapper.deleteById(id);
  }

  private User requireUser(UserDetails userDetails) {
    if (userDetails == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    User user = userMapper.findByUsername(userDetails.getUsername());
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return user;
  }
}
