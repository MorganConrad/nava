package com.flyingspaniel.nava.callback;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Immutable Utility classes/data-structures to group a Callback with associated inputs, outputs, or exceptions<br>
 * Input and Output are package visible, and are used internally by Callbacks and/or CallbackExecutor
 * <p>
 * Xception might be used by an end user so is public
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class CallbackAnd {

   private CallbackAnd() {}  // all we have are nested inner classes
   
   /**
    * Represents a not-yet-run Callback and it's inputs
    */
   static class Input<IN, OUT> implements Callable<CallbackAnd.Output<OUT>> {

      final Callback<IN, OUT> callback; // to be run
      final Exception ex;
      final IN input;
      final Object[] more;

      Input(Callback<IN, OUT> nextCallback, Exception ex, IN in, Object...more) {
         this.callback = nextCallback;
         this.ex = ex;
         this.input = in;
         this.more = more;
      }


      /**
       * Create inputs from the previous output in the chain
       * @param  output   wrapped results from a previous callback.  may be null
       * @return null     if at the end of the chain
       */
      static Input fromOutput(CallbackAnd.Output output) {
         if ((output != null) && (output.completedCallback.getNextCallback() != null))
            return new Input(output.completedCallback.getNextCallback(), null, output.output, output.more);
         else
            return null;
      }

      static Input fromXception(CallbackAnd.Xception xception) {
         return new Input(xception.callbackThatThrew.getNextCallback(), xception.getException(), null);
      }

      /**
       * Wraps ourself in a Callable that will return a CallbackAnd.Output
       * @return Callable that will produce a CallbackAnd.Output
       * @throws InterruptedException
       * @throws CallbackAnd.Xception
       */
      @Override
      public CallbackAnd.Output<OUT> call() throws InterruptedException, CallbackAnd.Xception {
         try {
            OUT output = callback.callback(ex, input, more);
            Object more = Callbacks.getMore(callback);
            return new CallbackAnd.Output<OUT>(callback, output, more);
         }
         catch (InterruptedException ie) {
             throw ie;
         }
         catch (Exception e) {
            throw new CallbackAnd.Xception(callback, e);
         }
      }
   }


   /**
    * Represents a completed Callback and it's output
    */
   static class Output<OUT> {

      final Callback completedCallback;
      final OUT output;
      final Object[] more;

      Output(Callback completedCallback, OUT output, Object...more) {
         this.completedCallback = completedCallback;
         this.output = output;
         this.more = more;
      }

      /**
       * Takes the data from a Future (probably created via CallbackAnd.Input.call())
       * @param future  non-null
       * @return CallbackAnd.Output
       * @throws InterruptedException
       * @throws CallbackAnd.Xception if callback threw an exception
       */
      public static Output fromFuture(Future<CallbackAnd.Output> future) throws InterruptedException {
         try {
            return future.get();
         } catch (ExecutionException e) {
            Exception ur = Callbacks.getCause(e);
            if (ur instanceof Xception)
               throw (Xception)ur;
            else  // should not happen
               throw new IllegalStateException(ur);
         }

      }

   }


   /**
    * Represents an Exception thrown by a Callback
    * Unlike the two others this is public cause users might want to catch it
    */
   public static class Xception extends RuntimeException {
      final Callback callbackThatThrew;

      public Xception(Callback callbackThatThrew, Throwable t) {
         super(t);
         this.callbackThatThrew = callbackThatThrew;
      }

      /**
       * Convenience, returns the wrapped Throwable as an Exception
       * @return Exception  what we wrapped.  Could be null if we wrapped a null. (highly unusual)
       * @throws Error      if that's what we wrapped
       */
      public Exception getException() {
         Throwable t = getCause();
         if (t instanceof Error)
            throw (Error) t;
         return (Exception) t;
      }
   }



}
