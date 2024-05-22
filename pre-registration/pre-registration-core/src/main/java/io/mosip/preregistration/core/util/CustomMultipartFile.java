package io.mosip.preregistration.core.util;

import java.io.*;
import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

public class CustomMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final ByteArrayResource resource;

    public CustomMultipartFile(String name, String originalFilename, String contentType, ByteArrayResource resource) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.resource = resource;
    }

   
    public String getName() {
        return name;
    }

    
    public String getOriginalFilename() {
        return originalFilename;
    }

    
    public String getContentType() {
        return contentType;
    }

   
    public boolean isEmpty() {
        return resource.contentLength() == 0;
    }

   
    public long getSize() {
        return resource.contentLength();
    }

   
    public byte[] getBytes() throws IOException {
        return resource.getInputStream().readAllBytes();
    }

  
    public InputStream getInputStream() throws IOException {
    	
       return resource.getInputStream();

    }


    public void transferTo(File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("Not implemented");
    }
}

