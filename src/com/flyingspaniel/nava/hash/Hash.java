package com.flyingspaniel.nava.hash;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 *
 * Utilities to simplify reading values from a Map
 * <p>
 * Typical name is  <code>getType(Map map, String key, Type...or)</code>
 * <p>
 * Almost all methods take the varargs or... argument.  Only the 0th value is ever used.
 * If present, and the key is not in the map, the or[0] will be returned
 * If not present, and the key is not in the map, a NoSuchKeyException will be thrown
 * <p>
 * 
 * 
 */
public class Hash {

   /**
    * Get the value for that key (mainly used internally)
    * @param map  non-null
    * @param key  generally non-null
    * @param mustBePresent if true, and key does not exist, will throw a NoSuchKeyException
    * @return  value, possibly null iff !mustBePresent
    */
   public static Object get(Map<String,?> map, String key, boolean mustBePresent) {
      Object v = map.get(key);
      if (v == null && mustBePresent)
         throw new NoSuchKeyException("No key exists for: " + key);

      return v;
   }

   /**
    * Return the value for that key, converted to a boolean
    * @param map  non-null
    * @param key  generally non-null
    * @param or   optional
    * @return     boolean
    */
   public static boolean getBoolean(Map<String,?> map, String key, boolean...or) {
      Object v = get(map, key, or.length == 0);
      return (v != null) ? To.booleanFrom(v) : or[0];
   }

   /**
    * Return the value for that key, converted to a double
    * @param map  non-null
    * @param key  generally non-null
    * @param or   optional
    * @return     double
    */
   public static double getDouble(Map<String,?> map, String key, double...or) {
      Object v = get(map, key, or.length == 0);
      return (v != null) ? To.doubleFrom(v) : or[0];
   }

   /**
    * Return the value for that key, converted to a int
    * @param map  non-null
    * @param key  generally non-null
    * @param or   optional
    * @return     int
    */
   public static int getInt(Map<String,?> map, String key, int...or) {
      Object v = get(map, key, or.length == 0);
      return (v != null) ? To.intFrom(v) : or[0];
   }

   /**
    * Return the value for that key, converted to a String
    * @param map  non-null
    * @param key  generally non-null
    * @param or   optional
    * @return     String
    */
   public static String getString(Map<String,?> map, String key, String...or) {
      Object v = get(map, key, or.length == 0);
      return (v != null) ? v.toString() : or[0];
   }


   /**
    * Return an Object from the map
    * @param map
    * @param key
    * @param defaultObject   returned if key not in the map.
    * @return  Object
    */
   public static Object getObject(Map<String,?> map, String key, Object defaultObject) {
      Object v = get(map, key, defaultObject == null);
      return (v != null) ? v : defaultObject;
   }


   /**
    * Wrapper class that just calls the statics.  
    * TODO  This may become a "true" wrapper class.
    */
   public static class Wrapper extends ConcurrentHashMap<String, Object> {

      public Wrapper() { super(); }
      public Wrapper(Map map) {  super(map); }

      public boolean getBoolean(String key, boolean...or) {
         // special case, default to false if nothing is found
         return (or.length > 0) ? Hash.getBoolean(this, key, or) : Hash.getBoolean(this, key, false);
      }
      public double getDouble(String key, double...or) {
         return Hash.getDouble(this, key, or);
      }
      public int getInt(String key, int...or) {
         return Hash.getInt(this, key, or);
      }
      public String getString(String key, String...or) {
         return Hash.getString(this, key, or);
      }
      public Object getObject(String key, Object defaultObject) {
         return Hash.getObject(this, key, defaultObject);
      }
   }


   public static class NoSuchKeyException extends RuntimeException {
      public  NoSuchKeyException(String message)  {
         super(message);
      }
   }

}