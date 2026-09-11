package com.pantrymate.orderpayment.order.application.service;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.orderpayment.cart.application.dto.ProductInfoResponse;
import com.pantrymate.orderpayment.cart.domain.CartItems;
import com.pantrymate.orderpayment.cart.domain.repository.CartItemRepository;
import com.pantrymate.orderpayment.cart.infrastructure.client.ProductServiceClient;
import com.pantrymate.orderpayment.order.application.dto.OrderCreateRequest;
import com.pantrymate.orderpayment.order.domain.OrderItems;
import com.pantrymate.orderpayment.order.domain.Orders;
import com.pantrymate.orderpayment.order.domain.exception.OrderErrorCode;
import com.pantrymate.orderpayment.order.domain.repository.OrderItemRepository;
import com.pantrymate.orderpayment.order.domain.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Orders createOrder(Long userId, OrderCreateRequest request, String idempotencyKey) {
        Optional<Orders> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
            if(existingOrder.isPresent()) {
                Orders order = existingOrder.get();

                if(!order.getUserId().equals(userId)) {
                    throw new BusinessException(OrderErrorCode.INVALID_IDEMPOTENCY_KEY);
                }
                return order;
            }

        List<CartItems> selectedItems = cartItemRepository.findAllById(
            request.selectedCartItemIds());
        List<validateItem> validateItems = selectedItems.stream()
            // 상품 건수마다 현재 getProductInfo를 하면서 N+1 문제가 발생하고있음. 리펙토링 필요함..
            .map(cartItem -> {
                ProductInfoResponse product = productServiceClient.getProductInfo(
                    cartItem.getProductId()).data();
                validateProductAvailable(product, cartItem.getQuantity());
                return new validateItem(cartItem, product);
            }).toList();
        long totalAmount = validateItems.stream()
            .mapToLong(v -> v.product().price() * v.items().getQuantity())
            .sum();
        String orderName = createOrderName(validateItems);
        Orders order = orderRepository.save(Orders.create(userId, orderName, totalAmount, idempotencyKey));

        List<OrderItems> orderItems = validateItems.stream()
            .map(v -> OrderItems.create(
                order.getId(),
                v.items().getProductId(),
                v.product().name(),
                v.product().price(),
                v.items().getQuantity()
            ))
            .toList();
        orderItemRepository.saveAll(orderItems);
        return order;
    }

    private record validateItem(CartItems items, ProductInfoResponse product) {

    }

    private void validateProductAvailable(ProductInfoResponse product, Integer requestedQuantity) {
        if (!"ON_SALE".equals(product.status())) {
            throw new BusinessException(OrderErrorCode.PRODUCT_OUT_OF_STOCK);
        }
        if (requestedQuantity > product.stockQuantity()) {
            throw new BusinessException(OrderErrorCode.PRODUCT_OUT_OF_STOCK);   // 같은 에러코드 재사용 가능
        }
    }

    private String createOrderName(List<validateItem> item) {
        String firstProductName = item.getFirst().product().name();

        if (item.size() == 1) {
            return firstProductName;
        } else {
            return firstProductName + "외" + (item.size() - 1) + "건";
        }
    }
}
