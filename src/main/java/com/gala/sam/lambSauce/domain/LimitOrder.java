package com.gala.sam.lambSauce.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
public class LimitOrder extends ReadyOrder {

    float limit;

    @Builder
    public LimitOrder(int orderId, DIRECTION direction, int quantity,
        TIME_IN_FORCE timeInForce, String ticker, float limit) {
        super(orderId, direction, quantity, timeInForce, ticker);
        this.limit = limit;
    }

    public boolean limitMatches(LimitOrder other) {
        if(getDirection().equals(DIRECTION.BUY)) {
            return getLimit() >= other.getLimit();
        } else if(getDirection().equals(DIRECTION.SELL)) {
            return getLimit() <= other.getLimit();
        } else {
            throw new UnsupportedOperationException("Order direction not supported");
        }
    }

}
