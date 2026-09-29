package com.pantrymate.product.wishlist.application.dto;

import java.util.List;

public record WishedProductsResponse(
    List<Long> productIds
) {

}
