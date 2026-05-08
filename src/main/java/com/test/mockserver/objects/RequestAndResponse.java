package com.test.mockserver.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestAndResponse {
    private ORequest request;
    private OResponse response;
    private Integer times;
}
