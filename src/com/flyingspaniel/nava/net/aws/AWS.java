package com.flyingspaniel.nava.net.aws;

/**
 * Constants and enums for Amazon Web Services
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class AWS {

   public static String ENDPOINT = "webservices.amazon.com"; // must be lowercase
   public static final String REQUEST_URI = "/onca/xml";

   private AWS() { ; }  // utility class, not to be instantiated...

   public final static String URLPreface(String endpoint, String uri) {
      return "http://" + endpoint + uri + "?";
   }


   /**
    * Enum for keys for queries (go to the left of the = in the URL)
    * Note that we use mixed case so the name() exactly matches the URL terms
    */
   public enum Key {
      AssociateTag,
      AWSAccessKeyId,
      Keywords,
      Operation,
      SearchIndex,
      Signature,
      Timestamp,
      Version;


      public String asQuery() {
         return  "&" + name() + "=";
      }


   }

}
