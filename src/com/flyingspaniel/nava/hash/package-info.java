/**
 *
 * @author Morgan Conrad
 * <p>
 * Prelimary utility code for pulling values from Maps.
 * Useful because JavaScript/node.js usually use { } "objects" or "hashes" to pass settings and options.
 * <p>
 * To.java converts a raw Object "to" a different type, with two main method signatures:
 * <ul>
 *    <li><code>type typeFrom(Object in)</code> converts a single Object to a type</li>
 *    <li><code>type typeOr(Object in, type...or)</code> mimics the JavaScript || operator, returning the 2nd arg if the 1st is null</li>
 * </ul>
 * Hash.java gets the value from a Map.  The major method signature is
 * <code>getType(Map map, String key, Type...or)</code><br>
 *    Note: Hash.Wrapper is likely to be changed.
 * <p>
 * Almost all methods take the varargs or... argument.  Only the 0th value is ever used.
 * <ul>
 *    <li>If the key is not in the map, and or is present, or[0] is returned</li>
 *    <li>If the key is not in the map, and or is not present, a Hash.NoSuchKeyException is thrown</li>
 * </ul>
 */

package com.flyingspaniel.nava.hash;
