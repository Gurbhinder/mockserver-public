package com.test.mockserver.helper;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    public static List<String> getResourceFiles(String path) throws Exception,IOException {
        List<String> filenames = new ArrayList<>();
        ClassLoader cl = FileHelper.class.getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources = resolver.getResources("classpath*:"+path+"*.yaml") ;
        for (Resource resource: resources){
            System.out.println("PATH MATCHER: "+resource.getFilename());
            filenames.add(resource.getFilename());
        }
        return filenames;
    }
}
