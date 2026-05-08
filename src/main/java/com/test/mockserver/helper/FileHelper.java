package com.test.mockserver.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    private static final Logger log = LoggerFactory.getLogger(FileHelper.class);

    public static List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();
        ClassLoader cl = FileHelper.class.getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources = resolver.getResources("classpath*:" + path + "*.yaml");
        for (Resource resource : resources) {
            log.debug("Discovered mock file: {}", resource.getFilename());
            filenames.add(resource.getFilename());
        }
        return filenames;
    }
}
