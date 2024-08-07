package com.maveric.blog.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import com.maveric.blog.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

  @Mock private UserRepository userRepository;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setFullName("John Doe");
    user.setMobileNumber("1234567890");
    user.setEmail("a@example.com");
    user.setPassword("password123");
  }

  @Test
  void whenFindByEmail_thenReturnUser() {

    when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

    Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

    assertEquals("a@example.com", foundUser.get().getEmail());
  }

  @Test
  void whenFindByEmail_thenReturnEmpty() {
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

    assertFalse(foundUser.isPresent());
  }
}
