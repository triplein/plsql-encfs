package org.mrpdaemon.sec.encfs;

import java.security.SecureRandom;

/**
 * To avoid editing the EncFS implementation directly, this class was implemented for creating a config file without directly using a file provider. 
 * @author Samuel Moosmann @ <a href="http://www.triplein.at">TripleIn software solutions GmbH</a>
 *
 */
public class DirectConfigBuilder {
	EncFSConfig config;
	EncFSPBKDF2Provider provider;
	String password;
	
	public DirectConfigBuilder(EncFSPBKDF2Provider provider, EncFSConfig config, String password) throws EncFSInvalidConfigException, EncFSUnsupportedException, EncFSCorruptDataException{
		this.provider = provider;
		this.config = config;
		this.password = password;
		generateKeys();
	}
	
	private void generateKeys() throws EncFSInvalidConfigException, EncFSUnsupportedException, EncFSCorruptDataException{
		byte[] randVolKey = new byte[config.getVolumeKeySizeInBits() / 8
         					+ EncFSVolume.IV_LENGTH_IN_BYTES];
        new SecureRandom().nextBytes(randVolKey);
        VolumeKey.encodeVolumeKey(config, password, randVolKey, provider);
	}
	
	public String generateConfig() {
		String result = "";

		result += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		result += "<!DOCTYPE boost_serialization>\n";
		result += "<boost_serialization signature=\"serialization::archive\" version=\"9\">\n";
		result += " <cfg class_id=\"0\" tracking_level=\"0\" version=\"20\">\n";
		result += "\t<version>20100713</version>\n";
		result += "\t<creator>encfs-java"
				+ "</creator>\n";
		result += "\t<cipherAlg class_id=\"1\" tracking_level=\"0\" version=\"0\">\n";
		result += "\t\t<name>ssl/aes</name>\n";
		result += "\t\t<major>3</major>\n";
		result += "\t\t<minor>0</minor>\n";
		result += "\t</cipherAlg>\n";

		result += "\t<nameAlg>\n";

		EncFSFilenameEncryptionAlgorithm algorithm = config
				.getFilenameAlgorithm();
		result += "\t\t<name>" + algorithm.getIdentifier() + "</name>\n";
		result += "\t\t<major>" + algorithm.getMajor() + "</major>\n";
		result += "\t\t<minor>" + algorithm.getMinor() + "</minor>\n";

		result += "\t</nameAlg>\n";

		result += "\t<keySize>"
				+ Integer.toString(config.getVolumeKeySizeInBits())
				+ "</keySize>\n";

		result += "\t<blockSize>"
				+ Integer.toString(config.getEncryptedFileBlockSizeInBytes())
				+ "</blockSize>\n";

		result += "\t<uniqueIV>" + (config.isUseUniqueIV() ? "1" : "0")
				+ "</uniqueIV>\n";

		result += "\t<chainedNameIV>" + (config.isChainedNameIV() ? "1" : "0")
				+ "</chainedNameIV>\n";

		result += "\t<externalIVChaining>"
				+ (config.isSupportedExternalIVChaining() ? "1" : "0")
				+ "</externalIVChaining>\n";

		result += "\t<blockMACBytes>"
				+ Integer
						.toString(config.getNumberOfMACBytesForEachFileBlock())
				+ "</blockMACBytes>\n";
		result += "\t<blockMACRandBytes>"
				+ Integer.toString(config
						.getNumberOfRandomBytesInEachMACHeader())
				+ "</blockMACRandBytes>\n";

		result += "\t<allowHoles>"
				+ (config.isHolesAllowedInFiles() ? "1" : "0")
				+ "</allowHoles>\n";

		result += "\t<encodedKeySize>"
				+ Integer.toString(config.getEncodedKeyLengthInBytes())
				+ "</encodedKeySize>\n";
		result += "\t<encodedKeyData>" + config.getBase64EncodedVolumeKey()
				+ "\n</encodedKeyData>\n";

		result += "\t<saltLen>" + Integer.toString(config.getSaltLengthBytes())
				+ "</saltLen>\n";
		result += "\t<saltData>" + config.getBase64Salt() + "\n</saltData>\n";

		result += "\t<kdfIterations>"
				+ Integer.toString(config
						.getIterationForPasswordKeyDerivationCount())
				+ "</kdfIterations>\n";

		// XXX: We don't support custom KDF durations
		result += "\t<desiredKDFDuration>500</desiredKDFDuration>\n";

		result += "  </cfg>\n";
		result += "</boost_serialization>\n";

		return result;
	}
}
