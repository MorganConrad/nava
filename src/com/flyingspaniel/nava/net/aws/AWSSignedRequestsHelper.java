package com.flyingspaniel.nava.net.aws;

import com.flyingspaniel.nava.lib3rdparty.Base64Coder;
import com.flyingspaniel.nava.request.HTTPMultiMap;
import com.flyingspaniel.nava.net.URLEncoding;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;



/**
 * Based upon Amazon's example code at
 *  http://code.google.com/p/amazon-product-advertising-api-sample/source/browse/src/com/amazon/advertising/api/sample/SignedRequestsHelper.java?r=3
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */

public class AWSSignedRequestsHelper {
   private static final String UTF8_CHARSET = "UTF-8";
   private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
   private static final String REQUEST_METHOD = "GET";

   protected Calendar calendar;
   protected DateFormat dateFormat;

   private SecretKeySpec secretKeySpec = null;
   private Mac mac = null;


   /**
    * Constructor
    *
    * @param awsSecretKey
    * @throws GeneralSecurityException
    * @throws UnsupportedEncodingException
    */
   public AWSSignedRequestsHelper(String awsSecretKey) throws GeneralSecurityException {
      byte[] secretKeyBytes;
      try {
         secretKeyBytes = awsSecretKey.getBytes(UTF8_CHARSET);
      } catch (UnsupportedEncodingException e) {
         throw new Error("UTF8_CHARSET not supported", e);
      }
      secretKeySpec = new SecretKeySpec(secretKeyBytes, HMAC_SHA256_ALGORITHM);
      mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
      mac.init(secretKeySpec);

      calendar = Calendar.getInstance();
      dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
   }


   /**
    * Signs a request
    *
    * @param params
    * @param awsAccessKeyId  if null, you have already added it and the timestamp manually
    * @return the URL you should then GET
    */
   public String sign(Map<String, String> params, String awsAccessKeyId, String method, String endpoint, String uri) {
      String canonicalQS = generateCanonicalQuery(params, awsAccessKeyId);

      String toSign =
            method + "\n"
                  + endpoint + "\n"
                  + uri + "\n"
                  + canonicalQS;

      String hmac = hmac(toSign);
      String sig = URLEncoding.Impl.RFC3986.encode(hmac);
      String url = AWS.URLPreface(endpoint, uri) + canonicalQS + AWS.Key.Signature.asQuery() + sig;

      return url;
   }

   public String sign(Map<String, String> params, String awsAccessKeyId, String method) {
      return sign(params, awsAccessKeyId, method, AWS.ENDPOINT, AWS.REQUEST_URI);
   }


   public String generateCanonicalQuery(Map<String, String> params, String awsAccessKeyId) {
      if (awsAccessKeyId != null) {
         params.put(AWS.Key.AWSAccessKeyId.name(), awsAccessKeyId);
         params.put(AWS.Key.Timestamp.name(), timestamp());
      }

      Map<String, String> sorted = new TreeMap<String, String>(params);
      StringBuilder sb = new StringBuilder();

      for (Map.Entry<String, String> entry : sorted.entrySet()) {
         sb.append("&" + URLEncoding.Impl.RFC3986.encodeKVs(entry.getKey(), entry.getValue()));
      }

      return sb.substring(1);  // remove leading &
   }


   /**
    * Converts a query string to a (sorted) Map
    *
    * @param encoding
    * @param queryString
    * @return a SortedMap which is never-null but may be empty.
    */
   public static Map<String, String> queryStringToMap( URLEncoding encoding, String queryString) {
      HTTPMultiMap hmm = new HTTPMultiMap();
      hmm.addQueryString(encoding, queryString);

      return hmm.toSingleMap(true, true);
   }



   private String hmac(String stringToSign) {
      String signature = null;
      byte[] data;
      byte[] rawHmac;
      try {
         data = stringToSign.getBytes(UTF8_CHARSET);
      } catch (UnsupportedEncodingException e) {
         // cannot happen since the constructor checks that UTF8 is supported
         throw new Error(UTF8_CHARSET + " is unsupported!", e);
      }
      rawHmac = mac.doFinal(data);
      signature = new String(Base64Coder.encode(rawHmac, 0, rawHmac.length));
      return signature;
   }


   public String timestamp() {
      return dateFormat.format(calendar.getTime());
   }



}

