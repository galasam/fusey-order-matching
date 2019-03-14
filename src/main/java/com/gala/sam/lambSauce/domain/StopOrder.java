package com.gala.sam.lambSauce.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=false)
@Builder
public class StopOrder extends Order {

    float triggerPrice;
    protected ReadyOrder readyOrder;

    public float getTriggerPrice() {
        return triggerPrice;
    }

    public ReadyOrder getReadyOrder() {
        return  readyOrder;
    }

}
