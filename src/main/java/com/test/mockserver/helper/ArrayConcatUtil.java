package com.test.mockserver.helper;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Stream;

public interface ArrayConcatUtil {

    @SuppressWarnings("unchecked")
    static  <T> T[] concatWithStream(T[] array1, T[] array2) {
        return Stream.concat(Arrays.stream(array1), Arrays.stream(array2))
                .toArray(size -> (T[]) Array.newInstance(array1.getClass().getComponentType(), size));
    }
}
