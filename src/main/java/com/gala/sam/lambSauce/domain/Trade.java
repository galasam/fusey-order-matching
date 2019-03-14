package com.gala.sam.lambSauce.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Trade {

    int buyOrder;
    int sellOrder;
    int matchQuantity;
    float matchPrice;

}
