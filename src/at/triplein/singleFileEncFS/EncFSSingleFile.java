package at.triplein.singleFileEncFS;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.driver.OracleDriver;
import org.mrpdaemon.sec.encfs.DirectConfigBuilder;
import org.mrpdaemon.sec.encfs.EncFSConfig;
import org.mrpdaemon.sec.encfs.EncFSCorruptDataException;
import org.mrpdaemon.sec.encfs.EncFSCrypto;
import org.mrpdaemon.sec.encfs.EncFSFile;
import org.mrpdaemon.sec.encfs.EncFSFilenameEncryptionAlgorithm;
import org.mrpdaemon.sec.encfs.EncFSInputStream;
import org.mrpdaemon.sec.encfs.EncFSInvalidConfigException;
import org.mrpdaemon.sec.encfs.EncFSInvalidPasswordException;
import org.mrpdaemon.sec.encfs.EncFSOutputStream;
import org.mrpdaemon.sec.encfs.EncFSUnsupportedException;
import org.mrpdaemon.sec.encfs.EncFSVolume;
import org.mrpdaemon.sec.encfs.EncFSVolumeBuilder;
import org.mrpdaemon.sec.encfs.OpenPBKDF2Provider;
import org.mrpdaemon.sec.encfs.SimulatedFileProvider;

/**
 * Methods to use EncFS encryption on single files, without direct file system access.
 * @author Samuel Moosmann @ <a href="http://www.triplein.at">TripleIn software solutions GmbH</a>
 *
 */
public final class EncFSSingleFile {
	
	/** 
	 * Decrypts data using a EncFS configuration file and the password
	 * 
	 * @param encryptedData Data to decrypt
	 * @param config EncFS configuration file 
	 * @param password Password used when creating the EncFS volume
	 * @return Decrypted data as byte array
	 * 
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * @throws EncFSCorruptDataException
	 * @throws IOException
	 * 
	 * @author Samuel Moosmann @ TripleIn software solutions GmbH
	 */
	public final static byte[] decrypt(byte[] encryptedData, byte[] config, String password) 
			throws EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, EncFSCorruptDataException, IOException{
		
		EncFSVolume volume = new EncFSVolume();
		SimulatedFileProvider fileProvider = new SimulatedFileProvider();
		
		fileProvider.addFile("/.encfs6.xml", config);
		volume = new EncFSVolumeBuilder().withFileProvider(fileProvider).withPbkdf2Provider(new OpenPBKDF2Provider()).withPassword(password).buildVolume();
		
		fileProvider.addFile("/encrypted.enc", encryptedData);
		
		EncFSInputStream inputStream = new EncFSInputStream(volume, fileProvider.openInputStream("/encrypted.enc"), "/encrypted.enc");
		
		byte[] data = new byte[16384];
		int read;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		
		while((read = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, read);
		}
		
		inputStream.close();
		return buffer.toByteArray();
	}
	
	/**
	 * Decrypts data using a EncFS configuration file and the password, usable in Oracle PL/SQL
	 * 
	 * @param encryptedData Data to decrypt
	 * @param config EncFS configuration file as {@link oracle.sql.BLOB}
	 * @param password Password used when generating the config file
	 * @return Decrypted data as {@link oracle.sql.BLOB}
	 * 
	 * @throws SQLException
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * @throws EncFSCorruptDataException
	 * @throws IOException
	 * 
	 */
	public final static oracle.sql.BLOB decrypt(oracle.sql.BLOB encryptedData, oracle.sql.BLOB config, String password) 
			throws SQLException, EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, EncFSCorruptDataException, IOException{
		byte[] convertedData = encryptedData.getBytes((long) 1, (int) encryptedData.length());
		byte[] convertedConfig = config.getBytes(1, (int) config.length());
		byte[] result = decrypt(convertedData, convertedConfig, password);
		
		oracle.sql.BLOB decryptedBlob = oracle.sql.BLOB.createTemporary(encryptedData.getOracleConnection(), false, oracle.sql.BLOB.DURATION_SESSION);
		OutputStream os = decryptedBlob.setBinaryStream(0);
		
		os.write(result);
		os.flush();
		os.close();
		return decryptedBlob;
	}
	
