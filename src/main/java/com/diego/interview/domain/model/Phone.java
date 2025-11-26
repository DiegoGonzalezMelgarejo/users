package com.diego.interview.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Phone {
    private Long id;
    private String number;
    private String cityCode;
    private String countryCode;
}
