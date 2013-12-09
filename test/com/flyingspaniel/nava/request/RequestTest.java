package com.flyingspaniel.nava.request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;




import junit.framework.TestCase;

public class RequestTest extends TestCase {

   static final String GOOGLE_URL = "http://www.google.com";

   static final Object[] QUERIES = { "foo", "bar", "total count", 1 };
   static final String[] HEADERS = { "Content-Type", "PeanutButterAndJelly", "Accept-Encoding", "EasyToBreak" };

   static Request makeRequest() {
      return new Request("", HTTPMethod.GET);
   }


   // this may throw a Stub! exception so the name is changed to donttest
   public void testHTTPUtilsBASICAuthorization() {
      Request request = makeRequest();
      request.auth( "user", "password", true);
      request.doAuth();
      assertEquals("BASIC dXNlcjpwYXNzd29yZA==", request.headersMMap.map.get(Request.AUTHORIZATION).get(0));
      request.auth(null, null, true);
      request.doAuth();
      assertFalse(request.headersMMap.map.containsKey(Request.AUTHORIZATION));
   }


   public void testRequest1() throws IOException {

      Request request = new Request("http://foo.com", HTTPMethod.GET);
      request.query(QUERIES);
      assertEquals("http://foo.com", request.baseUrl );   // TODO

      request = new Request("http://foo.com", HTTPMethod.POST);  // POST requires data
      request.query(QUERIES);
      String mockUploadData = "Preface to upload data:" + Request.QUERY_DATA;
      assertEquals("Preface to upload data:foo=bar&total+count=1", request.doSubstituteMultiMapData(mockUploadData));
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

      Map<String, String> standardProperties = new HashMap<String, String>();

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




   public void testARealHTTPResponse() throws IOException {
      Response response = new Request(GOOGLE_URL).call();
      assertEquals("text/html; charset=ISO-8859-1", response.getContentType());
      assertTrue(response.getBody().toString().startsWith("<!doctype html>"));
      assertEquals("OK", response.getResponseMessage());
      assertEquals(200, response.getResponseCode());
      assertEquals(HTTPMethod.GET, response.getRequest().method);
      assertEquals("[HTTP/1.1 200 OK]", response.getHeaderFields().get(null).toString());
      assertEquals(GOOGLE_URL, response.getURL().toString() );

      assertTrue(response.toString().contains("google"));

      response.throwIOException();  // should do nothing
      assertNull(response.getIOException());
      assertTrue(response.wasSuccessful());
   }


   public void testMilestones() throws IOException {

      Request request = new Request("malformed URL", HTTPMethod.GET);
      Response response = request.call();
      assertEquals(Request.Milestone.OpenConnection.errorCode(), response.getResponseCode());

      request = new Request("http://this.doesnt.exist", HTTPMethod.GET);
      response = request.call();
      assertEquals(Request.Milestone.Connect.errorCode(), response.getResponseCode());

      request = new Request(GOOGLE_URL, HTTPMethod.GET);
      request.options("{'timeout': 1}");   // set an absurdly low connection timeout
      response = request.call();
      int responseCode = response.getResponseCode();

      if (responseCode != 200)        // but sometimes it still works...  Step through in debugger to slow it down...
         assertEquals(Request.Milestone.Connect.errorCode(), responseCode);

      request.options(("{'timeout': 2000, 'readTimeout': 1}"));  // set an absurdly low response timeout
      response = request.call();
      // sometimes this works anyway...
      assertEquals(Request.Milestone.Download.errorCode(), response.getResponseCode());
   }
}
