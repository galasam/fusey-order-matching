package com.gala.sam.lambSauce.domain;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;

@NonFinal
@Value
@EqualsAndHashCode(callSuper=false)
public class ReadyOrder extends Order {
    public enum DIRECTION {SELL, BUY}
    public enum TIME_IN_FORCE {FOK, GTC}

    int orderId;
    DIRECTION direction;
    int quantity;
    TIME_IN_FORCE timeInForce;
    String ticker;

}
