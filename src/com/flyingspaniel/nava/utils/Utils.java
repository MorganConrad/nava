package com.flyingspaniel.nava.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class Utils {

   public static <T> boolean nullOrEmpty(T[] array) {
      return (array == null) || (array.length == 0);
   }

   public static boolean nullOrEmpty(String s) {
      return (s == null) || (s.length() == 0);
   }


   public static void closeQuietly(Closeable c) {
      if (c != null)
         try {
            c.close();
         } catch (IOException e) {
            ;  // ignore
         }
   }

   public static boolean smellsLikeJSON(String in) {
      return (in != null) && in.trim().startsWith("{");
   }

   public static String[] pop(String[] in) {
      String[] copy = new String[in.length-1];
      System.arraycopy(in, 0, copy, 1, in.length-1);
      return copy;
   }

   public static String[] shift(String[] in) {
      String[] copy = new String[in.length-1];
      System.arraycopy(in, 1, copy, 0, in.length-1);
      return copy;
   }
}
