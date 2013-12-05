package com.flyingspaniel.nava.callback;

import java.util.concurrent.*;

/**
 * Utilities for dealing with Callbacks
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class Callbacks {

   private Callbacks() {}  // utility class

   static final Object[] EMPTY = new Object[0];

   /**
    * Create a chain of Callbacks, by setting nextCallback along the line...
    * @param first
    * @param others
    * @return first
    */
   public static Callback chainUp(Callback first, Callback...others) {
      Callback head = first;
      for (Callback c : others) {
         head.setNextCallback(c);
         head = c;
      }
      // terminate the end
      head.setNextCallback(null);

      return first;
   }


   /**
    * Runs a callback (and any associated chain) synchronously
    * @param callback  first callback, if null, data will be returned immediately
    * @param data      input to first callback
    * @return          result from the final callback
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   public static Object runSync(Callback callback, Object data, Object...more) throws Exception {
      Exception ex = null;

      while (callback != null) {

         if (callback instanceof Callback.ProducesMultiple)
            throw new IllegalStateException("Callbacks.runSync() cannot handle a Callback.ProducesMultiple");

         try {
            data = callback.callback(ex, data, more);
         } catch (Exception e) {
            ex = e;
         }

         more = getMore(callback);
         callback = callback.getNextCallback();
      }

      if (ex != null)
         throw ex;

      return data;
   }


   /**
    * Wraps runSync() in a Callable so it can be run in another thread
    *
    * @param first   if null, returns inData
    * @param inData  inputs to the first callback
    * @param more    inputs to the first callback
    * @return        result from the final callback
    */
   public static Callable callableRunSync(final Callback first, final Object inData, final Object...more) {
      return new Callable() {
         @Override
         public Object call() throws Exception {
            return runSync(first, inData, more);
         }
      };
   }


   /**
    * Runs a completedCallback (and any associated chain) using an ExecutorService
    * However, since we wait for the Futures "in line" here, no real speed gain (see runASyncInLine)
    *
    * @param xs       if null, they get run sync
    * @param callback if null, returns inData
    * @param data     inputs to the first callback
    * @param more     inputs to the first callback
    * @return         result from the final callback
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   public static Object runASyncInLine(ExecutorService xs, Callback callback,
                                       Object data, Object...more) throws Exception {
      Exception ex = null;

      if (xs == null)
         return runSync(callback, data);

      while (callback != null) {

         if (callback instanceof Callback.ProducesMultiple)  // TODO possibly this could be handled
            throw new IllegalStateException("Callbacks.runSync() cannot handle a Callback.ProducesMultiple");

         Callable callable = callableCallback(callback, ex, data, more);
         Future future = xs.submit(callable);
         try {
            data = future.get();
         } catch (ExecutionException e) {
            ex = getCause(e);
         }
         more = getMore(callback);
         callback = callback.getNextCallback();
      }

      if (ex != null)  // nobody handled it...
         throw ex;

      return data;
   }

   /**
    * By wrapping runASyncInLine() in a Callable, we can now run *everything* off the original thread.
    * However, see CallbackExecutor for a better alternative
    *
    * @param xs
    * @param first
    * @param inData
    * @return Callable
    * @see CallbackExecutor  for a better alternative
    */
   public static Callable callableRunASync(final ExecutorService xs, final Callback first,
                                           final Object inData, final Object...more) {
      return new Callable() {
         @Override
         public Object call() throws Exception {
            return runASyncInLine(xs, first, inData, more);
         }
      };
   }


   /**
    * Convert any Callback into a Callable
    * @param callback  if null this returns null
    * @param ex        input to the Callback
    * @param inData    input to the Callback
    * @return          a Callable, or null
    */
   public static <IN,OUT> Callable callableCallback(final Callback<IN, OUT> callback, final Exception ex,
                                                    final IN inData, final Object...more) {
      if (callback == null)
         return null;
      return new Callable() {
         public OUT call() throws Exception {
            return callback.callback(ex, inData, more);
         }
      } ;
   }


   /**
    * Utility to get the more results (or an empty []) from a callback
    *
    * @param  callback non-null
    * @return returns [] if callback is not a Callback.ProducesMore
    */
   public static Object[] getMore(Callback callback) {
      if (callback instanceof Callback.ProducesMore)
         return ((Callback.ProducesMore)callback).getMore();

      return EMPTY;
   }



   /**
    * Utility to get the cause from an ExecutionException as an Exception, not a Throwable
    * @param ee      may be null
    * @return        null if ee is null
    * @throws Error  if ee wrapped an Error
    */
   public static Exception getCause(ExecutionException ee) {
      if (ee == null)
         return null;
      Throwable t = ee.getCause();
      if (t instanceof Error)
         throw (Error)t;
      return (Exception)t;
   }
}
