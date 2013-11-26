package com.flyingspaniel.nava.emit;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * A semi-replacement for EventListenerList, inspired by node.js Emitters. <br>
 * This handles emitting events for one particular ID (e.g. "error") of listener.
 * <p>
 * Uses a CopyOnWriteArrayList.  Order of addition is preserved.  
 * You can generally NOT add a listener twice - see allowDuplicates
 * <p>
 * Normally clients don't use this directly, they use Emitter which fires multiple event types
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 *
 * @param <A0> the class of the first argument sent to the listener
 */

public class EmitListenerList<A0> implements Emit.IListenerList<A0> {
      
   // reasonable to assume that we will always have one listener
   // this contains both "on" listeners and "once" listeners
   protected Collection<Emit.IListener<A0>> listeners = createCollection();
   
   // but be a little frugal with these.  contains only the "once" listeners
   protected Collection<Emit.IListener<A0>> onces = null;

   protected boolean allowDuplicates;
   
   
   /**
    * Constructor
    * @param allowDuplicates
    */
   public EmitListenerList(boolean allowDuplicates) {
      this.allowDuplicates = allowDuplicates;
   }
   
   
   
   /**
    * Add a listener
    * 
    * @param listener  if null nothing happens.
    */
   @Override
   public void on(Emit.IListener<A0> listener) {
      if (listener != null) {
         boolean ok  = allowDuplicates;
         if (!ok) synchronized (this) {
            ok =  !listeners.contains(listener);
         }

         if (ok)
            listeners.add(listener);
      }
   }
   
   @Override 
   public void addListener(Emit.IListener<A0> listener) {
      on(listener);
   }
   
   /**
    * Add a listener that will be triggered only once
    * 
    * @param listener  if null nothing happens.
    */
   public synchronized void once(Emit.IListener<A0> listener) {
      if (listener != null) {
         
         addListener(listener);
         
         if (onces == null)
            onces = createCollection();
         
         if (allowDuplicates || !onces.contains(listener))
            onces.add(listener);
      }
   }



   /**
    * Remove the listener, from onces and listeners
    * 
    * @param listener  if null nothing happens.  They expect an <A0> as their first arg to the callback
    */
   public void removeListener(Emit.IListener<A0> listener) {
      if (listener != null)
         listeners.remove(listener);
   }
   
   
   /**
    * Remove all listeners
    */
   public synchronized void removeAllListeners() {
      listeners.clear();
      if (onces != null)
         onces.clear();
   }

   
   /**
    * Fires the event/object
    * 
    * @param arg0   hopefully an <A0> 
    * @param more   may be an empty array 
    */
   @Override
   public boolean emit(A0 arg0, Object...more) {
      boolean hasListeners = listenerCount() > 0;
      if (hasListeners) {
         for (Emit.IListener<A0> listener : listeners)
            listener.handleEvent(arg0, more);
         
         // do the removal in a synchronized block
         if (onces != null) synchronized (this) {
            listeners.removeAll(onces);
            onces.clear();
         }
      }
      
      return hasListeners;
   }

   
   
   /**
    * Current number of listeners
    */
   public int listenerCount() {
      return listeners.size();
   }
   
   
   /**
    * Returns the raw listeners, use with extreme caution
    */
   public Collection<Emit.IListener<A0>> listeners() {
      return listeners;
   }
   
   
   /**
    * This is a good implementation, but subclasses might want to override...
    * @return a thread-safe Collection
    */
   protected Collection<Emit.IListener<A0>> createCollection() {
      return new CopyOnWriteArrayList<Emit.IListener<A0>>();
   }

}
