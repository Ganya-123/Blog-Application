package com.maveric.blog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.CategoryDto;
import com.maveric.blog.entity.Category;
import com.maveric.blog.exception.CategoryNotFoundException;
import com.maveric.blog.exception.CategoryPresentException;
import com.maveric.blog.repository.CategoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;

  @Mock private ModelMapper mapper;

  @InjectMocks private CategoryService categoryService;

  private CategoryDto categoryDto;
  private Category category;

  @BeforeEach
  void setUp() {
    categoryDto = new CategoryDto();
    categoryDto.setName("Technology");

    category = new Category();
    category.setId(1L);
    category.setName("Technology");
  }

  @Test
  void testCreateCategory_Success() {
    when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
    when(mapper.map(any(CategoryDto.class), any(Class.class))).thenReturn(category);
    when(categoryRepository.save(any(Category.class))).thenReturn(category);
    when(mapper.map(any(Category.class), any(Class.class))).thenReturn(categoryDto);

    CategoryDto createdCategory = categoryService.createCategory(categoryDto);

    assertNotNull(createdCategory);
    assertEquals("Technology", createdCategory.getName());
    verify(categoryRepository, times(1)).findByName(anyString());
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  void testCreateCategory_CategoryAlreadyExists() {
    when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));

    CategoryPresentException exception =
        assertThrows(
            CategoryPresentException.class,
            () -> {
              categoryService.createCategory(categoryDto);
            });

    assertEquals(Constants.CATEGORY_EXISTS, exception.getMessage());
    verify(categoryRepository, times(1)).findByName(anyString());
    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  void testDeleteCategory_Success() {
    when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

    String result = categoryService.deleteCategory(1L);

    assertEquals(Constants.CATEGORY_DELETE_SUCCESS, result);
    verify(categoryRepository, times(1)).findById(anyLong());
    verify(categoryRepository, times(1)).deleteById(anyLong());
  }

  @Test
  void testDeleteCategory_CategoryNotFound() {
    when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

    CategoryNotFoundException exception =
        assertThrows(
            CategoryNotFoundException.class,
            () -> {
              categoryService.deleteCategory(1L);
            });

    assertEquals(Constants.CATEGORY_NOT_FOUND, exception.getMessage());
    verify(categoryRepository, times(1)).findById(anyLong());
    verify(categoryRepository, never()).deleteById(anyLong());
  }
}
