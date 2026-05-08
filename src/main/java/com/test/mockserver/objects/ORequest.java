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
public class ORequest {
    private String method;
    private String path;
    private List<OHeader> headers = new ArrayList<>();
    private List<OParameters> queryParameters=new ArrayList<>();
    private List<OParameters> pathParameters=new ArrayList<>();
    private ORequestBody requestBody = new ORequestBody();
}
