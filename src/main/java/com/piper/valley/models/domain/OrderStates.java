package com.piper.valley.models.domain;

import com.piper.valley.models.enums.OrderStatus;

final class OrderStates {
    private static final OrderState UNPROCESSED = new UnprocessedOrderState();
    private static final OrderState PROCESSED = new ProcessedOrderState();
    private static final OrderState DELIVERED = new DeliveredOrderState();

    private OrderStates() {
    }

    static OrderState forStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalStateException("Order has no status");
        }
        switch (status) {
            case UNPROCESSED:
                return UNPROCESSED;
            case PROCESSED:
                return PROCESSED;
            case DELIVERED:
                return DELIVERED;
            default:
                throw new IllegalStateException("Unknown order status: " + status);
        }
    }
}
