package com.flyingspaniel.nava.request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;




import junit.framework.TestCase;

public class RequestTest extends TestCase {

   static final String GOOGLE_URL = "http://www.google.com";

   static final Object[] FORMS = { "form1", "red", "form2", 2.34 };
   static final Object[] QUERIES = { "foo", "bar", "total count", 1 };
   static final String[] HEADERS = { "Content-Type", "PeanutButterAndJelly", "Accept-Encoding", "EasyToBreak" };

   static Request makeRequest() {
      return new Request("", HTTPMethod.GET);
   }


   // this may throw a Stub! exception so the name is changed to donttest
   public void testHTTPUtilsBASICAuthorization() {
      Request request = makeRequest();
      request.auth( "user", "password", true);
      request.doAuth(null);
      assertEquals("Basic dXNlcjpwYXNzd29yZA==", request.headersMMap.map.get(Request.AUTHORIZATION).get(0));
      request.auth(null, null, true);
      request.doAuth(null);
      assertFalse(request.headersMMap.map.containsKey(Request.AUTHORIZATION));
   }


   public void testRequest1() throws IOException {
      Request request = new Request("http://foo.com", HTTPMethod.GET);
      request.query(QUERIES);
      request.form(FORMS);
      assertEquals("http://foo.com", request.baseUrl);   // TODO
      assertEquals("http://foo.com?foo=bar&total+count=1&form1=red&form2=2.34", request.fullURL());

      request.method(HTTPMethod.POST);  // POST requires data, and form data isn't it
      assertEquals("http://foo.com?foo=bar&total+count=1", request.fullURL()); // form data gone from URL
      String mockUploadData = "Preface to upload data:" + Request.FORM_DATA;
      assertEquals("Preface to upload data:form1=red&form2=2.34", request.doSubstituteMultiMapData(mockUploadData));
   }


   public void testMethodCalcs() throws IOException {
      Request request = new Request("http://foo.com");
      HttpURLConnection conn = request.openConnection();
      request.prepareConnection(conn);
      assertEquals("GET", conn.getRequestMethod());
      try {
         request.upload("some data"); fail();
      }
      catch (IllegalStateException expected) {}

      request = new Request("http://foo.com");
      request.upload("some data");
      conn = request.openConnection();
      request.prepareConnection(conn);
      assertEquals("POST", conn.getRequestMethod());
   }


   public void testRequestProperties() throws IOException {
      Request request = new Request(GOOGLE_URL, HTTPMethod.GET);
      request.headers("Content-Type", "PeanutButterAndJelly", "Accept-Encoding", "EasyToBreak");
      request.setHeaders("Accept-Encoding", "Caesar-Cipher");  // will replace "EasyToBreak"
      request.headers("Content-Type", "Tuna Salad");        // will add-to "PeanutButterAndJelly"

      request.options("{\"timeout\": 222, \"foo\":\"bar\", \"array\": [1,2,3,4]}");
      HttpURLConnection conn = request.openConnection();
      request.prepareConnection(conn);
      assertEquals(222, conn.getConnectTimeout());

      Map m = conn.getRequestProperties();
      assertEquals("{Accept-Charset=[UTF-8], Accept-Encoding=[Caesar-Cipher], Content-Type=[Tuna Salad, PeanutButterAndJelly]}", m.toString());
   }




}
