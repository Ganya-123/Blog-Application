package com.maveric.blog.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import com.maveric.blog.entity.Category;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryTest {

  @Mock private CategoryRepository categoryRepository;

  private Category category;

  @BeforeEach
  void setUp() {
    category = new Category();
    category.setId(1L);
    category.setName("Technology");
  }

  @Test
  void whenFindByName_thenReturnCategory() {
    when(categoryRepository.findByName("Technology")).thenReturn(Optional.of(category));
    Optional<Category> foundCategory = categoryRepository.findByName("Technology");
    assertEquals("Technology", foundCategory.get().getName());
  }

  @Test
  void whenFindByName_thenReturnEmpty() {
    when(categoryRepository.findByName("NonExistentCategory")).thenReturn(Optional.empty());
    Optional<Category> foundCategory = categoryRepository.findByName("NonExistentCategory");
    assertFalse(foundCategory.isPresent());
  }
}
