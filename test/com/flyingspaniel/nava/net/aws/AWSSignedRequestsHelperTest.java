package com.flyingspaniel.nava.net.aws;

import com.flyingspaniel.nava.net.URLEncoding;
import com.flyingspaniel.nava.request.HTTPMethod;
import com.flyingspaniel.nava.request.Request;
import com.flyingspaniel.nava.request.Response;
import junit.framework.TestCase;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class AWSSignedRequestsHelperTest extends TestCase {

   static final String QUERY_STRING = "Service=AWSECommerceService" +
         "&Version=2011-08-01" +
         "&AssociateTag=PUT_ASSOCIATE_TAG_HERE" +         // this may be extraneous...
         "&Operation=ItemSearch" +
         "&SearchIndex=Books" +
         "&Keywords=harry+potter";


   public void testCanned() throws Exception {
      String associateTag = System.getenv("AWS_ASSOCIATE_TAG");
      String secretKey = System.getenv("AWS_SECRET_KEY");

      String query = QUERY_STRING.replace("PUT_ASSOCIATE_TAG_HERE", associateTag);
      Map<String, String> asMap = AWSSignedRequestsHelper.queryStringToMap(URLEncoding.Impl.RFC3986, query);

      AWSSignedRequestsHelper helper = new AWSSignedRequestsHelper(secretKey);
      helper.setDate(new Date(0));
      String signed = helper.sign(asMap, associateTag, "GET");
      assertTrue(signed.contains("Signature=aYMC4fWYMB4pBeTSafPhN0wzrMD5fN7U2XqcsbcAMoc%3D"));
   }

   public void testReality() throws Exception {

      String associateTag = System.getenv("AWS_ASSOCIATE_TAG");
      String secretKey = System.getenv("AWS_SECRET_KEY");

      String query = QUERY_STRING.replace("PUT_ASSOCIATE_TAG_HERE", associateTag);
      Map<String, String> asMap = AWSSignedRequestsHelper.queryStringToMap(URLEncoding.Impl.RFC3986, query);

      AWSSignedRequestsHelper helper = new AWSSignedRequestsHelper(secretKey);
      String signed = helper.sign(asMap, associateTag, "GET");

      Request request = new Request(signed, HTTPMethod.GET);
      Response response = request.call();
      String lotsOfXML = response.getBody().toString();

      assertTrue(lotsOfXML.contains("Deathly-Hallows"));
   }
}
