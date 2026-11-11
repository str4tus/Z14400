package oit.is.team8.z14400.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import oit.is.team8.z14400.model.User;
import oit.is.team8.z14400.model.UserMapper;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserMapper userMapper;

  public CustomUserDetailsService(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userMapper.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("User not found: " + username);
    }
    return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
        .password("{noop}" + user.getPassword())
        .roles("USER")
        .build();
  }
}
