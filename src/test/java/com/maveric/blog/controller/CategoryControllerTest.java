package com.maveric.blog.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.CategoryDto;
import com.maveric.blog.exception.CategoryNotFoundException;
import com.maveric.blog.exception.CategoryPresentException;
import com.maveric.blog.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

  @InjectMocks private CategoryController categoryController;

  @Mock private CategoryService categoryService;

  private CategoryDto categoryDto;

  @BeforeEach
  void setUp() {
    categoryDto = new CategoryDto();
    categoryDto.setName("Test Category");
  }

  @Test
  void testCreateCategory_Success() {
    when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(categoryDto);

    ResponseEntity<CategoryDto> response = categoryController.createCategory(categoryDto);

    assertEquals(201, response.getStatusCodeValue());
    assertEquals("Test Category", response.getBody().getName());
    verify(categoryService, times(1)).createCategory(any(CategoryDto.class));
  }

  @Test
  void testCreateCategory_CategoryExists() {
    when(categoryService.createCategory(any(CategoryDto.class)))
        .thenThrow(new CategoryPresentException(Constants.CATEGORY_EXISTS));

    Exception exception =
        assertThrows(
            CategoryPresentException.class,
            () -> {
              categoryController.createCategory(categoryDto);
            });

    assertEquals(Constants.CATEGORY_EXISTS, exception.getMessage());
  }

  @Test
  void testDeleteCategory_Success() {
    when(categoryService.deleteCategory(anyLong())).thenReturn(Constants.CATEGORY_DELETE_SUCCESS);

    ResponseEntity<String> response = categoryController.deleteCategory(1L);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(Constants.CATEGORY_DELETE_SUCCESS, response.getBody());
    verify(categoryService, times(1)).deleteCategory(1L);
  }

  @Test
  void testDeleteCategory_NotFound() {
    when(categoryService.deleteCategory(anyLong()))
        .thenThrow(new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));

    Exception exception =
        assertThrows(
            CategoryNotFoundException.class,
            () -> {
              categoryController.deleteCategory(5L);
            });

    assertEquals(Constants.CATEGORY_NOT_FOUND, exception.getMessage());
  }
}
