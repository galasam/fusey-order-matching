package com.gala.sam.lambSauce.domain;

import lombok.Builder;


public class MarketOrder extends ReadyOrder{

    @Builder
    public MarketOrder(int orderId, DIRECTION direction, int quantity,
        TIME_IN_FORCE timeInForce, String ticker) {
        super(orderId, direction, quantity, timeInForce, ticker);
    }
}
