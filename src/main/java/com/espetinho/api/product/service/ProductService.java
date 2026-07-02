package com.espetinho.api.product.service;

import com.espetinho.api.category.dto.CategoryResponse;
import com.espetinho.api.category.entity.Category;
import com.espetinho.api.category.repository.CategoryRepository;
import com.espetinho.api.common.exception.BusinessException;
import com.espetinho.api.product.dto.ProductMenuResponse;
import com.espetinho.api.product.dto.ProductRequest;
import com.espetinho.api.product.dto.ProductResponse;
import com.espetinho.api.product.entity.Product;
import com.espetinho.api.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public ProductMenuResponse getMenu(String search, UUID categoryId, Boolean available) {
        String normalizedSearch = normalizeSearch(search);
        List<Product> menuProducts = normalizedSearch == null
                ? productRepository.findMenuProducts(categoryId, available)
                : productRepository.searchMenuProducts(normalizedSearch, categoryId, available);

        List<ProductResponse> products = menuProducts
                .stream()
                .map(this::toResponse)
                .toList();

        List<CategoryResponse> categories = categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(this::toCategoryResponse)
                .toList();

        return new ProductMenuResponse(categories, products);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        Product product = productRepository.findPublicById(id)
                .orElseThrow(() -> new BusinessException("Produto nao encontrado", HttpStatus.NOT_FOUND));

        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setActive(true);
        applyProductData(product, request);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = findDetailedProduct(id);
        applyProductData(product, request);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = findDetailedProduct(id);
        product.setAvailable(false);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponse activate(UUID id) {
        Product product = findDetailedProduct(id);
        product.setActive(true);
        product.setAvailable(true);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void softDelete(UUID id) {
        Product product = findDetailedProduct(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private void applyProductData(Product product, ProductRequest request) {
        Category category = categoryRepository.findByIdAndActiveTrue(request.categoryId())
                .orElseThrow(() -> new BusinessException("Categoria nao encontrada", HttpStatus.NOT_FOUND));

        product.setName(request.name().trim());
        product.setDescription(request.description().trim());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setImageUrls(normalizeImageUrls(request.imageUrls()));
        product.setAvailable(request.available() == null || request.available());
        product.setStockQuantity(request.stockQuantity());
    }

    private Product findDetailedProduct(UUID id) {
        return productRepository.findDetailedById(id)
                .orElseThrow(() -> new BusinessException("Produto nao encontrado", HttpStatus.NOT_FOUND));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                toCategoryResponse(product.getCategory()),
                List.copyOf(product.getImageUrls()),
                product.isAvailable(),
                product.getStockQuantity()
        );
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDisplayOrder()
        );
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }

        return imageUrls.stream()
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .map(String::trim)
                .toList();
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }
}
