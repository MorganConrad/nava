package com.flyingspaniel.nava.hash;

import java.util.Collection;

/**
 * Utilities for converting "To" other formats, with some JavaScript conventions.
 * <ul>
 *    <li>type typeFrom(Object in) convert a single Object to a type</li>
 *    <li>type typeOr(Object in, type or) mimic the JavaScript || operator, to use the 2nd arg if the 1st is null</li>
 * </ul>
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */

public class To {

   private To() {} // utility

   /**
    * JavaScript style "false"
    * @param  in
    * @return boolean
    */
   public static boolean isFalsy(Object in) {
      if (in == null)
         return true;
      if (in instanceof Boolean)
         return !((Boolean)in).booleanValue();
      if (in instanceof Number) {
         Number n = (Number)in;
         return (n.intValue() == 0) || (n.doubleValue() == Double.NaN);
      }
      String s = in.toString();
      return (s.length() == 0) || "0".equals(s) || "false".equalsIgnoreCase(s);
   }

   public static boolean isTruthy(Object in) {
      return !isFalsy(in);
   }

   public static boolean booleanFrom(Object in) {
      return isTruthy(in);
   }

   public static double doubleFrom(Object in) {
      return (in instanceof Number) ?
            ((Number)in).doubleValue() :
            Double.parseDouble(in.toString().trim());
   }

   public static int intFrom(Object in) {
      return (in instanceof Number) ?
            ((Number)in).intValue() :
            Integer.parseInt(in.toString().trim());
   }


   public static boolean booleanOr(Object in, boolean or) {
      return (in != null) ? booleanFrom(in) : or;
   }

   public static double doubleOr(Object in, double or) {
      return (in != null) ? doubleFrom(in) : or;
   }

   public static int intOr(Object in, int or) {
      return (in != null) ? intFrom(in) : or;
   }

   public static String stringOr(Object in, String or)  {
      return  (in != null) ? in.toString() : or;
   }

   public static String string(Object in) {
      return  (in != null) ? in.toString() : "";
   }

   /**
    * Converts a Collection of any object to a String[]
    * @param objects  incoming
    * @param useForNull  if an object is null, use this String to represent it
    * @return  String[], sam size as Collection
    */
   public static String[] stringsFrom(Collection<?> objects, String useForNull) {
      String[] strings = new String[objects.size()];
      int idx = 0;
      for (Object o : objects)
         strings[idx++] = stringOr(o, useForNull);

      return strings;
   }

   public static String trimmedString(Object in) {
      return  (in != null) ? in.toString().trim() : "";
   }


   /**
    * Special case, picks the first non-null Object
    * @param in     most likely candidate
    * @param or     more candidates
    * @return       First non-null Object, or null if all were null
    */
   public static Object objectOr(Object in, Object... or)  {
      Object result = in;
      int idx = 0;
      while (result == null && idx<or.length)
         result = or[idx++];

      return result;
   }


}
