package com.millenniumitesp.productinventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millenniumitesp.productinventoryservice.config.PaginationProperties;
import com.millenniumitesp.productinventoryservice.dto.CreateProductRequest;
import com.millenniumitesp.productinventoryservice.dto.ProductResponse;
import com.millenniumitesp.productinventoryservice.enums.ProductStatus;
import com.millenniumitesp.productinventoryservice.exception.ProductExceptions;
import com.millenniumitesp.productinventoryservice.security.JwtAuthFilter;
import com.millenniumitesp.productinventoryservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { JwtAuthFilter.class }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    // Constructor injection for the one real, retrievable Spring bean
    // this test needs.
    private final MockMvc mockMvc;

    // Not Spring-managed at all - a plain object we create ourselves,
    // so it's simply a final field, never something to inject.
    private final ObjectMapper objectMapper = new ObjectMapper();

    // @MockitoBean stays as a field deliberately - it's a bean-override
    // mechanism, not ordinary retrieval, and Spring's own documented
    // usage pattern for it is field-level, not constructor-level.
    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private PaginationProperties paginationProperties;

    ProductControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUp() {
        when(paginationProperties.getDefaultSize()).thenReturn(20);
    }

    @Test
    void getProduct_shouldReturn200_withCorrectJson() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "Test Widget", "TW-1", new BigDecimal("25.50"), 100,
                ProductStatus.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(productService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TW-1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getProduct_shouldReturn404_whenProductMissing() throws Exception {
        when(productService.getById(99L)).thenThrow(new ProductExceptions.NotFound(99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product Not Found"));
    }

    @Test
    void createProduct_shouldReturn201_whenValid() throws Exception {
        CreateProductRequest request = new CreateProductRequest("Test Widget", "TW-1", new BigDecimal("25.50"), 100);
        ProductResponse response = new ProductResponse(
                1L, "Test Widget", "TW-1", new BigDecimal("25.50"), 100,
                ProductStatus.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(productService.create(any(CreateProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createProduct_shouldReturn400_whenNameIsBlank() throws Exception {
        CreateProductRequest invalidRequest = new CreateProductRequest("", "TW-1", new BigDecimal("25.50"), 100);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).create(any());
    }

    @Test
    void getAllProducts_shouldReturn200_withPagedResults() throws Exception {
        ProductResponse product = new ProductResponse(
                1L, "Test Widget", "TW-1", new BigDecimal("25.50"), 100,
                ProductStatus.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now()
        );
        Page<ProductResponse> page = new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1);

        when(productService.getAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("TW-1"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}