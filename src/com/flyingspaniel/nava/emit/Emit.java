package com.flyingspaniel.nava.emit;

import java.util.Collection;


/**
 * Interfaces and utilities for node.js style event handling stuff, a simplified, more generic event handling
 * The "events" are passed as an arg0, followed by a possibly empty varags Object...
 * 
 * Warning: The generics <A0> are mainly for documentation and decoration.  
 * Internally there are unchecked casts and if you screw up you will get ClassCastExceptions
 * 
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://nodejs.org/api/events.html#events_events>node.js Events</a>
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 *
 */
public class Emit {
   
   public static final String NEW_LISTENER = "newListener";
   public static final String REMOVE_LISTENER = "removeListener";

   
   // handy constant if there are no more argments after arg0
   public static final Object[] NO_MORE = new Object[0];
   
   /**
    * To mimic node.js EventEmitter, this is static.  
    * Note that it is also an instance method of IEmitter
    * @param  emitter
    * @param  event
    * @return count
    */
   public static int listenerCount(IEmitter emitter, Object event) {
      return emitter.listeners(event).size();
   }
   
   /**
    * Client Interface to listen to "events".
    * For documentation, you get to declare in <A0> what you expect the first argument to be.
    * However, internally, there are unchecked casts so this is not assured.
    * 
    * @author Morgan Conrad
    * Copyright(c) 2013  Morgan Conrad
    *
    * @param <A0>  the type of the first argument you expect to receive in handleEvent()
    */
   public interface IListener<A0> {
      
      
      /**
       * Receive and handle an "event", represented by arg0 and possibly more.
       * The details of what gets sent depend on {@link IEmitter#emit(Object, Object, Object...) IEmitter.emit()}
       * 
       * @param arg0  hopefully of type <A0>
       * @param more  often empty 
       */
      public void handleEvent(A0 arg0, Object...more);
   }
   

   
   /**
    * Interface for objects that can emit/broadcast "events" of various eventIDs
    * <p>
    * In node.js the eventIDs are Strings, e.g 'error'.  
    * We allow anything.  Strings are probably simplest, but Classes and Enums also make sense.
    * 
    * @author Morgan Conrad
    * Copyright(c) 2013  Morgan Conrad
    *
    */
   public interface IEmitter {
      
      /**
       * Add a listener for the specific eventID
       * @param  eventID   non-null
       * @param  listener  if null nothing happens
       * @return this (for chaining)
       */
      public <A0> Emitter addListener(Object eventID, IListener<A0> listener);
      
      
      /**
       * Syntactic sugar, same as addListener()
       * @param eventID   non-null
       * @param listener
       * @return this (for chaining)
       */
      public <A0> Emitter on(Object eventID, IListener<A0> listener);
      
      
      /**
       * Add a listener that will only receive one event, then get disconnected
       * @param  eventID   non-null
       * @param  listener  if null nothing happens
       * @return this (for chaining)
       */
      public <A0> Emitter once(Object eventID, IListener<A0> listener);
      
      /**
       * Remove a listener for the specific eventID
       * @param eventID     non-null
       * @param listener    if null nothing happens
       * @return  this (for chaining)
       */
      public <A0> Emitter removeListener(Object eventID, IListener<A0> listener);
      
      
      /**
       * Remove all listeners for the specified eventID
       * @param eventID  if null, all listeners of all eventIDs are removed
       * @return   this (for chaining)
       */
      public <A0> Emitter removeAllListeners(Object eventID);
      
      /**
       * Fire the event to all listeners for that eventID
       *
       * @param  eventID non-null
       * @param  arg0    1st arg to listener
       * @param  more    more args
       * @return true    if event had listeners
       */
      public <A0> boolean emit(Object eventID, A0 arg0, Object...more);
      
      
      /**
       * Convenience method, fire the event to all listeners for that eventID, with more = empty array
       *
       * @param  eventID non-null
       * @param  arg0    1st arg to listener
       * @return true    if event had listeners
       */
      public <A0> boolean emit(Object eventID, A0 arg0);
      
      /**
       * Same as emit, convenience method
       * @param eventID
       * @param arg0
       * @param more
       * @return true if event had listeners
       */
      public <A0> boolean fireEvent(Object eventID, A0 arg0, Object...more);

      
      /**
       * Return a shallow copy of listeners for a given eventID
       * @param eventID    if null, collects ALL listeners
       * @return  Collection, non-null, may be empty if there are none
       */
      public <A0> Collection<IListener<A0>> listeners(Object eventID);
  
      
      /**
       * Counts all listeners for a given event  (same as listeners(eventID).size())
       * @param eventID  if null, counts ALL listeners
       * @return integer count
       */
      public int listenerCount(Object eventID);
  
      
      /**
       * Sets the maximum listeners for each eventID.  Default = 10
       * @param max   0 means unlimited.  Use -1 to allow no listeners at all
       * @return      this
       */
      public Emitter setMaxListeners(int max);
   }
   
   
   /**
    * Interface for emitting only a single eventID of event.  Typically not-used.
    * Put here in case you want to override internals of Emitter.
    * 
    * @author Morgan Conrad
    * @since Copyright(c) 2013  Morgan Conrad
    *
    * @see <a href="http://www.gnu.org/copyleft/lesser.html">This software is released under the LGPL</a>
    *
    */
   public interface IListenerList<A0> {
      public boolean emit(A0 arg0, Object...more);
      public int listenerCount();
      public Collection<IListener<A0>> listeners();
      public void on(IListener<A0> listener);
      public void once(IListener<A0> listener);
      public void addListener(IListener<A0> listener);
      public void removeListener(IListener<A0> listener);
      public void removeAllListeners();
      
   }
}

