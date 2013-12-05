package com.flyingspaniel.nava.callback;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class CallbacksTest extends TestCase {

   static final int LEN = 1453;
   static final File FILE = new File("test/data/Gettysburg.txt");

//   static final Reader READER = new Reader();
//   static final Counter COUNTER = new Counter();

   public void testGetSyncCallable() throws Exception {
      Reader READER = new Reader();
      Counter COUNTER = new Counter();
      Callbacks.chainUp(READER, COUNTER);
      Object foo = Callbacks.callableRunSync(READER, FILE).call();
      assertEquals(LEN, ((Integer) foo).intValue());
   }

   public void testRunASyncInLine() throws Exception {
      Reader READER = new Reader();
      Counter COUNTER = new Counter();
      ExecutorService es = Executors.newSingleThreadExecutor();
      Callbacks.chainUp(READER, COUNTER);
      Object len = Callbacks.runASyncInLine(es, READER, FILE);
      assertEquals(LEN, ((Integer) len).intValue());
   }

   public void testASync() throws Exception {
      Reader READER = new Reader();
      Counter COUNTER = new Counter();
      ExecutorService es = Executors.newFixedThreadPool(2);
      Callbacks.chainUp(READER, COUNTER);
      Callable c =  Callbacks.callableRunASync(es, READER, FILE);
      Future<Integer> future = es.submit(c);
      assertEquals(LEN, future.get().intValue());
   }

   public void testCallbackExecutor() throws Exception {
      Reader READER = new Reader();
      Counter COUNTER = new Counter();
      Callbacks.chainUp(READER, COUNTER);
      CallbackExecutor cex = new CallbackExecutor(2,1);
      cex.submitCallback(READER, FILE);
      cex.shutdown(2000L);
      Object len = cex.lastResultOrException.get();
      //Object len = f.get();
      assertEquals(LEN, ((Integer)len).intValue());
   }

   public void testSyncFails() throws Exception {
      Reader READER = new Reader();
      Counter COUNTER = new Counter();
      Throws THROWS = new Throws();
      Callbacks.chainUp(COUNTER, THROWS);
      try {
         Callbacks.runSync(COUNTER, "this is a string");
         fail();
      }
      catch (Exception expected) {
        assertEquals("THROWS", expected.getMessage());
      }

      Callbacks.chainUp(THROWS, COUNTER);
      try {
         Callbacks.runSync(THROWS, "this is a string");
         fail();
      }
      catch (Exception expected) {
         assertEquals("THROWS", expected.getMessage());
      }

   }

   public void testCallbackExecutorFails() throws Exception {
      Counter COUNTER = new Counter();
      Throws THROWS = new Throws();
      Callbacks.chainUp(COUNTER, THROWS);
      CallbackExecutor cex = new CallbackExecutor(2,2);
      cex.submitCallback(COUNTER, "this is a string");

      COUNTER = new Counter();
      THROWS = new Throws();
      Callbacks.chainUp(THROWS, COUNTER);
      cex.submitCallback(THROWS, "this is a string");
      cex.shutdown(2000L);
      assertEquals(2, cex.exceptionCount.intValue());
      assertEquals("THROWS", ((Exception)cex.lastResultOrException.get()).getMessage());
   }


   public void testMultiple() throws Exception {
      Reader4 READER = new Reader4();
      Counter COUNTER = new Counter();
      Callbacks.chainUp(READER, COUNTER);
      CallbackExecutor cex = new CallbackExecutor(2,2);
      cex.submitCallback(READER, FILE);
      cex.shutdown(2000L);
      Integer len = (Integer)cex.lastResultOrException.get();
      assertEquals(4*LEN, len.intValue());  // TODO sometimes this is flaky

      try {
         Callbacks.runSync(READER, FILE);
         fail();
      } catch (IllegalStateException expected) {}
   }

   static class Reader extends AbstractCallback<File, String> {

      @Override
      public String callback(Exception ex, File v, Object...more) throws Exception {
         FileReader reader = new FileReader(v);
         StringBuilder sb = new StringBuilder();

         char[] buf = new char[1024];
         int len;

         while ((len = reader.read(buf, 0, buf.length)) >= 0) {
            sb.append(buf, 0, len);
         }
         reader.close();
         return sb.toString();
      }
   }


   static class Reader4 extends Reader implements Callback.ProducesMultiple<File, String> {
      String[] multiples;
      @Override  public String[] getMultiple() {
         return multiples;
      }

      @Override
      public String callback(Exception ex, File v, Object...more) throws Exception {
         String r1 = super.callback(ex, v, more);
         multiples = new String[] { r1, r1, r1, r1 };
         return r1;
      }
   }

   static class Counter extends AbstractCallback<String, Integer> {
      int length = 0;

      @Override
      public Integer callback(Exception ex, String v, Object...more) throws Exception {
         failSlow(ex, v, more);
         length += v.length();
         return length;
      }
   }

   static class Throws extends AbstractCallback {
      @Override
      public Object callback(Exception ex, Object in, Object...more) throws Exception {
         throw new Exception("THROWS");
      }
   };


}