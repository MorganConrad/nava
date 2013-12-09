package com.flyingspaniel.nava.net.aws;

import com.flyingspaniel.nava.net.URLEncoding;
import junit.framework.TestCase;

import java.util.Map;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class AWSSignedRequestsHelperTest extends TestCase {

   static final String SECRET_KEY = "secretKey";
   static final String ASSOCIATE_TAG = "associateTag";

   static final String QUERY_STRING = "Service=AWSECommerceService" +
         "&Version=2011-08-01" +
         "&AssociateTag=PUT_ASSOCIATE_TAG_HERE" +
         "&Operation=ItemSearch" +
         "&SearchIndex=Books" +
         "&Keywords=harry+potter";


   public void testReality() throws Exception {
      // I never figured out how to get this to work with command line args...
      String secretKey = System.getProperty(SECRET_KEY, "You are missing your secret key");
      String associateTag = System.getProperty(ASSOCIATE_TAG, "You are missing your secret key");

      // so now doing this - put into a separate class
      secretKey = SecretDoNotCheckIn.SECRET_KEY_VALUE;
      associateTag = SecretDoNotCheckIn.ASSOCIATE_TAG_VALUE;

      String query = QUERY_STRING.replace("PUT_ASSOCIATE_TAG_HERE", associateTag);
      Map<String, String> asMap = AWSSignedRequestsHelper.queryStringToMap(URLEncoding.Impl.RFC3986, query);

      AWSSignedRequestsHelper helper = new AWSSignedRequestsHelper(secretKey);
      String signed = helper.sign(asMap, associateTag, "GET");

      // since Timestamp varies, not much we can do to test signed

//      Response response = Response.fromBasicRequest(signed, HTTPMethod.GET.name(), null);
//      String lotsOfXML = response.getBody().toString();
//
//      assertTrue(lotsOfXML.contains("Deathly-Hallows"));
//      System.out.println(lotsOfXML);
   }
}
