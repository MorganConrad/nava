package com.flyingspaniel.nava.callback;

import com.flyingspaniel.nava.emit.Emit;

/**
 * Abstract implementation of a Callback, which also implements Emit.IListener.<br>
 * Subclasses need only implement callback().
 * <p>
 * Also contains a few special inner class implementations useful for unit tests
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */

public abstract class AbstractCallback<IN, OUT> implements Callback<IN, OUT>, Emit.IListener<IN> {

   protected Callback nextCallback = null;

   @Override
   /*
        NOTE: the first line of your implementation should be either <ul>
          <li>failFast()
          <li>failSlow()
          <li> or perhaps super.callback() </ul>
    */
   public abstract OUT callback(Exception ex, IN in, Object...more) throws Exception;

   @Override   // implement Emit.IListener
   public void handleEvent(IN arg0, Object...more) {
      try {
         callback(null, arg0, more);
      } catch (Exception e) {
         throw new CallbackAnd.Xception(this, e);
      }
   }

   @Override
   public void setNextCallback(Callback nextCallback) {
      this.nextCallback = nextCallback;
   }

   @Override
   public Callback getNextCallback() {
      return nextCallback;
   }


   /**
    * Override this if you want to do something to try to recover
    * @param ex   the Exception that was thrown by a previous callback
    * @param in   results from the previous callback, usually null and ignored
    * @throws Exception
    */
   protected void handleException(Exception ex, IN in, Object...more) throws Exception {
      throw ex;
   }


   /**
    * Fail Fast means that you want this completedCallback to handle the exception
    * @param ex   if null, nothing happens
    * @param in   results from the previous callback
    * @throws Exception
    */
   protected void failFast(Exception ex, IN in, Object...more) throws Exception {
      if (ex != null)
         handleException(ex, in, more);
   }

   /**
    * Fail Slow means that you will pass the Exception down to (perhaps the end) of the chain
    * This is handy if you have special Exception handling and can tack it on the end of all the callbacks.
    *
    * @param ex   if null, nothing happems
    * @param in   results from the previous callback
    * @throws Exception
    */
   protected void failSlow(Exception ex, IN in, Object...more) throws Exception {
      if (ex != null) {
         if (nextCallback != null)
            nextCallback.callback(ex, in, more);
         else
            handleException(ex, in, more);
      }
   }


   /**
    * Inner class mainly for tests, allows for canned results
    * @param <IN>
    * @param <OUT>
    */
   public static class Canned<IN, OUT> extends AbstractCallback<IN, OUT> {
      final Exception exToThrow;
      final OUT output;

      public Canned(Exception exToThrow, OUT output) {
         this.exToThrow = exToThrow;
         this.output = output;
      }

      @Override
      public OUT callback(Exception ex, IN in, Object...more) throws Exception {
         failSlow(ex, in, more);
         if (exToThrow != null)
            throw exToThrow;

         return output;
      }
   }


   /**
    * Class that does nothing (just calls failFast), then returns input unchanged
    * @param <IN>
    */
   public static class NOP<IN> extends AbstractCallback<IN, IN> {
      @Override
      public IN callback(Exception ex, IN in, Object...more) throws Exception {
         failFast(ex, in, more);
         return in;
      }
   }


}
