package com.maveric.blog.service;

import com.maveric.blog.constant.Constants;
import com.maveric.blog.dto.CategoryDto;
import com.maveric.blog.entity.Category;
import com.maveric.blog.exception.CategoryNotFoundException;
import com.maveric.blog.exception.CategoryPresentException;
import com.maveric.blog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final ModelMapper mapper;

    public CategoryDto createCategory(CategoryDto categoryDto) {
        Optional<Category> existingCategory = categoryRepository.findByName(categoryDto.getName());
        if (existingCategory.isPresent()) {
            throw new CategoryPresentException(Constants.CATEGORY_EXISTS);
        }

        Category category = categoryRepository.save(mapper.map(categoryDto, Category.class));
        return mapper.map(category, CategoryDto.class);
    }

    public String deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(Constants.CATEGORY_NOT_FOUND));

        categoryRepository.deleteById(id);
        return Constants.CATEGORY_DELETE_SUCCESS;
    }
}