	/**
	 * Encrypts data using a EncFS configuration file.
	 * 
	 * @param unencryptedData Data to encrypt
	 * @param config EncFS configuration file
	 * @param password Password used to create the EncFS volume
	 * @return Encrypted data as byte array
	 * 
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * @throws EncFSCorruptDataException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * 
	 */
	public final static byte[] encrypt(byte[] unencryptedData, byte[] config, String password) 
			throws EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, EncFSCorruptDataException, IOException, NoSuchAlgorithmException, IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException{
		
		EncFSVolume volume = new EncFSVolume();
		SimulatedFileProvider fileProvider = new SimulatedFileProvider();

		// Hinzufügen der Config und Initalisierung des Volumes mit virtuellem Datei-Provider
		fileProvider.addFile("/.encfs6.xml", config);
		volume = new EncFSVolumeBuilder().withFileProvider(fileProvider).withPbkdf2Provider(new OpenPBKDF2Provider()).withPassword(password).buildVolume();
		
		ByteArrayInputStream is = new ByteArrayInputStream(unencryptedData);
		ByteArrayOutputStream out = (ByteArrayOutputStream) fileProvider.openOutputStream("", 0);
		OutputStream os = new EncFSOutputStream(volume, out, "/encrypted.enc");
		
		byte[] buffer = new byte[2048];
		int bytesRead = 0;
		while((bytesRead = is.read(buffer)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
		
		os.close();
		return out.toByteArray();
	}
	
	/**
	 * Encrypts data using a EncFS configuration file. Usable in Oracle PL/SQL
	 * @param unencryptedData Data do encrypt
	 * @param config EncFS configuration file as {@link oracle.sql.BLOB}
	 * @param password Password used when creating the EncFS volume
	 * @return Encrypted data as {@link oracle.sql.BLOB}
	 * 
	 * @throws SQLException
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * @throws EncFSCorruptDataException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * 
	 */
	public final static oracle.sql.BLOB encrypt(oracle.sql.BLOB unencryptedData, oracle.sql.BLOB config, String password)
			throws SQLException, EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, EncFSCorruptDataException, IOException, NoSuchAlgorithmException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		byte[] convertedData = unencryptedData.getBytes(1, (int) unencryptedData.length());
		byte[] convertedConfig = config.getBytes(1, (int) config.length());
		byte[] result = encrypt(convertedData, convertedConfig, password);
		oracle.sql.BLOB encryptedBlob = oracle.sql.BLOB.createTemporary(unencryptedData.getOracleConnection(), false, oracle.sql.BLOB.DURATION_SESSION);
		OutputStream os = encryptedBlob.setBinaryStream(0);
		
		os.write(result);
		os.flush();
		os.close();
		
		return encryptedBlob;	
		}
	
	
	/**
	 * Decrypts an array of filenames
	 * @param encryptedFilenames Encrypted filenames
	 * @param config EncFS configuration file
	 * @param password Password used when creating the EncFS volume
	 * @return Array with the decrypted filenames
	 * 
	 * @throws EncFSCorruptDataException
	 * @throws IOException
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * 
	 */
	public final static String[] decryptFilenames(String[] encryptedFilenames, byte[] config, String password)
			throws EncFSCorruptDataException, IOException, EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException{
		EncFSVolume volume = new EncFSVolume();
		SimulatedFileProvider fileProvider = new SimulatedFileProvider();
		fileProvider.addFile("/.encfs6.xml", config);
		volume = new EncFSVolumeBuilder().withFileProvider(fileProvider).withPbkdf2Provider(new OpenPBKDF2Provider()).withPassword(password).buildVolume();
		
		for(int i=0; i < encryptedFilenames.length; i++){
			fileProvider.addFile("/" + encryptedFilenames[i], new byte[1]);
		}
		
		EncFSFile[] infolist = volume.listFilesForPath("/");
		
		String[] result = new String[encryptedFilenames.length];
		for(int i=0; i < infolist.length; i++){
			result[i] = infolist[i].getName();
		}
		return result;
	}
	

	public final static String[] decryptFilenames(String[] encryptedFilenames, oracle.sql.BLOB config, String password) 
			throws SQLException, EncFSCorruptDataException, EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, IOException {
		
		byte[] convertedConfig = config.getBytes(1, (int) config.length());
		
		return decryptFilenames(encryptedFilenames, convertedConfig, password);
	}
	
	/**
	 * Decrypts a single filename, usable in Oracle database environments
	 * @param encryptedFilename Encrypted filename
	 * @param config EncFS configuration file as {@link oracle.sql.BLOB}
	 * @param password Password used when creating the EncFS volume
	 * @return Decrypted filename
	 * 
	 * @throws EncFSCorruptDataException
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public final static String decryptFilename(String encryptedFilename, oracle.sql.BLOB config, String password) 
			throws EncFSCorruptDataException, EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, SQLException, IOException{
		
		return decryptFilenames(new String[]{encryptedFilename}, config, password)[0];
	}
	
	/**
	 * Decrypts a single filename
	 * @param encryptedFilename Encrypted filename
	 * @param config EncFS configuration file
	 * @param password Password used when creating the EncFS volume
	 * @return Decrypted filename
	 * 
	 * @throws EncFSCorruptDataException
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * @throws SQLException
	 * @throws IOException
	 * 
	 */
	public final static String decryptFilename(String encryptedFilename, byte[] config, String password) 
			throws EncFSCorruptDataException, EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, SQLException, IOException{
		
		return decryptFilenames(new String[]{encryptedFilename}, config, password)[0];
	}
	
	/**
	 * Encrypts an array of filenames
	 * @param unencryptedFilenames Unencrypted filenames
	 * @param config EncFS configuration file
	 * @param password Password used when creating the EncFS volume
	 * @return Encrypted filenames
	 * 
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * @throws EncFSCorruptDataException
	 * @throws IOException
	 * 
	 */
	public final static String[] encryptFilenames(String[] unencryptedFilenames, byte[] config, String password) 
			throws EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, EncFSCorruptDataException, IOException{
		
		EncFSVolume volume = new EncFSVolume();
		SimulatedFileProvider fileProvider = new SimulatedFileProvider();
		fileProvider.addFile("/.encfs6.xml", config);
		volume = new EncFSVolumeBuilder().withFileProvider(fileProvider).withPbkdf2Provider(new OpenPBKDF2Provider()).withPassword(password).buildVolume();
		
		String[] result = new String[unencryptedFilenames.length];
		for(int i=0; i < unencryptedFilenames.length; i++){
			result[i] = EncFSCrypto.encodeName(volume, unencryptedFilenames[i], unencryptedFilenames[i]);
			System.out.println(result[i]);
		}
		return result;		
	}
	
	/**
	 * Encrypts a single filename 
	 * @param unencryptedFilename Unencrypted filename
	 * @param config EncFS configuration file
	 * @param password Password used when creating the EncFS volume
	 * @return Encrypted filename
	 * 
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * @throws EncFSCorruptDataException
	 * @throws IOException
	 * 
	 */
	public final static String encryptFilename(String unencryptedFilename, byte[] config, String password) 
			throws EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, EncFSCorruptDataException, IOException{
		return encryptFilenames(new String[]{unencryptedFilename}, config, password)[0];
	}
	
	/**
	 * Encrypts a single filename, usable in Oracle database environments
	 * @param unencryptedFilename Unencrypted filename
	 * @param config EncFS configuration file as {@link oracle.sql.BLOB}
	 * @param password Password used when creating the EncFS volume
	 * @return Encrypted filename
	 * 
	 * @throws SQLException
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSInvalidPasswordException
	 * @throws EncFSCorruptDataException
	 * @throws IOException
	 * 
	 */
	public final static String encryptFilename(String unencryptedFilename, oracle.sql.BLOB config, String password) 
			throws SQLException, EncFSUnsupportedException, EncFSInvalidConfigException, EncFSInvalidPasswordException, EncFSCorruptDataException, IOException{
		byte[] convertedConfig = config.getBytes(1, (int) config.length());
		return encryptFilename(unencryptedFilename, convertedConfig, password);
	}
	
	/**
	 * Generates a EncFS configuration file, compatible with Boxcryptor Classic
	 * @param newPassword Password to use for the configuration file
	 * @return EncFS configuration file as a byte array
	 * 
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSCorruptDataException
	 * @throws EncFSInvalidPasswordException
	 * @throws IOException
	 * 
	 * @author Samuel Moosmann @ TripleIn software solutions GmbH
	 */
	public final static byte[] generateBoxcryptorConfigFile(String newPassword) 
			throws EncFSUnsupportedException, EncFSInvalidConfigException, EncFSCorruptDataException, EncFSInvalidPasswordException, IOException {
		EncFSConfig config = new EncFSConfig();
		config.setChainedNameIV(false);
		config.setEncodedKeyLengthInBytes(52);
		config.setSaltLengthBytes(20);
		config.setNumberOfMACBytesForEachFileBlock(0);
		config.setNumberOfRandomBytesInEachMACHeader(0);
		config.setFilenameAlgorithm(EncFSFilenameEncryptionAlgorithm.NULL);
		config.setIterationForPasswordKeyDerivationCount(5000);
		config.setUseUniqueIV(false);
		config.setVolumeKeySizeInBits(256);
		config.setHolesAllowedInFiles(true);
		config.setEncryptedFileBlockSizeInBytes(4096);
		
		
		DirectConfigBuilder builder = new DirectConfigBuilder(new OpenPBKDF2Provider(), config, newPassword);
		return builder.generateConfig().getBytes();
	}
	/**
	 * Generates an EncFS configuration file, compatible with Boxcryptor Classic. Usable in Oracle database environments.
	 * @param newPassword Password to use for the configuration file
	 * @return EncFS configuration file as {@link oracle.sql.BLOB}
	 * 
	 * @throws SQLException
	 * @throws EncFSUnsupportedException
	 * @throws EncFSInvalidConfigException
	 * @throws EncFSCorruptDataException
	 * @throws EncFSInvalidPasswordException
	 * @throws IOException
	 * 
	 */
	public final static oracle.sql.BLOB generateBoxcryptorConfigFileAsBlob(String newPassword) 
			throws SQLException, EncFSUnsupportedException, EncFSInvalidConfigException, EncFSCorruptDataException, EncFSInvalidPasswordException, IOException{
		Connection conn = (new OracleDriver()).defaultConnection();
		generateBoxcryptorConfigFile(newPassword);
		oracle.sql.BLOB configFile = oracle.sql.BLOB.createTemporary(conn, false, oracle.sql.BLOB.DURATION_SESSION);
		OutputStream os = configFile.setBinaryStream(0);
		
		os.write(generateBoxcryptorConfigFile(newPassword));
		os.flush();
		os.close();
		
		return configFile;
	}
	
}
