For more information regarding the base API (`encfs-java`), see the original README, named `README_EncFS_Java.md`

Additional functions for encfs-java
======

This project extends the functionality of [encfs-java](https://github.com/mrpdaemon/encfs-java/).
It adds the ability to decrypt and encrypt single files from EncFS setups without any form of file system access. Also, these functions can be used in Oracle database environments, starting with Oracle Database 11g (*previous versions untested*).

To make this possible, the JRE requirement has been lowered to Java 1.5 by replacing some array functions. Also a missing cipher algorithm on Oracle Databases has been replaced with a 3rd party variant.

## Requirements

- Oracle Database 11g+ (10g *might* be supported, depends on Java 1.4 compatibility)
- Installed and activated Java VM on database (see [Java Installation and Configuration](http://docs.oracle.com/cd/B28359_01/java.111/b31225/chfour.htm#BABCFGAB))
- [Java Cryptography Extension - Unlimited Strength Jurisdication Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html) installed
- Java 1.5+

### Java Build Dependencies

- Java Development Kit 1.5+
- [Oracle JDBC Drivers + DMS](http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html) (included)



## Installation

Using the Java functions in PL/SQL requires loading the Java Archive into the database and binding the Java methods to PL/SQL functions. This can be done by following these steps:

1. Place the **ENCFS.jar** file on the server in a directory, which is visible to the database (see [Oracle / CREATE DIRECTORY](http://docs.oracle.com/cd/B12037_01/server.101/b10759/statements_5007.htm) for more information)
2. Edit the **/server/loadEncFS.sql** file and replace "/path/to/ENCFS.jar" in it with the full path to your ENCFS.jar file
3. Execute the edited script now. After some seconds the Java classes should show up in the database
4. Execute the **/server/createPackage.sql** to create the `docEncFS` package in your database. It binds the usable Java methods to PL/SQL functions
5. The methods should now be useable by calling the functions in the `docEncFS` package


## Usage

The configuration file for EncFS is usually named **.encfs6.xml** or **Keyfile.bcx** (Boxcryptor) and stored in the encrypted EncFS folder. It contains the keys needed for the EncFS encryption. 

### Java development

The functions in **EncFSCrypt.java** can be used to decrypt, encrypt data and filenames and generate configuration files for EncFS.  

Technically it works by using a modified File Provider called `SimulatedFileProvider`, which stores a list of files and their raw data in a Map. Whenever the EncFS-java API calls a file from the provider, this raw data is passed instead of a real file. 

See the attached JavaDoc in **/doc** for more information on every included method. 

### PL/SQL development

Follow the installation guide above to use these functions in PL/SQL.


#### Functions

	fDecrypt (i_lbEncryptedData blob, i_lbConfig blob, i_vcPassword varchar2) return blob

Uses the i_lbEncryptedData and decrypts it using the keys from the passed config file and the password.

*bound to `EncFSSingleFile.decrypt(...)`*

----------


	fEncrypt (i_lbUnencryptedData blob, i_lbConfig blob, i_vcPassword varchar2) return blob

Encrypts the data in i_lbUnencryptedData using the keys from the passed config file and the password.

*bound to `EncFSSingleFile.encrypt(...)`*

----------


	fDecryptFilename (i_vcEncryptedFilename varchar2, i_lbConfig blob, i_vcPassword varchar2) return blob

Decrypts the filename using the keys from the passed config file and the password.

*bound to `EncFSSingleFile.decryptFilename(...)`*

----------

	
	fEncryptFilename (i_vcUnencryptedFilename varchar2, i_lbConfig blob, i_vcPassword varchar2) return blob

Encrypts the filename using the keys from the passed config file and the password.

*bound to `EncFSSingleFile.encryptFilename(...)`*

----------


	fGenerateBoxcryptorConfig (i_vcPassword varchar2) return blob

Generates a EncFS configuration file, which can be used with Boxcryptor Classic

*bound to `EncFSSingleFile.generateBoxcryptorConfigFileAsBlob(...)`*


## Updating

The base source, [encfs-java](https://github.com/mrpdaemon/encfs-java/) was not directly modified while adding the functions. Therefore it's possible to update the encFS-java components in case a new version is released. Functionality should remain the same as long as interfaces and abstract methods still have the same specifications. 

For reference, these are the files that were created / modified since the commit [5f477a9aee](https://github.com/mrpdaemon/encfs-java/commit/5f477a9aee8985068aec38b214f19124e8dc1c74) of encfs-java:

- Created
	- EncFSSingleFile.java
	- Compatibility.java
	- OpenPBKDF2.java
	- DirectConfigBuilder.java
	- OpenPBKDF2Provider.java
	- SimulatedFileProvider.java
- Modified for Java 1.5 compatibility (see `comp` package for more info)
	- BasicFilenameDecryptionStrategy.java
	- BasicFilenameEncryptionStrategy.java
	- BlockFilenameDecryptionStrategy.java
	- BlockFilenameEncryptionStrategy.java
	- EncFSCrypto.java
	- EncFSOutputStream.java
	- EncFSVolume.java
	- StreamCrypto.java
	- VolumeKey.java

## Licensing

SingleFileEncFS is licensed under the Lesser GNU Public License, which allows non-GPL
applications to make use of the library with the restriction that the source code
for any modifications to the library itself need to be made available to be able
to legally redistribute the modified library. For more information, please see the
LICENSE file and the Free Software Foundation
[website](http://www.gnu.org/licenses/lgpl.html).