package com.flyingspaniel.nava.callback;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class to execute callbacks, and manage their links, using two ExecutorServices
 * <p>
 * callbackExecutorService  is where the callbacks get run.<br>
 * handlerExecutorService   listens to results from callbackExecutorService (using completionService)<br>
 *                          and, when needed, submits any followup callbacks to callbackExecutorService<p>
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class CallbackExecutor implements Runnable {

   protected final ExecutorService callbackExecutorService;
   protected final CompletionService<CallbackAnd.Input> completionService;
   protected final ExecutorService handlerExecutorService;

   // mainly for unit tests
   public final AtomicInteger exceptionCount = new AtomicInteger(0);
   public final AtomicReference<Object> lastResultOrException = new AtomicReference<Object>();


   /**
    * Constructor
    * @param callbackThreads  should generally be >= handlerThreads
    * @param handlerThreads   must be > 0
    */
   public CallbackExecutor(int callbackThreads, int handlerThreads) {

      handlerExecutorService = Executors.newFixedThreadPool(handlerThreads);

      callbackExecutorService = new MyThreadPoolExecutor(callbackThreads, handlerExecutorService);
      completionService = new ExecutorCompletionService<CallbackAnd.Input>(callbackExecutorService);

      for (int i=0; i<handlerThreads; i++)
         handlerExecutorService.submit(this);
   }


   /**
    * Submit a callback (generally the first of a chain)
    * @param first   should be non-null
    * @param inData  passed to first callback
    */
   public void submitCallback(Callback first, Object inData, Object...more) {
      if (handlerExecutorService.isTerminated())
         throw new RejectedExecutionException("Handler isTerminated");

      CallbackAnd.Input caInput = new CallbackAnd.Input(first, null, inData, more);
      completionService.submit(caInput);
   }


   /**
    * Mainly for unit tests, but performs an orderly shutdown
    *
    * @param waitMS  milliseconds, if > 0, will put in three waits along the way
    * @throws InterruptedException
    */
   public void shutdown(long waitMS) throws InterruptedException {
      if (waitMS > 0)
          Thread.sleep(waitMS);
      callbackExecutorService.shutdown();
      if (waitMS > 0) {
         callbackExecutorService.awaitTermination(waitMS, TimeUnit.MILLISECONDS);
         if (callbackExecutorService.isTerminated())
            handlerExecutorService.awaitTermination(waitMS, TimeUnit.MILLISECONDS);
      }
   }



   @Override
   @SuppressWarnings("unchecked")
   public void run() {

      try { // outer try
         while (!callbackExecutorService.isTerminated())  {

            Future future = completionService.take();

            try { // inner try

               // Get the output, handling CallbackAnd.Xceptions
               CallbackAnd.Output caOutput = null;
               try {
                  caOutput = CallbackAnd.Output.fromFuture(future);
               }
               catch (CallbackAnd.Xception cax) {
                  Exception urException = cax.getException();
                  lastResultOrException.set(urException);

                  Callback next =  cax.callbackThatThrew.getNextCallback();
                  if (next == null)  // nobody to handle it
                     throw urException;
                  else
                     next.callback(urException, null);
               }

               // special check to spinoff multiple callbacks
               if (caOutput.completedCallback instanceof Callback.ProducesMultiple) {
                  submitMultiple(caOutput);
               }

               // normal operation is here.  We have a legit result to pass to the next callback
               else {
                  CallbackAnd.Input next = CallbackAnd.Input.fromOutput(caOutput);
                  if (next != null)
                     completionService.submit(next);
                  else // mainly for unit tests
                     lastResultOrException.set(caOutput.output);
               }
            } // end of inner try

            catch (InterruptedException ie) {
               throw ie; // exit to outer try and stop running
            }
            catch (RejectedExecutionException ree) { // if callbackExecutorService has shutdown
               throw ree;  // exit to outer try and stop running
            }

            // all other Exceptions get handled here and do not exit the loop
            catch (Exception urException) {
               handleNormalCallbackException(urException);
            }

         } // end of while()
      } // end of outer try

      catch (InterruptedException ie) {
         ; // TODO log normal termination
      }
      catch (RejectedExecutionException ree) {
         ; // TODO Log a rocky termination
      }
      catch (Error e) {
         throw e;   // TODO option to log extremely abnormal termination
      }
      finally {
         callbackExecutorService.shutdown();
      }

   }


   protected void submitMultiple(CallbackAnd.Output cao) {
      Object[] inputs = ((Callback.ProducesMultiple)cao.completedCallback).getMultiple();
      Callback next = cao.completedCallback.getNextCallback();
      if (next == null)  {   // that was end of the line   TODO throw something???
         lastResultOrException.set(inputs);
      }

      else for (Object in : inputs) {
         Callable c = new CallbackAnd.Input(next, null, in);
         completionService.submit(c);
      }
   }


   /**
    * Handle a normal callback exception.  Subclasses should override to provide logging etc...
    * @param urException the original exception (not wrapped in any of our constructs)
    */
   protected void handleNormalCallbackException(Exception urException) {
      exceptionCount.incrementAndGet();
      urException.printStackTrace();
   }


   /**
    *  ThreadPoolExecutor for the callbacks that, when shutdown,
    *  then shutdownNows the handlerService
    */
   static class MyThreadPoolExecutor extends ThreadPoolExecutor {

      final ExecutorService shutdownNext;

      MyThreadPoolExecutor(int nThreads, ExecutorService shutdownNext) {
         super(nThreads, nThreads,
               0L, TimeUnit.MILLISECONDS,
               new LinkedBlockingQueue<Runnable>());

         this.shutdownNext = shutdownNext;
      }

      @Override
      protected void terminated() {
          if (shutdownNext != null)
             shutdownNext.shutdownNow();
      }
   }
}
