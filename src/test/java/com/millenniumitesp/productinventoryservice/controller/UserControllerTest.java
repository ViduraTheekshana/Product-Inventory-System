package com.millenniumitesp.productinventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millenniumitesp.productinventoryservice.config.PaginationProperties;
import com.millenniumitesp.productinventoryservice.dto.AssignRoleRequest;
import com.millenniumitesp.productinventoryservice.dto.CreateUserRequest;
import com.millenniumitesp.productinventoryservice.dto.UserResponse;
import com.millenniumitesp.productinventoryservice.enums.Role;
import com.millenniumitesp.productinventoryservice.enums.UserStatus;
import com.millenniumitesp.productinventoryservice.exception.AuthExceptions;
import com.millenniumitesp.productinventoryservice.security.JwtAuthFilter;
import com.millenniumitesp.productinventoryservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { JwtAuthFilter.class })
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PaginationProperties paginationProperties;

    UserControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void createUser_shouldReturn201_whenValid() throws Exception {
        CreateUserRequest request = new CreateUserRequest("manager1", "pass123", Role.MANAGER);
        UserResponse response = new UserResponse(UUID.randomUUID(), "manager1", Role.MANAGER, UserStatus.ACTIVE);

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("manager1"));
    }

    @Test
    void createUser_shouldReturn409_whenUsernameTaken() throws Exception {
        CreateUserRequest request = new CreateUserRequest("manager1", "pass123", Role.MANAGER);

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new AuthExceptions.UsernameAlreadyExists("manager1"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getAllUsers_shouldReturn200_withPagedResults() throws Exception {
        UserResponse user = new UserResponse(UUID.randomUUID(), "manager1", Role.MANAGER, UserStatus.ACTIVE);
        Page<UserResponse> page = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);

        when(paginationProperties.getDefaultSize()).thenReturn(20);
        when(userService.getAllUsers(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("manager1"));
    }

    @Test
    void assignRole_shouldReturn200_whenValid() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse(id, "manager1", Role.ADMIN, UserStatus.ACTIVE);

        when(userService.assignRole(eq(id), any(AssignRoleRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/{id}/role", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignRoleRequest(Role.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(id);
    }
}