package com.gala.sam.lambSauce.utils;

import com.gala.sam.lambSauce.domain.ReadyOrder;
import com.gala.sam.lambSauce.domain.ReadyOrder.TIME_IN_FORCE;
import java.util.SortedSet;
import lombok.extern.java.Log;

@Log
public class MarketUtils {

    public static <T extends ReadyOrder> void queueIfTimeInForce(T order,
        SortedSet<T> sameTypeLimitOrders) {
        if(order.getTimeInForce().equals(TIME_IN_FORCE.GTC)) {
            log.finest("Time in force is GTC so add to queue");
            sameTypeLimitOrders.add(order);
        } else if (order.getTimeInForce().equals(TIME_IN_FORCE.FOK)) {
            log.finest("Time in force is FOK so drop");
        } else {
            throw new UnsupportedOperationException("TIME IN FORCE mode not supported");
        }
    }
}
