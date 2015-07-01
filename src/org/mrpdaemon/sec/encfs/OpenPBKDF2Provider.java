package org.mrpdaemon.sec.encfs;

import org.mrpdaemon.sec.comp.OpenPBKDF2;


/**
 * For compatibility with Java versions that don't include the PBKDF2 algorithm, a method from <a href="http://cryptofreek.org/2012/11/29/pbkdf2-pure-java-implementation/">cryptofreek</a> was used and implemented here.
 * @author Samuel Moosmann @ <a href="http://www.triplein.at">TripleIn software solutions GmbH</a>
 *
 */
public class OpenPBKDF2Provider extends EncFSPBKDF2Provider {


	@Override
	public byte[] doPBKDF2(String password, int saltLen, byte[] salt,
			int iterations, int keyLen) {
		byte[] derived = OpenPBKDF2.derive(password, salt, iterations, keyLen);
		return derived;
	}

}
