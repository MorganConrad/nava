package com.flyingspaniel.nava.net;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class JRFC2617Test extends TestCase {

   static final String WIKIPEDIA_CHALLENGE =  "Digest realm=\"testrealm@host.com\", qop=\"auth,auth-int\", " +
         "nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

   static final String WIKIPEDIA_RESPONSE = "username=\"Mufasa\", realm=\"testrealm@host.com\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," +
         " uri=\"/dir/index.html\", qop=auth, nc=00000001, cnonce=\"0a4f113b\", response=\"6629fae49393a05397450978507c4ef1\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"";

   static final String HTTPBIN_CHALLENGE =  "Digest realm=\"me@kennethreitz.com\", qop=auth, " +
         "nonce=\"c61c960886c3466d7911e42f3f29e64a\", opaque=\"6a18eb7a32e96a9dc59b05e2d13228d8\"";

   static final String HTTPBIN_RESPONSE = "username=\"user\", realm=\"me@kennethreitz.com\", nonce=\"c61c960886c3466d7911e42f3f29e64a\", uri=\"/digest-auth/auth/user/passwd\"," +
         " qop=auth, nc=00000001, cnonce=\"75743107ae1779d0\", response=\"caa5a9103aa50c781407bea4ff5493d7\", opaque=\"6a18eb7a32e96a9dc59b05e2d13228d8\"";

   static final String HTTPBIN1 = "http://httpbin.org/digest-auth/auth/user/passwd";
   static final String WEBDAV = "http://test.webdav.org/auth-digest/";


   public void testWIKIPEDIA() throws Exception {
      JRFC2617 jRFC2617 = new JRFC2617(WIKIPEDIA_CHALLENGE);
      String response = jRFC2617.createResponse("Mufasa", "Circle Of Life", "0a4f113b", "GET", "/dir/index.html");
      assertEquals(WIKIPEDIA_RESPONSE, response);
   }

   public void testExceptions() throws Exception {
      JRFC2617 jRFC2617;

      try {
         jRFC2617 = new JRFC2617("not a real challenge");
         fail();
      }
      catch (IllegalStateException expected) { ; }

      String wiki =  WIKIPEDIA_CHALLENGE;
      wiki = wiki.replace("auth,auth-int", "fakeqop");
      try {
         jRFC2617 = new JRFC2617(wiki);
         fail();
      }
      catch (UnsupportedOperationException expected) { ; }

      wiki =  WIKIPEDIA_CHALLENGE + ", algorithm=foobar";
      try {
         jRFC2617 = new JRFC2617(wiki);
         fail();
      }
      catch (IllegalArgumentException expected) { ; }


   }

   public void testCannedHTTPBin() throws Exception {
      JRFC2617 jRFC2617 = new JRFC2617(HTTPBIN_CHALLENGE);
      String response = jRFC2617.createResponse("user", "passwd", "75743107ae1779d0", "GET", "/digest-auth/auth/user/passwd");
      assertEquals(HTTPBIN_RESPONSE, response);
   }


   // Never got this to work, so "commented out" va naming conventions
   public void doNotRuntestRealHTTPBin() throws Exception {
      InputStream is = null;
      HttpURLConnection conn = createConnection(HTTPBIN1);
      conn.connect();
      try {
         is = conn.getInputStream();
      }
      catch (IOException ioe) {
         ioe.printStackTrace();
      }
      conn.disconnect();

      JRFC2617 jRFC2617 = new JRFC2617(conn);
      conn = jRFC2617.createRetryConnection("user", "passwd");
      conn.connect();
      is = conn.getInputStream();

      byte[] buf = new byte[256];
      is.read(buf);
      String sss = new String(buf);
      System.out.println(sss);
   }

   // Never got this to work, so "commented out" va naming conventions
   public void doNotRuntestRealwebdav() throws Exception {
      InputStream is = null;
      HttpURLConnection conn = createConnection(WEBDAV);
      conn.connect();
      try {
         is = conn.getInputStream();
      }
      catch (IOException ioe) {
         ioe.printStackTrace();
      }
      conn.disconnect();

      JRFC2617 jRFC2617 = new JRFC2617(conn);
      conn = jRFC2617.createRetryConnection("user", "passwd");
      conn.connect();
      is = conn.getInputStream();

      byte[] buf = new byte[256];
      is.read(buf);
      String sss = new String(buf);
      System.out.println(sss);
   }


   private HttpURLConnection createConnection(String url) throws Exception {
      HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection(Proxy.NO_PROXY);
      conn.setUseCaches(false);
      conn.setInstanceFollowRedirects(true);

      return conn;
   }
}
