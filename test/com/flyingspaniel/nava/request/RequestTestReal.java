package com.flyingspaniel.nava.request;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Map;

/**
 * Since these tests hit real servers they are split off from RequestTest
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class RequestTestReal extends TestCase {

   static final String GOOGLE_URL = "http://www.google.com";
   static final String HTTPBIN = "http://httpbin.org/";
   static final String FOUR_SCORE = "Fourscore and seven years ago";

   static final Object[] FORMS = { "form1", "red", "form2", 2.34 };
   static final Object[] QUERIES = { "foo", "bar", "total count", 1 };
   static final String[] HEADERS = { "Content-Type", "PeanutButterAndJelly", "Accept-Encoding", "EasyToBreak" };


   public void testGoogle() throws IOException {
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

   public void testHeaders() {
      Request request = new Request(HTTPBIN + "headers");
      request.headers("dumbHeader", "dumbValue");
      request.acceptCharset("ISO-8859-1");
      String s = request.call().getBody().toString();
      assertTrue(s.contains("Dumbheader\": \"dumbValue"));
      assertTrue(s.contains("Accept-Charset\": \"ISO-8859-1"));
   }

   public void testErrcode() {
      Request request = new Request(HTTPBIN + "status/418");
      Response response = request.call();
      assertEquals(418, response.getResponseCode());
      assertEquals(400, response.responseGroup());
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
      request.baseUrl = "http://httpbin.org/delay/3";
      response = request.call();
      // sometimes this works anyway...
      assertEquals(Request.Milestone.Download.errorCode(), response.getResponseCode());
   }

   public void testPost() {
      Request request = new Request(HTTPBIN + "post", HTTPMethod.POST);
      request.query(QUERIES);
      request.form(FORMS);
      Response response = request.call();
      String body = response.getBody().toString();
      // reading the form data is trickier
      Map<String, Object> responseMap = request.JSONtoMap(body);
      assertEquals("http://httpbin.org/post?foo=bar&total+count=1", responseMap.get("url"));
      assertEquals("red", ((Map)responseMap.get("form")).get("form1")) ;
   }

   public void testPut() {
      Request request = new Request(HTTPBIN + "put", HTTPMethod.PUT);
      request.query(QUERIES);
      StringBufferInputStream baos = new StringBufferInputStream(FOUR_SCORE);
      request.pipe(baos);
      Response response = request.call();
      String body = response.getBody().toString();
      assertTrue(body.contains("data\": \"" + FOUR_SCORE));
      assertTrue(body.contains("url\": \"http://httpbin.org/put?foo=bar&total+count=1"));
   }

   public void testRedirects() {
      Request request = new Request(HTTPBIN + "redirect-to");
      request.query("url", "http://example.com/");
      Response response = request.call();
      String body = response.getBody().toString();
      assertTrue(body.contains("<h1>Example Domain"));

      request.options("{ \"followRedirect\": false, \"useCaches\": false }");
      response = request.call();
      assertEquals(302, response.getResponseCode());
   }

   public void testBasicAuth1() {
      Request request = new Request(HTTPBIN + "basic-auth/user/secret", HTTPMethod.GET);
      request.options("{ \"useCaches\": false }");
      request.auth("user", "secret", false); // false = allow delayed auth
      Response response = request.call();
      String body = response.getBody().toString();
      assertTrue(body.contains("\"authenticated\": true"));
      assertTrue(body.contains("\"user\": \"user\""));
      request.auth("user", "secret", false); // recharge this
      request.baseUrl = HTTPBIN + "basic-auth/user/someotherPassword";
      response = request.call();
      assertEquals(401, response.getResponseCode());

      // HTTPBins "Hidden Basic Auth" requires you send auth the 1st time
      request = new Request(HTTPBIN + "hidden-basic-auth/user/secret", HTTPMethod.GET);
      request.auth("user", "secret", true);
      response = request.call();
      body = response.getBody().toString();
      assertTrue(body.contains("\"authenticated\": true"));
      assertTrue(body.contains("\"user\": \"user\""));
   }


   public void testDigestAuth1() {
      Request request = new Request(HTTPBIN + "digest-auth/auth/user/passwd", HTTPMethod.GET);
      request.auth("user", "passwd", false);  // false = delayed auth
      request.options("{ \"useCaches\": false }");
      request.cookie("needed after commit 4870f70");  // as per https://github.com/kennethreitz/httpbin/issues/124
      Response response = request.call();
      String body = response.getBody().toString();
      assertTrue(body.contains("\"authenticated\": true"));
      assertTrue(body.contains("\"user\": \"user\""));
   }

   // never got this to work, commented out
   public void doNotRuntestDigestAuth2() {
      Request request = new Request("http://test.webdav.org/auth-digest/", HTTPMethod.GET);
      request.auth("user8", "user8", false);  // false = delayed auth
      Response response = request.call();
      String body = response.getBody().toString();
      assertTrue(body.contains("\"authenticated\": true"));
      assertTrue(body.contains("\"user\": \"user\""));
      request.auth("user", "wrongPassword", true);
      request.baseUrl = HTTPBIN + "basic-auth/user/someotherPassword";
      response = request.call();
      assertEquals(404, response.getResponseCode());
   }
}
