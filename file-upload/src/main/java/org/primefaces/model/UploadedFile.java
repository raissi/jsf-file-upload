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
package org.primefaces.model;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;

public interface UploadedFile {

	public String getFileName();
	
	public InputStream getInputstream() throws IOException;
	
	public long getSize();
	
	public byte[] getContents();
	
	public String getContentType();
	
	public FileItem getFileItem();
}
