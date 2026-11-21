package oit.is.team8.z14400.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Group {

  private static final String JOIN_ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int JOIN_ID_LENGTH = 6;
  private static final SecureRandom RANDOM = new SecureRandom();

  private Long id;
  private String name;
  private String joinId;
  private Long adminUserId;
  private List<User> members = new ArrayList<>();
  private boolean canDissolve;

  public Group() {
  }

  public Group(String name, Long adminUserId) {
    this.name = name;
    this.adminUserId = adminUserId;
  }

  public Group(String name, String joinId, Long adminUserId) {
    this.name = name;
    this.joinId = joinId;
    this.adminUserId = adminUserId;
  }

  public static String generateJoinId() {
    StringBuilder builder = new StringBuilder(JOIN_ID_LENGTH);
    for (int i = 0; i < JOIN_ID_LENGTH; i++) {
      int index = RANDOM.nextInt(JOIN_ID_CHARS.length());
      builder.append(JOIN_ID_CHARS.charAt(index));
    }
    return builder.toString();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getJoinId() {
    return joinId;
  }

  public void setJoinId(String joinId) {
    this.joinId = joinId;
  }

  public Long getAdminUserId() {
    return adminUserId;
  }

  public void setAdminUserId(Long adminUserId) {
    this.adminUserId = adminUserId;
  }

  public List<User> getMembers() {
    return members;
  }

  public void setMembers(List<User> members) {
    this.members = members != null ? members : new ArrayList<>();
  }

  public boolean isCanDissolve() {
    return canDissolve;
  }

  public void setCanDissolve(boolean canDissolve) {
    this.canDissolve = canDissolve;
  }
}
