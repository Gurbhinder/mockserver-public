package com.test.mockserver.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ODelay {
    private String unit;   // MILLISECONDS | SECONDS
    private long value;
}
