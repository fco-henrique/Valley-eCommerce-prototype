package com.piper.valley.models.domain;

import com.piper.valley.models.enums.OrderStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OrderStateTest {
    @Test
    public void followsOrderLifecycle() {
        Order order = new Order();
        assertEquals(OrderStatus.UNPROCESSED, order.getOrderStatus());
        assertFalse(order.canBeDelivered());
        assertNull(order.getProcessedDate());

        order.process();
        assertEquals(OrderStatus.PROCESSED, order.getOrderStatus());
        assertNotNull(order.getProcessedDate());
        assertTrue(order.canBeDelivered());

        order.deliver();
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
        assertNotNull(order.getDeliveredDate());
        assertFalse(order.canBeDelivered());
    }

    @Test
    public void rejectsInvalidTransitionsWithoutChangingStatus() {
        Order order = new Order();
        expectInvalidTransition(new Runnable() {
            @Override
            public void run() {
                order.deliver();
            }
        });
        assertEquals(OrderStatus.UNPROCESSED, order.getOrderStatus());
        assertNull(order.getDeliveredDate());

        order.process();
        expectInvalidTransition(new Runnable() {
            @Override
            public void run() {
                order.process();
            }
        });
        assertEquals(OrderStatus.PROCESSED, order.getOrderStatus());

        order.deliver();
        expectInvalidTransition(new Runnable() {
            @Override
            public void run() {
                order.deliver();
            }
        });
        expectInvalidTransition(new Runnable() {
            @Override
            public void run() {
                order.process();
            }
        });
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
    }

    private void expectInvalidTransition(Runnable transition) {
        try {
            transition.run();
            fail("Expected an invalid transition");
        } catch (IllegalStateException expected) {
            // Invalid transitions must leave the order unchanged.
        }
    }
}
