package com.flyingspaniel.nava.emit;

import java.util.Date;

import junit.framework.TestCase;


// also tests Broadcasters

public class EmitterTest extends TestCase {
   
   DateListener dateListener = new DateListener();
   StringListener stringListener1 = new StringListener();
   StringListener stringListener2 = new StringListener();
   StringListener stringListener3 = new StringListener();
   IntegerListener integerListener = new IntegerListener();
   NewListener newListener = new NewListener();
   RemoveListener removeListener = new RemoveListener();
   
   
   public void testEmitListenerList() {
      EmitListenerList<String> mString = new EmitListenerList<String>(true);
     
      mString.on(stringListener1);
      mString.removeListener(stringListener2);  // no effect, can't remove one that isn't there
      
      mString.addListener(stringListener2);     // these are all fine
      mString.addListener(stringListener2);     // since we are allowing duplicates
      assertEquals(3, mString.listenerCount());
      
      mString.removeListener(stringListener2);
       
      assertEquals(2,  mString.listenerCount());

      mString.emit("spam");
      
      assertEquals(1, stringListener1.count);
      assertEquals(1, stringListener2.count);
   }
   
   
   public void testEmitter() {
      Emitter emitter = new Emitter();
      emitter.addListener(Emit.NEW_LISTENER, newListener);
      emitter.addListener(Emit.REMOVE_LISTENER, removeListener);
      emitter.once("string", stringListener1);
      emitter.addListener("string", stringListener2).
          addListener("string", stringListener3);
      
      emitter.addListener("date", dateListener); 
      emitter.addListener("integer", integerListener);
      emitter.addListener("integer", integerListener);  // no effect, we aren't allowing duplicates
      
      assertEquals(7, emitter.listenerCount(null));
      assertEquals(3, Emit.listenerCount(emitter, "string"));
      
      emitter.fireEvent("string", "String Event #1");
      emitter.fireEvent("string", "String Event #2");
  //    emitter.fireEvent("date", new Date(0));
      emitter.fireEvent("integer",  1);
      emitter.fireEvent("integer",  2, 3, 4);
      emitter.fireEvent("bogus",  "Bogus Event");
      assertEquals(1, stringListener1.count);
      assertEquals(2, stringListener2.count);
      assertEquals(0, dateListener.count);   
      assertEquals(10, integerListener.sum);
      
      assertEquals(2, emitter.listeners("string").size());
      
      emitter.removeListener("string", stringListener2);
      emitter.fireEvent("string", "String Event #3");
      emitter.fireEvent("integer",  3);
      assertEquals(2, stringListener2.count);
      assertEquals(3, stringListener3.count);
      assertEquals(13, integerListener.sum);
      
      emitter.removeAllListeners("string");
      emitter.fireEvent("string", "String Event #4");
      emitter.fireEvent("integer",  4);
      assertEquals(3, stringListener3.count);
      assertEquals(17, integerListener.sum);
      
      emitter.removeAllListeners(null);
      emitter.fireEvent("integer",  5);
      assertEquals(17, integerListener.sum);
      
      assertEquals(7, newListener.count);
      assertEquals(2, removeListener.count);
      
   }
   
   public void testEmitterMax() {
      Emitter emitter = new Emitter();
      emitter.setMaxListeners(2);
      emitter.once("string", stringListener1);
      emitter.on("string", stringListener2);
      
      try {
         emitter.on("string", stringListener3);
         fail();
      }
      catch (Exception expected) {
         assertEquals("Exceeded maxListenener count of 2", expected.getMessage());
      }
      
      try {
         emitter.once("string", stringListener3);
         fail();
      }
      catch (Exception expected) {
         assertEquals("Exceeded maxListenener count of 2", expected.getMessage());
      }
      
      try {
         emitter.once(null, stringListener3);
         fail();
      }
      catch (Exception expected) {
         assertEquals("an eventID may not be null", expected.getMessage());
      }
      
   }
   
}


// some simple listeners for the unit test - they just count and print incoming events

class DateListener implements Emit.IListener<Date> {
   int count = 0;

   @Override
   public void handleEvent(Date date, Object...more) {
      count++;
      System.out.println(date);
   }  
}


class StringListener implements Emit.IListener<String> {
   int count = 0;
   
   @Override
   public void handleEvent(String event, Object...more) {
      count++;
      System.out.println(event);
   }  
}

class RemoveListener implements Emit.IListener<String> {
   int count = 0;
   
   @Override
   public void handleEvent(String event, Object...more) {
      count++;
      System.out.println("removed listener " + event);
   }  
}

class NewListener implements Emit.IListener<String> {
   int count = 0;
   
   @Override
   public void handleEvent(String event, Object...more) {
      count++;
      System.out.println("new listener " + event);
   }  
}

class IntegerListener implements Emit.IListener<Integer> {
   int count = 0;
   int sum = 0;
   
   @Override
   public void handleEvent(Integer event, Object...more) {
      count++;
      sum += event;
      for (Object o : more)
         sum += (Integer)o;
      System.out.println(event);
   }  
}
