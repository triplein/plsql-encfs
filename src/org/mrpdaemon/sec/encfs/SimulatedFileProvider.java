package org.mrpdaemon.sec.encfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This file provider can be used to implement the EncFS methods without any real file system access.
 * Instead, byte arrays are used to store data. <br>
 * There is no way to represent a hierachical file structure implemented, as this provider is meant to be used for one or just a few files. <br>
 * Only the essential methods for basic operations are implemented yet.
 * @author Samuel Moosmann @ <a href="http://www.triplein.at">TripleIn software solutions GmbH</a>
 *
 */
public class SimulatedFileProvider implements EncFSFileProvider {
	Map<String, byte[]> fileCollection;
	
	public SimulatedFileProvider() {
		this.fileCollection = new HashMap<String, byte[]>();
	}
	
	/**
	 * Adds a single file to the file collection
	 * @param name Name for the file
	 * @param file Data to store
	 */
	public void addFile(String name, byte[]file) {
		fileCollection.put(name, file);
	}
	

	public boolean isDirectory(String srcPath) throws IOException {
		if(srcPath.equalsIgnoreCase("/")){
			return true;
		}
		else return false;
	}

	public boolean exists(String srcPath) throws IOException {
		return this.fileCollection.containsKey(srcPath);
	}

	public String getFilesystemRootPath() {
		return "/";
	}

	public EncFSFileInfo getFileInfo(String srcPath) throws IOException {
		 
		 return new EncFSFileInfo(srcPath, getFilesystemRootPath(), isDirectory(srcPath), 0, 0, true, true, false);
	}

	public List<EncFSFileInfo> listFiles(String dirPath) throws IOException {
		
		List<EncFSFileInfo> fileInfos = new ArrayList<EncFSFileInfo>();
		for(Map.Entry<String, byte[]> it : fileCollection.entrySet()){
			fileInfos.add(new EncFSFileInfo(
					it.getKey().substring(1),
					"/",
					isDirectory(dirPath),
					0,
					it.getValue().length,
					true,
					true,
					false
					));
		}
		return fileInfos;
	}

	public boolean move(String srcPath, String dstPath) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean delete(String srcPath) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean mkdir(String dirPath) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean mkdirs(String dirPath) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public EncFSFileInfo createFile(String dstFilePath) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean copy(String srcFilePath, String dstFilePath)
			throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public InputStream openInputStream(String srcFilePath) throws IOException {
		if(fileCollection.containsKey(srcFilePath)){
			ByteArrayInputStream stream = new ByteArrayInputStream(fileCollection.get(srcFilePath));
			return stream;
		}
		return null;
	}

	public OutputStream openOutputStream(String dstFilePath, long outputLength)
			throws IOException {
		return new ByteArrayOutputStream();
	}
}
