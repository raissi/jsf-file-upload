package com.raissi.managedbeans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FilenameUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raissi.utils.CustomFileUtils;

@ManagedBean
@RequestScoped
public class FileManagedBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileManagedBean.class);
	private static final String uploadFolderPath = "E:/java/Docs/tmp/file-upload/data";
	private static final int BUFFER_SIZE = 6124;
	
	public void handleFileUpload(FileUploadEvent event) {
		long start = System.currentTimeMillis();
		File result = new File(uploadFolderPath + File.separator
				+ event.getFile().getFileName());

		copyFileByteArray(event, result);
		event.getFile().getFileItem().delete();
		long end  = System.currentTimeMillis();
		LOGGER.info("Time in classic method: {} ms",(end-start));
	}
	
	public void utf8HandleFileUpload(FileUploadEvent event) {
		long start = System.currentTimeMillis();
		String fileName = FilenameUtils.getName(event.getFile().getFileName());
		try {
			fileName = new String(fileName.getBytes(Charset.defaultCharset()), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			LOGGER.error("Error in charset:",e1);
		}
		
		fastFileCopy(event.getFile(), uploadFolderPath + File.separator + fileName);
		event.getFile().getFileItem().delete();
		long end  = System.currentTimeMillis();
		LOGGER.info("Time in utf8 method: {} ms",(end-start));
	}
	
	private void fastFileCopy(UploadedFile file, String filePath){
		try {
			final InputStream input = file.getInputstream();
			final OutputStream output = new FileOutputStream(filePath);
			CustomFileUtils.writeStream(input, output);
			FacesMessage msg = new FacesMessage("File Description",
					"file name: " + FilenameUtils.getName(filePath)
							+ "file size: " + file.getSize() / 1024
							+ " Kb content type: "
							+ file.getContentType()
							+ "The file was uploaded.");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (IOException e) {
			LOGGER.error("Error upload,", e);
			FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"The files were not uploaded!", "");
			FacesContext.getCurrentInstance().addMessage(null, error);
		}
	}
	

	private void copyFileByteArray(FileUploadEvent event, File result) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(result);

			byte[] buffer = new byte[BUFFER_SIZE];

			int bulk;
			InputStream inputStream = event.getFile().getInputstream();
			while (true) {
				bulk = inputStream.read(buffer);
				if (bulk < 0) {
					break;
				}
				fileOutputStream.write(buffer, 0, bulk);
				fileOutputStream.flush();
			}

			fileOutputStream.close();
			inputStream.close();

			FacesMessage msg = new FacesMessage("File Description",
					"file name: " + result.getName()
							+ "file size: " + event.getFile().getSize() / 1024
							+ " Kb content type: "
							+ event.getFile().getContentType()
							+ "The file was uploaded.");
			FacesContext.getCurrentInstance().addMessage(null, msg);

		} catch (IOException e) {
			LOGGER.error("Error upload,", e);

			FacesMessage error = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"The files were not uploaded!", "");
			FacesContext.getCurrentInstance().addMessage(null, error);
		}
	}

}
