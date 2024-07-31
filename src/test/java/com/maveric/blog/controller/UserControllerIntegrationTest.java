package com.maveric.blog.controller;

import com.maveric.blog.dto.ChangePassword;
import com.maveric.blog.dto.PostRequestDto;
import com.maveric.blog.dto.RegisterResponse;
import com.maveric.blog.dto.UserUpdateRequestDto;
import com.maveric.blog.exception.AuthorValidationException;
import com.maveric.blog.exception.UserNotFoundException;
import com.maveric.blog.security.JwtService;
import com.maveric.blog.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    private UserDetails mockUserDetails;
    private PostRequestDto postRequestDto;

    @BeforeEach
    void setUp() {
        mockUserDetails = new User("testuser", "password", Collections.emptyList());
        postRequestDto = new PostRequestDto();
        when(jwtService.extractUserId(anyString())).thenReturn(1L);
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
        when(jwtService.extractUsername(anyString())).thenReturn(mockUserDetails.getUsername());
    }
    @Test
    @WithMockUser(authorities = "WRITE")
    void testChangePassword_Success() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        String token = "Bearer valid_token";
        String expectedResponse = "Password changed successfully";

        when(authenticationService.changePassword(any(ChangePassword.class), any(String.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(put("/user/PasswordChange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("{\"emailId\":\"john.doe@example.com\",\"currentPassword\":\"currentpassword123\",\"newPassword\":\"newpassword123\",\"confirmPassword\":\"newpassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }
    @Test
    @WithMockUser(authorities = "WRITE")
    void testChangePassword_UserNotFound() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        String token = "Bearer valid_token";

        when(authenticationService.changePassword(any(ChangePassword.class), any(String.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(put("/user/PasswordChange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("{\"emailId\":\"john.doe@example.com\",\"currentPassword\":\"currentpassword123\",\"newPassword\":\"newpassword123\",\"confirmPassword\":\"newpassword123\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }


    @Test
    @WithMockUser(authorities = "WRITE")
    void testChangePassword_AuthorizationFailed() throws Exception {
        ChangePassword changePassword = new ChangePassword();
        String token = "Bearer invalid_token";

        when(authenticationService.changePassword(any(ChangePassword.class), any(String.class)))
                .thenThrow(new AuthorValidationException("Authorization failed"));

        mockMvc.perform(put("/user/PasswordChange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("{\"emailId\":\"john.doe@example.com\",\"currentPassword\":\"currentpassword123\",\"newPassword\":\"newpassword123\",\"confirmPassword\":\"newpassword123\"}"))
                .andExpect(jsonPath("$.message").value("Authorization failed"));
    }


    @Test
    @WithMockUser(authorities = "WRITE")
    void testUpdateUser_Success() throws Exception {
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto();
        RegisterResponse expectedResponse = new RegisterResponse();
        expectedResponse.setFullName("John Doe");
        expectedResponse.setMobileNumber("1234567890");

        when(authenticationService.updateUser(anyLong(), any(UserUpdateRequestDto.class), any(String.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid_token")
                        .content("{\"fullName\":\"John Doe\",\"mobileNumber\":\"1234567890\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.mobileNumber").value("1234567890"));
    }

    @Test
    @WithMockUser(authorities = "WRITE")
    void testUpdateUser_UserNotFound() throws Exception {
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto();

        when(authenticationService.updateUser(anyLong(), any(UserUpdateRequestDto.class), any(String.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid_token")
                        .content("{\"fullName\":\"John Doe\",\"mobileNumber\":\"1234567890\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser(authorities = "WRITE")
    void testUpdateUser_AuthorizationFailed() throws Exception {
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto();

        when(authenticationService.updateUser(anyLong(), any(UserUpdateRequestDto.class), any(String.class)))
                .thenThrow(new AuthorValidationException("Authorization failed"));

        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer invalid_token")
                        .content("{\"fullName\":\"John Doe\",\"mobileNumber\":\"1234567890\"}"))
                .andExpect(jsonPath("$.message").value("Authorization failed"));
    }
}
