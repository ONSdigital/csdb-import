package com.github.onsdigital.csdbimport.api;

import com.github.davidcarboni.restolino.framework.Api;
import org.apache.commons.fileupload.FileUploadException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

@Api
public class List {
    @GET
    public String home(HttpServletRequest request,
                                   HttpServletResponse response) throws IOException, FileUploadException {
        return "TODO: list available files";
    }
}
