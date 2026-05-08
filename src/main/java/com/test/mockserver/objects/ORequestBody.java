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
public class ORequestBody {
    private String type;
    private String matcher;
    private List<OParameters> parameters=new ArrayList<>();
}
