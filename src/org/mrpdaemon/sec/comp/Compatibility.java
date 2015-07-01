package org.mrpdaemon.sec.comp;

/**
 * Includes several methods that are not available in Java 1.5 and had to be replaced to make the tool compatible with 1.5.
 * Parts of the code are copied from the <a href="http://openjdk.java.net">OpenJDK</a> Source Code 
 * @author Samuel Moosmann @ <a href="http://www.triplein.at">TripleIn software solutions GmbH</a>
 *
 */
public final class Compatibility {
	
	/**
	 * Replacement for {@link Arrays.copyOfRange}
	 * @param original
	 * @param from
	 * @param to
	 * @return
	 */
	public static byte[] copyOfRange(byte[] original, int from, int to) {
	    int newLength = to - from;
	    if (newLength < 0)
	        throw new IllegalArgumentException(from + " > " + to);
	    byte[] copy = new byte[newLength];
	    System.arraycopy(original, from, copy, 0,
	                     Math.min(original.length - from, newLength));
	    return copy;
	}
	
	/**
	 * Replacement for {@link Arrays.copyOf}
	 * @param original
	 * @param newLength
	 * @return
	 */
	public static byte[] copyOf(byte[] original, int newLength) {
		byte[] copy = new byte[newLength];
		System.arraycopy(original, 0, copy, 0,
			Math.min(original.length, newLength));
		return copy;
	}
	
}
