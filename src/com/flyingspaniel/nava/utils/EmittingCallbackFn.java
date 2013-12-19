package com.flyingspaniel.nava.utils;

import com.flyingspaniel.nava.callback.Callback;
import com.flyingspaniel.nava.callback.CallbackAnd;
import com.flyingspaniel.nava.emit.Emit;
import com.flyingspaniel.nava.emit.Emitter;
import com.flyingspaniel.nava.fp.Fn;

/**
 * "Do It All" base class
 * Subclasses must override callback(), and may want to override others
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public abstract class EmittingCallbackFn<IN,OUT> extends Emitter implements Callback<IN,OUT>, Fn<IN,OUT>, Emit.IListener<IN> {

   protected Callback nextCallback = null;

   protected EmittingCallbackFn() {}

   @Override  // subclasses must implement this
   abstract public OUT callback(Exception ex, IN in, Object... more) throws Exception;

   @Override public void setNextCallback(Callback nextCallback) {
      this.nextCallback = nextCallback;
   }

   @Override public Callback getNextCallback() {
      return nextCallback;
   }

   @Override public OUT fn1(IN in) {
      try {
         return callback(null, in);
      } catch (Exception e) {
         throw new CallbackAnd.Xception(this, e);
      }
   }

   @Override public OUT fn2(IN in1, IN in2ignored) {
      try {
         return callback(null, in1);
      } catch (Exception e) {
         throw new CallbackAnd.Xception(this,e);
      }
   }

   @Override public void handleEvent(IN arg0, Object... more) {
      try {
         callback(null, arg0, more);
      } catch (Exception e) {
         throw new CallbackAnd.Xception(this, e);
      }
   }

   /**
    * failFast means handle any Exception immediately.  By default (see handleException) we throw it
    */
   protected void failFast(Exception ex, IN in, Object...more) throws Exception {
      if (ex != null)
         handleException(ex, in, more);
   }

   /**
    * failSlow means we let the nextCallback, if any, handle the Exception
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
    * Subclasses may want to override to log, etc...  (may be a good spot for D.I.)
    */
   protected void handleException(Exception ex, IN in, Object...more) throws Exception {
      throw ex;
   }


   // example inner class
   public static class NOP<IN> extends EmittingCallbackFn<IN, IN> {
      @Override
      public IN callback(Exception ex, IN in, Object...more) throws Exception {
         failFast(ex, in, more);
         return in;
      }
   }

}
