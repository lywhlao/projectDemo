package com.td.demo.distributeId.bean.item;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PaddedLong extends PaddingLong {


    public long value = 0L;

    public static PaddedLong getPaddedLong(long value){
       return PaddedLong.builder().value(value).build();
    }
}
