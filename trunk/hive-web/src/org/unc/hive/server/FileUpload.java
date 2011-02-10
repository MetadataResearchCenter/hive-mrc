package org.unc.hive.server;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUpload extends HttpServlet {

	private static final String UPLOAD_DIRECTORY = "/WEB-INF/tmp/";

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ServletContext context = this.getServletContext();
		String path = context.getRealPath("");
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		String fileName = "";

		String tempFileName = ""; 
		
		// Parse the request
		try {
			List<FileItem> items = upload.parseRequest(request);
			for(FileItem item : items) {
				System.out.println("File name: " + item.getName());
				fileName = item.getName();
				
				String extension = "";
				if (fileName.indexOf(".") > 0)
					extension = fileName.substring(fileName.indexOf("."), fileName.length());
				tempFileName = UUID.randomUUID().toString() + extension;
				File uploadedFile = new File(path + UPLOAD_DIRECTORY + tempFileName);
			    item.write(uploadedFile);
			}
			response.setStatus(HttpServletResponse.SC_OK);
			response.getOutputStream().print("success:" + "|" + fileName + "|" + tempFileName + "?");
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}