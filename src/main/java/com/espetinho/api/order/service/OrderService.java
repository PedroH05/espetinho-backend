package com.espetinho.api.order.service;

import com.espetinho.api.cart.entity.Cart;
import com.espetinho.api.cart.entity.CartItem;
import com.espetinho.api.cart.repository.CartRepository;
import com.espetinho.api.common.exception.BusinessException;
import com.espetinho.api.order.dto.CreateOrderRequest;
import com.espetinho.api.order.dto.CustomerInfoRequest;
import com.espetinho.api.order.dto.CustomerInfoResponse;
import com.espetinho.api.order.dto.DeliveryAddressRequest;
import com.espetinho.api.order.dto.DeliveryAddressResponse;
import com.espetinho.api.order.dto.OrderItemResponse;
import com.espetinho.api.order.dto.OrderResponse;
import com.espetinho.api.order.entity.CustomerOrder;
import com.espetinho.api.order.entity.OrderItem;
import com.espetinho.api.order.enums.OrderStatus;
import com.espetinho.api.order.enums.OrderType;
import com.espetinho.api.order.repository.CustomerOrderRepository;
import com.espetinho.api.product.entity.Product;
import com.espetinho.api.security.CustomUserPrincipal;
import com.espetinho.api.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String GUEST_ID_REQUIRED_MESSAGE = "Informe o header X-Guest-Id para pedidos sem login";

    private final CartRepository cartRepository;
    private final CustomerOrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(String guestIdHeader, CustomUserPrincipal principal, CreateOrderRequest request) {
        CartOwner owner = resolveOwner(guestIdHeader, principal);
        Cart cart = findActiveCart(owner);

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Carrinho vazio", HttpStatus.BAD_REQUEST);
        }

        validateOrderRequest(request, owner);

        CustomerOrder order = CustomerOrder.builder()
                .user(cart.getUser())
                .guestId(cart.getGuestId())
                .type(request.type())
                .status(OrderStatus.AWAITING_PAYMENT)
                .notes(trimToNull(request.notes()))
                .totalAmount(BigDecimal.ZERO)
                .build();

        applyCustomerData(order, request.customer(), owner);
        applyDeliveryData(order, request.type() == OrderType.DELIVERY ? request.deliveryAddress() : null);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            validateProductForOrder(product, cartItem.getQuantity());

            BigDecimal subtotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            order.addItem(OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(cartItem.getUnitPrice())
                    .quantity(cartItem.getQuantity())
                    .subtotal(subtotal)
                    .build());

            reduceStock(product, cartItem.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        CustomerOrder savedOrder = orderRepository.save(order);

        cart.setActive(false);
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id, String guestIdHeader, CustomUserPrincipal principal) {
        CustomerOrder order = orderRepository.findDetailedById(id)
                .orElseThrow(() -> new BusinessException("Pedido nao encontrado", HttpStatus.NOT_FOUND));

        validateCanViewOrder(order, guestIdHeader, principal);

        return toResponse(order);
    }

    private Cart findActiveCart(CartOwner owner) {
        if (owner.userId() != null) {
            return cartRepository.findActiveByUserId(owner.userId())
                    .orElseThrow(() -> new BusinessException("Carrinho nao encontrado", HttpStatus.NOT_FOUND));
        }

        return cartRepository.findActiveByGuestId(owner.guestId())
                .orElseThrow(() -> new BusinessException("Carrinho nao encontrado", HttpStatus.NOT_FOUND));
    }

    private void validateOrderRequest(CreateOrderRequest request, CartOwner owner) {
        if (request.type() == OrderType.DINE_IN) {
            throw new BusinessException("Consumo no local ainda nao implementado", HttpStatus.BAD_REQUEST);
        }

        if (owner.guestId() != null && request.customer() == null) {
            throw new BusinessException("Dados do cliente sao obrigatorios para pedido sem login", HttpStatus.BAD_REQUEST);
        }

        if (request.type() == OrderType.DELIVERY && request.deliveryAddress() == null) {
            throw new BusinessException("Endereco de entrega e obrigatorio para delivery", HttpStatus.BAD_REQUEST);
        }
    }

    private void applyCustomerData(CustomerOrder order, CustomerInfoRequest customer, CartOwner owner) {
        if (owner.userId() == null && customer != null) {
            order.setGuestName(customer.name().trim());
            order.setGuestPhone(customer.phone().trim());
            order.setGuestEmail(trimToNull(customer.email()));
        }
    }

    private void applyDeliveryData(CustomerOrder order, DeliveryAddressRequest deliveryAddress) {
        if (deliveryAddress == null) {
            return;
        }

        order.setDeliveryStreet(deliveryAddress.street().trim());
        order.setDeliveryNumber(deliveryAddress.number().trim());
        order.setDeliveryComplement(trimToNull(deliveryAddress.complement()));
        order.setDeliveryNeighborhood(deliveryAddress.neighborhood().trim());
    }

    private void validateProductForOrder(Product product, int quantity) {
        if (!product.isActive() || !product.isAvailable()) {
            throw new BusinessException("Produto indisponivel no carrinho: " + product.getName(), HttpStatus.BAD_REQUEST);
        }

        Integer stockQuantity = product.getStockQuantity();

        if (stockQuantity != null && quantity > stockQuantity) {
            throw new BusinessException("Estoque insuficiente para o produto: " + product.getName(), HttpStatus.BAD_REQUEST);
        }
    }

    private void reduceStock(Product product, int quantity) {
        Integer stockQuantity = product.getStockQuantity();

        if (stockQuantity != null) {
            product.setStockQuantity(stockQuantity - quantity);
        }
    }

    private void validateCanViewOrder(CustomerOrder order, String guestIdHeader, CustomUserPrincipal principal) {
        if (principal != null) {
            boolean ownOrder = order.getUser() != null && principal.getId().equals(order.getUser().getId());
            boolean staffOrAdmin = principal.getUser().getRoles().contains(UserRole.ADMIN)
                    || principal.getUser().getRoles().contains(UserRole.STAFF);

            if (ownOrder || staffOrAdmin) {
                return;
            }
        }

        if (order.getGuestId() != null && order.getGuestId().equals(parseGuestIdOrNull(guestIdHeader))) {
            return;
        }

        throw new BusinessException("Acesso negado ao pedido", HttpStatus.FORBIDDEN);
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

    private UUID parseGuestIdOrNull(String guestIdHeader) {
        if (guestIdHeader == null || guestIdHeader.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(guestIdHeader.trim());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Header X-Guest-Id invalido", HttpStatus.BAD_REQUEST);
        }
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getUser() == null ? null : order.getUser().getId(),
                order.getGuestId(),
                toCustomerResponse(order),
                order.getType(),
                order.getStatus(),
                toDeliveryAddressResponse(order),
                toItemResponses(order),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }

    private CustomerInfoResponse toCustomerResponse(CustomerOrder order) {
        if (order.getUser() != null) {
            return new CustomerInfoResponse(
                    order.getUser().getName(),
                    null,
                    order.getUser().getEmail()
            );
        }

        return new CustomerInfoResponse(
                order.getGuestName(),
                order.getGuestPhone(),
                order.getGuestEmail()
        );
    }

    private DeliveryAddressResponse toDeliveryAddressResponse(CustomerOrder order) {
        if (order.getDeliveryStreet() == null) {
            return null;
        }

        return new DeliveryAddressResponse(
                order.getDeliveryStreet(),
                order.getDeliveryNumber(),
                order.getDeliveryComplement(),
                order.getDeliveryNeighborhood()
        );
    }

    private List<OrderItemResponse> toItemResponses(CustomerOrder order) {
        return order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private record CartOwner(UUID userId, UUID guestId) {
    }
}
