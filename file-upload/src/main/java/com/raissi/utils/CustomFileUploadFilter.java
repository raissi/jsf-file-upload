/*
 * Copyright 2009-2013 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.raissi.utils;

import java.io.File;
import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.primefaces.util.Constants;
import org.primefaces.webapp.MultipartRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomFileUploadFilter implements Filter {
	
	static final Logger logger = LoggerFactory.getLogger(CustomFileUploadFilter.class);

	private final static String THRESHOLD_SIZE_PARAM = "thresholdSize";
	
	private final static String UPLOAD_DIRECTORY_PARAM = "uploadDirectory";
	
	private String thresholdSize;
	
	private String uploadDir;
    
    private boolean bypass;
    
	
	public void init(FilterConfig filterConfig) throws ServletException {
        boolean isAtLeastJSF22 = detectJSF22();
        String uploader = filterConfig.getServletContext().getInitParameter(Constants.ContextParams.UPLOADER);
        if(uploader == null || uploader.equals("auto"))
            bypass = isAtLeastJSF22 ? true : false;
        else if(uploader.equals("native"))
            bypass = true;
        else if(uploader.equals("commons"))
            bypass = false;
                        
		thresholdSize = filterConfig.getInitParameter(THRESHOLD_SIZE_PARAM);
		uploadDir = filterConfig.getInitParameter(UPLOAD_DIRECTORY_PARAM);
		logger.debug("FileUploadFilter initiated successfully");
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if(bypass) {
            filterChain.doFilter(request, response);
            return;
        }
        
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		boolean isMultipart = ServletFileUpload.isMultipartContent(httpServletRequest);
		
		if(isMultipart) {
			logger.debug("Parsing file upload request");
			
			FileCleaningTracker fileCleaningTracker = FileCleanerCleanup.getFileCleaningTracker(request.getServletContext());
			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
			diskFileItemFactory.setFileCleaningTracker(fileCleaningTracker);
			if(thresholdSize != null) {
				diskFileItemFactory.setSizeThreshold(Integer.valueOf(thresholdSize));
			}
			if(uploadDir != null) {
				diskFileItemFactory.setRepository(new File(uploadDir));
			}
				
			ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
			MultipartRequest multipartRequest = new MultipartRequest(httpServletRequest, servletFileUpload);
			
			logger.debug("File upload request parsed succesfully, continuing with filter chain with a wrapped multipart request");
			
			filterChain.doFilter(multipartRequest, response);
		} 
        else {
			filterChain.doFilter(request, response);
		}
	}

	public void destroy() {
			logger.debug("Destroying FileUploadFilter");
	}
    
    private boolean detectJSF22() {
        String version = FacesContext.class.getPackage().getImplementationVersion();
        
        if(version != null) {
            return version.startsWith("2.2");
        }
        else {
            //fallback
            try {
                Class.forName("javax.faces.flow.Flow");
                return true;
            } 
            catch (ClassNotFoundException ex) {
                return false;
            }
        }
    }

}
