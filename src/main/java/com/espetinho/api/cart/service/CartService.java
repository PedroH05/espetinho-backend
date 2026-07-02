package com.espetinho.api.cart.service;

import com.espetinho.api.cart.dto.AddCartItemRequest;
import com.espetinho.api.cart.dto.CartItemResponse;
import com.espetinho.api.cart.dto.CartResponse;
import com.espetinho.api.cart.dto.UpdateCartItemRequest;
import com.espetinho.api.cart.entity.Cart;
import com.espetinho.api.cart.entity.CartItem;
import com.espetinho.api.cart.repository.CartRepository;
import com.espetinho.api.common.exception.BusinessException;
import com.espetinho.api.product.entity.Product;
import com.espetinho.api.product.repository.ProductRepository;
import com.espetinho.api.security.CustomUserPrincipal;
import com.espetinho.api.user.entity.User;
import com.espetinho.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String GUEST_ID_REQUIRED_MESSAGE = "Informe o header X-Guest-Id para usar o carrinho sem login";

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse getCart(String guestIdHeader, CustomUserPrincipal principal) {
        Cart cart = getOrCreateActiveCart(resolveOwner(guestIdHeader, principal));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String guestIdHeader, CustomUserPrincipal principal, AddCartItemRequest request) {
        Cart cart = getOrCreateActiveCart(resolveOwner(guestIdHeader, principal));
        Product product = findAvailableProduct(request.productId());

        CartItem item = findItemByProductId(cart, product.getId());
        int newQuantity = item == null ? request.quantity() : item.getQuantity() + request.quantity();
        validateStock(product, newQuantity);

        if (item == null) {
            item = CartItem.builder()
                    .product(product)
                    .quantity(request.quantity())
                    .unitPrice(product.getPrice())
                    .build();
            cart.addItem(item);
        } else {
            item.setQuantity(newQuantity);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(
            String guestIdHeader,
            CustomUserPrincipal principal,
            UUID itemId,
            UpdateCartItemRequest request
    ) {
        Cart cart = getOrCreateActiveCart(resolveOwner(guestIdHeader, principal));
        CartItem item = findItemById(cart, itemId);

        validateStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(String guestIdHeader, CustomUserPrincipal principal, UUID itemId) {
        Cart cart = getOrCreateActiveCart(resolveOwner(guestIdHeader, principal));
        CartItem item = findItemById(cart, itemId);

        cart.removeItem(item);

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse clearCart(String guestIdHeader, CustomUserPrincipal principal) {
        Cart cart = getOrCreateActiveCart(resolveOwner(guestIdHeader, principal));
        cart.getItems().clear();

        return toResponse(cartRepository.save(cart));
    }

    private Cart getOrCreateActiveCart(CartOwner owner) {
        if (owner.userId() != null) {
            return cartRepository.findActiveByUserId(owner.userId())
                    .orElseGet(() -> createUserCart(owner.userId()));
        }

        return cartRepository.findActiveByGuestId(owner.guestId())
                .orElseGet(() -> createGuestCart(owner.guestId()));
    }

    private Cart createUserCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado", HttpStatus.NOT_FOUND));

        Cart cart = Cart.builder()
                .user(user)
                .active(true)
                .build();

        return cartRepository.save(cart);
    }

    private Cart createGuestCart(UUID guestId) {
        Cart cart = Cart.builder()
                .guestId(guestId)
                .active(true)
                .build();

        return cartRepository.save(cart);
    }

    private CartOwner resolveOwner(String guestIdHeader, CustomUserPrincipal principal) {
        if (principal != null) {
            return new CartOwner(principal.getId(), null);
        }

        if (guestIdHeader == null || guestIdHeader.isBlank()) {
            throw new BusinessException(GUEST_ID_REQUIRED_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        try {
            return new CartOwner(null, UUID.fromString(guestIdHeader.trim()));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Header X-Guest-Id invalido", HttpStatus.BAD_REQUEST);
        }
    }

    private Product findAvailableProduct(UUID productId) {
        return productRepository.findAvailableForCartById(productId)
                .orElseThrow(() -> new BusinessException("Produto indisponivel ou nao encontrado", HttpStatus.NOT_FOUND));
    }

    private CartItem findItemByProductId(Cart cart, UUID productId) {
        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private CartItem findItemById(Cart cart, UUID itemId) {
        return cart.getItems()
                .stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Item do carrinho nao encontrado", HttpStatus.NOT_FOUND));
    }

    private void validateStock(Product product, int quantity) {
        Integer stockQuantity = product.getStockQuantity();

        if (stockQuantity != null && quantity > stockQuantity) {
            throw new BusinessException("Quantidade solicitada maior que o estoque disponivel", HttpStatus.BAD_REQUEST);
        }
    }

    private CartResponse toResponse(Cart cart) {
        Set<CartItemResponse> itemResponses = new LinkedHashSet<>();
        int totalItems = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalItems += item.getQuantity();
            totalAmount = totalAmount.add(subtotal);
            itemResponses.add(toItemResponse(item, subtotal));
        }

        return new CartResponse(
                cart.getId(),
                cart.getUser() == null ? null : cart.getUser().getId(),
                cart.getGuestId(),
                itemResponses.stream().toList(),
                totalItems,
                totalAmount
        );
    }

    private CartItemResponse toItemResponse(CartItem item, BigDecimal subtotal) {
        Product product = item.getProduct();

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getImageUrls().isEmpty() ? null : product.getImageUrls().get(0),
                item.getUnitPrice(),
                item.getQuantity(),
                subtotal,
                product.isAvailable()
        );
    }

    private record CartOwner(UUID userId, UUID guestId) {
    }
}
