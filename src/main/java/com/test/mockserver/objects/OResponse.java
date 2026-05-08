package com.test.mockserver.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OResponse {
    private Integer statusCode;
    private String body;
    private List<OHeader> headers = new ArrayList<>();
    private ODelay delay;
}
