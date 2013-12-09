package com.flyingspaniel.nava.net;


import com.flyingspaniel.nava.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.Map;


/**
 * Interface for URL Encoding
 *
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 */
public interface URLEncoding {

   public static final String UTF8_CHARSET = "UTF-8";
   public static final String COMMA = ",";
   public static final String EQUALS = "=";

   final static String[] EMPTY = new String[0];

   /**
    * Encode the Object.  Nulls -> ""
    * @param o
    * @return  the encoded String
    */
   public String encode(Object o);


   /**
    * Decode the encoded String
    * @param in
    * @return  the decoded String
    */
   public String decode(String in);

   /*
    *  the following methods are more "utility" and I'm not sure where they belong
    *  but this seems like a reasonable spot
    */

   /**
    * Decodes a String of the format key[=][value1][,value2...]
    * @param keyPlusValues
    * @return  .getKey() has key, .getValues has String[], possibly empty
    */
   public Map.Entry<String, String[]> decodeKVs(String keyPlusValues);

   /**
    * Encodes into key=value1,value2...
    * @param key
    * @param values
    * @return String
    */
   public String encodeKVs(String key, Object...values);


   static final String[][] RFC3986_CHANGES = {
         { "*", "%2A" },
         { "%7E", "~" },
         { "+", "%20" } // assumes we run after a JAVA encoder
   };


   /**
    * Enum holding three implementations of URLEncoding
    */
   public enum Impl implements URLEncoding {

      /**
       * Does nothing
       */
      NONE {
         public String encode(Object o) {
            return toStringNullsBecome(o, "");
         }

         public String decode(String in) {
            return in;
         }
      },

      /**
       * uses Java's URLEncoder and URLDecoder. ' ' -> '+'
       *
       * @see java.net.URLEncoder
       * @see java.net.URLDecoder
       */
      JAVA {
         public String encode(Object o) {
            String stringValue = toStringNullsBecome(o, "");
            try {
               return URLEncoder.encode(stringValue, UTF8_CHARSET);
            } catch (UnsupportedEncodingException uee) {
               return noUTF8(uee);
            }
         }

         public String decode(String in) {
            try {
               return URLDecoder.decode(in, UTF8_CHARSET);
            } catch (UnsupportedEncodingException uee) {
               return noUTF8(uee);
            }
         }
      },

      /**
       * strict RFC3986, e.g. ' ' -> "%20"
       * Based upon Amazon's example code.
       */
      RFC3986 {
         public String encode(Object o) {
            String encoded = JAVA.encode(o);  // first encode Java style
            for (int i = 0; i < RFC3986_CHANGES.length; i++)
               encoded = encoded.replace(RFC3986_CHANGES[i][0], RFC3986_CHANGES[i][1]);

            return encoded;
         }

         public String decode(String in) {
            for (int i = 0; i < RFC3986_CHANGES.length; i++)
               in = in.replace(RFC3986_CHANGES[i][1], RFC3986_CHANGES[i][0]);

            return JAVA.decode(in);
         }
      };


      // end of ENUM declarations.  Methods shared by all follow...

      @Override
      public Map.Entry<String, String[]> decodeKVs(String keyPlusValues) {
         String[] split = keyPlusValues.split(EQUALS);
         String[] values = (split.length > 1) ? decode(split[1]).split(COMMA) : EMPTY;
         for (int i=0; i<values.length; i++)
            values[i] = decode(values[i]);
         return new AbstractMap.SimpleImmutableEntry<String, String[]>(decode(split[0]), values);
      }



      @Override
      public String encodeKVs(String key, Object...values) {
         StringBuilder sb = new StringBuilder();
         sb.append(encode(key));
         for (int i=0; i<values.length; i++) {
            sb.append( i==0? EQUALS:COMMA);
            sb.append(encode(values[i]));
         }

         if (key.length() == 0)
            return sb.substring(1);  // no leading =
         else
            return sb.toString();
      }


      /**
       * Utility to obtain the correct encoder
       *
       * @param doEncode
       * @param useStrictRFC3986
       * @return never null
       */
      public static URLEncoding getInstance(boolean doEncode, boolean useStrictRFC3986) {
         if (!doEncode)
            return NONE;
         return useStrictRFC3986 ? RFC3986 : JAVA;
      }

      /**
       * Like @link {@link String#valueOf(Object)} but you get to pick the result for nulls
       *
       * @param o
       * @param nullsBecome  what String to return if o is null
       * @return String
       */
      public static String toStringNullsBecome(Object o, String nullsBecome) {
         return (o != null) ? o.toString() : nullsBecome;
      }

      /**
       * UTF-8 is always supported by Java, so, in theory, this is impossible
       *
       * @return for convenience with the compiler, declares that it returns a
       *         String, but it never will
       * @throws Error
       */
      static String noUTF8(UnsupportedEncodingException uee) throws Error {
         throw new Error("UTF8_CHARSET not supported", uee);
      }

   }

}

