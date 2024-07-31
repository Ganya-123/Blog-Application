package com.maveric.blog.controller;

import com.maveric.blog.dto.CategoryDto;
import com.maveric.blog.exception.CategoryNotFoundException;
import com.maveric.blog.exception.CategoryPresentException;
import com.maveric.blog.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class CategoryControllerIntegrationTesting {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        categoryDto = new CategoryDto();
        categoryDto.setName("Test Category");
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testCreateCategory_Success() throws Exception {
        when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Category\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Category"));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testCreateCategory_CategoryExists() throws Exception {
        when(categoryService.createCategory(any(CategoryDto.class)))
                .thenThrow(new CategoryPresentException("Category already exists"));

        mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Category\"}"))
                .andExpect(jsonPath("$.message").value("Category already exists"));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testDeleteCategory_Success() throws Exception {
        when(categoryService.deleteCategory(anyLong())).thenReturn("Category deleted successfully");

        mockMvc.perform(delete("/category/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("Category deleted successfully"));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testDeleteCategory_NotFound() throws Exception {
        when(categoryService.deleteCategory(anyLong()))
                .thenThrow(new CategoryNotFoundException("Category not found"));

        mockMvc.perform(delete("/category/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Category not found"));
    }
}
