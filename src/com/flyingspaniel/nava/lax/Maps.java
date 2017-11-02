package com.flyingspaniel.nava.lax;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
public class Maps {

   public static final Object MUST_BE_PRESENT = new Object();

   /**
    * Get the value for that key (mainly used internally)
    * @param map  may be null
    * @param key  generally non-null
    * @param or   returned if key not present    if Maps.MUST_BE_PRESENT will throw an exception
    * @return  value, possibly null iff !mustBePresent
    */
   public static Object objectOr(Map<String, ?> map, String key, Object or) {
      Object v = (map != null) ? map.get(key) : null;
      if (v == null)
         v = or;

      if (MUST_BE_PRESENT==v)
            throw new NoSuchKeyException("No key exists for: " + key);

      return v;
   }



   public static boolean bool(Map<String,?> map, String key) {
      Object v = objectOr(map, key, MUST_BE_PRESENT);
      return To.bool(v);
   }

   /**
    * Return the value for that key, converted to a boolean
    * @param map  non-null
    * @param key  generally non-null
    * @param or   default value if not present
    * @return     boolean
    */
   public static boolean boolOr(Map<String,?> map, String key, boolean or) {
      Object v = objectOr(map, key, null);
      return To.boolOr(v, or);
   }


   public static double real(Map<String,?> map, String key) {
      Object v = objectOr(map, key, MUST_BE_PRESENT);
      return To.real(v);
   }


   /**
    * Return the value for that key, converted to a double
    * @param map  non-null
    * @param key  generally non-null
    * @param or   optional
    * @return     double
    */
   public static double realOr(Map<String,?> map, String key, double or) {
      Object v = objectOr(map, key, null);
      return To.realOr(v, or);
   }


   public static int integer(Map<String,?> map, String key) {
      Object v = objectOr(map, key, MUST_BE_PRESENT);
      return To.integer(v);
   }


   /**
    * Return the value for that key, converted to a int
    * @param map  non-null
    * @param key  generally non-null
    * @param or   optional
    * @return     int
    */
   public static int integerOr(Map<String,?> map, String key, int or) {
      Object v = objectOr(map, key, null);
      return To.integerOr(v, or);
   }

   /**
    * Return the value for that key, converted to a List<Object>  (e.g. for JSON arrays)
    * @param map  non-null
    * @param key  generally non-null
    * @param      create  if true, and key not present, creates new ArrayList
    * @return     List, possibly null or empty
    */
   public static List<Object> list(Map<String,Object> map, String key, boolean create) {
      synchronized(map) {
         Object v = objectOr(map, key, null);
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
   public static Map<String,Object> map(Map<String,Object> map, String key, boolean create) {
      synchronized(map) {
         Object v = objectOr(map, key, null);
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
   public static String stringOr(Map<String,?> map, String key, String or) {
      Object v = objectOr(map, key, or);
      return (v != null) ? v.toString() : or;
   }

   public static String string(Map<String,?> map, String key) {
      return objectOr(map, key, MUST_BE_PRESENT).toString();
   }


   public static class NoSuchKeyException extends RuntimeException {
      public  NoSuchKeyException(String message)  {
         super(message);
      }
   }

}