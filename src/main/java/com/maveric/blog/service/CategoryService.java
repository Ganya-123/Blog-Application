package com.maveric.blog.service;

import com.maveric.blog.entity.Category;
import com.maveric.blog.exceptions.CategoryExistsException;
import com.maveric.blog.exceptions.CategoryNotFoundException;
import com.maveric.blog.repository.CategoryRepository;
import com.maveric.blog.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  public Category createCategory(Category category) {
    categoryRepository
        .findByName(category.getName())
        .ifPresent(
            existingCategory -> {
              throw new CategoryExistsException(Constants.CATEGORY_EXISTS);
            });
    return categoryRepository.save(category);
  }

  public String deleteCategory(Long id) {
    Category category =
        categoryRepository
            .findById(id)
            .orElseThrow(() -> new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));
    categoryRepository.delete(category);
    return Constants.CATEGORY_DELETE_SUCCESS;
  }
}
