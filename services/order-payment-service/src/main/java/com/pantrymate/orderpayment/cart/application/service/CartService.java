package com.pantrymate.orderpayment.cart.application.service;

import com.pantrymate.common.exception.BusinessException;
import com.pantrymate.orderpayment.cart.application.dto.CartItemAddRequest;
import com.pantrymate.orderpayment.cart.application.dto.CartItemAddResponse;
import com.pantrymate.orderpayment.cart.application.dto.CartItemResponse;
import com.pantrymate.orderpayment.cart.application.dto.CartItemUpdateRequest;
import com.pantrymate.orderpayment.cart.application.dto.CartResponse;
import com.pantrymate.orderpayment.cart.application.dto.ProductInfoResponse;
import com.pantrymate.orderpayment.cart.domain.CartItems;
import com.pantrymate.orderpayment.cart.domain.Carts;
import com.pantrymate.orderpayment.cart.domain.exception.CartErrorCode;
import com.pantrymate.orderpayment.cart.domain.repository.CartItemRepository;
import com.pantrymate.orderpayment.cart.domain.repository.CartRepository;
import com.pantrymate.orderpayment.cart.infrastructure.client.ProductServiceClient;
import feign.FeignException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public CartItems addItem(Long userId, CartItemAddRequest request) {
        Carts cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> cartRepository.save(Carts.createFor(userId)));
        ProductInfoResponse product;
        try {
            product = productServiceClient.getProductInfo(request.productId())
                .data();
        } catch (FeignException.NotFound e) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_FOUND);
        } catch (FeignException e) {
            throw new BusinessException(CartErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
        }
        if(!"ON_SALE".equals(product.status())){
            throw new BusinessException(CartErrorCode.PRODUCT_UNAVAILABLE);
        }
        Optional<CartItems> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(),
            request.productId());
        if (existingItem.isPresent()) {
            CartItems item = existingItem.get();
            int newTotalQuantity = item.getQuantity() + request.quantity();
            if (newTotalQuantity > product.stockQuantity()) {
                throw new BusinessException(CartErrorCode.STOCK_EXCEEDED);
            }
            item.addQuantity(request.quantity());
            return item;
        } else {
            if (request.quantity() > product.stockQuantity()) {
                throw new BusinessException(CartErrorCode.STOCK_EXCEEDED);
            }
            CartItems newItem = CartItems.create(cart.getId(), request.productId(),
                request.quantity());
            return cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public CartResponse getCart(Long userId) {
        Carts cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> cartRepository.save(Carts.createFor(userId)));
        List<CartItems> cartItems = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = cartItems.stream()

            .map(cartItem ->{
                ProductInfoResponse product;
                try {
                    product = productServiceClient.getProductInfo(
                        cartItem.getProductId()).data();
                }catch (FeignException.NotFound e) {
                    return null; // 우선 null을 반환하고 추후에 품절상품입니다를 반환하는 메서드나 상품판매가 종료되었다는 메서드 추가
                }
                    return CartItemResponse.of(cartItem, product);
            })
            .toList();
        return CartResponse.of(cart.getId(), itemResponses);
    }

    @Transactional
    public CartItems updateCart(Long userId, Long cartItemId, CartItemUpdateRequest request) {
        CartItems cartItems = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
        checkOwnership(userId, cartItems);
        ProductInfoResponse product = productServiceClient.getProductInfo(cartItems.getProductId()).data();
        if(request.quantity() > product.stockQuantity()){
            throw new BusinessException(CartErrorCode.STOCK_EXCEEDED);
        }
        cartItems.changeQuantity(request.quantity());
        return cartItems;
    }

    @Transactional
    public void deleteCart(Long userId, Long cartItemId) {
        CartItems cartItems = cartItemRepository.findById(cartItemId)
            .orElseThrow(()-> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
        checkOwnership(userId, cartItems);

        cartItemRepository.delete(cartItems);
    }


    private void checkOwnership(Long userId, CartItems item) {
        Carts cart = cartRepository.findByUserId(userId)
            .orElseThrow(()-> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
        if(!item.getCartId().equals(cart.getId())){
            throw new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND);
        }

    }
}
