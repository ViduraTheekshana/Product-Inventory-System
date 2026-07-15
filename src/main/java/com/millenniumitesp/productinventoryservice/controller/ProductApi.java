package com.millenniumitesp.productinventoryservice.controller;

import com.millenniumitesp.productinventoryservice.dto.CreateProductRequest;
import com.millenniumitesp.productinventoryservice.dto.ProductResponse;
import com.millenniumitesp.productinventoryservice.dto.UpdateStockPriceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Products", description = "Endpoints for managing product inventory")
public interface ProductApi {

    @Operation(summary = "Get a product by id",
            description = "Fetches a single product's details using its unique identifier.")
    @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "id was not a positive number",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No product exists with the given id",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "The product's unique id", example = "1")
            @PathVariable @Positive Long id
    );

    @Operation(summary = "List products (paginated)",
            description = "Supports ?page=, ?size= (capped server-side), and ?sort= query params.")
    @ApiResponse(responseCode = "200", description = "A page of products")
    ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    );

    @Operation(summary = "Create a new product",
            description = "Adds a new product. sku must be unique across all active products.")
    @ApiResponse(responseCode = "201", description = "Product created successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed, or stockQuantity outside configured limits",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "A product with this sku already exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request);

    @Operation(summary = "Partially update price and/or stock",
            description = "Updates ONLY price and/or stockQuantity. No other fields can change here.")
    @ApiResponse(responseCode = "200", description = "Product updated successfully",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed, or stockQuantity outside configured limits",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No product exists with the given id",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Product was concurrently modified by another request",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<ProductResponse> updatePriceAndStock(
            @Parameter(description = "The product's unique id", example = "1")
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateStockPriceRequest request
    );

    @Operation(summary = "Delete a product",
            description = "Archives then removes the product. Not a permanent, destructive delete.")
    @ApiResponse(responseCode = "204", description = "Product deleted (archived) successfully")
    @ApiResponse(responseCode = "400", description = "id was not a positive number",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No product exists with the given id",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<Void> deleteProduct(
            @Parameter(description = "The product's unique id", example = "1")
            @PathVariable @Positive Long id
    );
}