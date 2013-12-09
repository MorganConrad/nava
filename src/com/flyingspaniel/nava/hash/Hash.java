package com.flyingspaniel.nava.hash;

import java.util.*;
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
    * @param map  may be null
    * @param key  generally non-null
    * @param mustBePresent if true, and key does not exist, will throw a NoSuchKeyException
    * @return  value, possibly null iff !mustBePresent
    */
   public static Object get(Map<String,?> map, String key, boolean mustBePresent) {
      Object v = (map != null) ? map.get(key) : null;
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
    * Return the value for that key, converted to a List<Object>  (e.g. for JSON arrays)
    * @param map  non-null
    * @param key  generally non-null
    * @param      create  if true, and key not present, creates new ArrayList
    * @return     List, possibly null or empty
    */
   public static List<Object> getList(Map<String,Object> map, String key, boolean create) {
      synchronized(map) {
         Object v = get(map, key, false);
         if (v != null || !create)
            return (List<Object>)v;

         List<Object> empty = new ArrayList<Object>();
         map.put(key, empty);
         return empty;
      }
   }

   /**
    * Return the value for that key, converted to a Map<String,Object>  (e.g. for nested JSON)
    * @param map  non-null
    * @param key  generally non-null
    * @param      create  if true, and key not present, creates new LinkedHashMap
    * @return     Map, possibly null or empty
    */
   public static Map<String,Object> getMap(Map<String,Object> map, String key, boolean create) {
      synchronized(map) {
         Object v = get(map, key, false);
         if (v != null || !create)
            return (Map<String,Object>)v;

         Map<String,Object> empty = new LinkedHashMap<String, Object>();
         map.put(key, empty);
         return empty;
      }
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

   public static String getString(Map<String,?> map, String key) {
      Object v = get(map, key, false);
      return (v != null) ? v.toString() : null;
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


   public static class NoSuchKeyException extends RuntimeException {
      public  NoSuchKeyException(String message)  {
         super(message);
      }
   }

}