package com.flyingspaniel.nava.emit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * An implementation of a node.js-style Emitter, composed of a Map of Emit.IListenerList
 * <p>
 * Warning: The generics <A0> (for "arg0") indicate what the listener expects as the first
 * argument to Emit.IListener.handleEvent(arg0, more...), and are primarily for documentation and decoration.
 * Internally there are unchecked casts and if you screw up you will get runtime ClassCastExceptions
 *
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 */

@SuppressWarnings({"rawtypes", "unchecked"})
public class Emitter implements Emit.IEmitter {

   protected int maxListeners = 10;

   final protected Map<Object, Emit.IListenerList> idMap;

   // default is false
   final protected boolean allowDuplicates;



   protected Emitter(boolean allowDuplicates, Map map) {
      this.allowDuplicates = allowDuplicates;
      idMap = map;
   }

   /**
    * Constructor
    * @param allowDuplicates  if true, you can add the same listener twice
    */
   public Emitter(boolean allowDuplicates) {
      this(allowDuplicates, new ConcurrentHashMap());
   }
   /**
    * Default Constructor with allowDuplicates = false
    */
   public Emitter() {
      this(false);
   }


   /**
    * Add a listener for the specific eventID
    * @param  eventID   non-null
    * @param  listener  if null nothing happens
    * @param  <A0>      what listener expects as arg0
    * @return this
    */
   @Override
   public <A0> Emitter on(Object eventID, Emit.IListener<A0> listener) {
      if (listener != null) {
         emit(Emit.NEW_LISTENER, eventID);  // note: fired before adding
         Emit.IListenerList<A0> listenersForID = getListenerList(eventID, true);
         if (listenersForID.listenerCount() >= maxListeners)
               throw new IllegalStateException("Exceeded maxListenener count of " + maxListeners);
         listenersForID.on(listener);
      }

      return this;
   }


   @Override
   public <A0> Emitter addListener(Object eventID, Emit.IListener<A0> listener) {
      return on(eventID, listener);
   }


   /**
    * Add a one-time listener for the specific eventID
    *
    * @param  eventID   non-null
    * @param  listener  if null nothing happens
    * @param  <A0>      what listener expects as arg0
    * @return this
    */
   public <A0> Emitter once(Object eventID, Emit.IListener<A0> listener) {
      if (listener != null) {
         emit(Emit.NEW_LISTENER, eventID);  // note: fired before adding
         Emit.IListenerList<A0> listenersForID = getListenerList(eventID, true);
         if (listenersForID.listenerCount() >= maxListeners)
               throw new IllegalStateException("Exceeded maxListenener count of " + maxListeners);
         listenersForID.once(listener);
      }

      return this;
   }



   /**
    * Removes the listener for the eventID.
    * If that eventID booleanOr listener is not present, nothing happens
    *
    * @param eventID    non-null
    * @param listener   to be removed (if null nothing happens)
    * @param  <A0>      what listener expected as arg0
    * @return           this
    */
   @Override
   public <A0> Emitter removeListener(Object eventID, Emit.IListener<A0> listener) {
      if (listener != null) {
         Emit.IListenerList<A0> listenersForID = getListenerList(eventID, false);
         if (listenersForID != null)
            listenersForID.removeListener(listener);
         emit(Emit.REMOVE_LISTENER, eventID);  // note: fired after removing
      }

      return this;
   }

   /**
    * Remove all listeners for the specified eventID
    *
    * @param eventID  if null, all listeners of all eventIDs are removed
    * @return         this
    */
   @Override
   public synchronized Emitter removeAllListeners(Object eventID) {
      if (eventID == null) {
         idMap.clear();
         // no point in firing a REMOVE_LISTENER
      }
      else {
         idMap.remove(eventID);
         emit(Emit.REMOVE_LISTENER, eventID);  // note: fired after removing
      }

      return this;
   }


   /**
    * Here's one of the main functions - Fire an event
    *
    * @param eventID   typically a String
    * @param arg0      1st arg to listener
    * @param more      additional varags
    * @param  <A0>     what listener expects as arg0
    * @return          if eventID had listeners
    */
   @Override
   public <A0> boolean emit(Object eventID, A0 arg0, Object...more) {
      Emit.IListenerList<A0> e1 = getListenerList(eventID, false);
      return (e1 != null) ? e1.emit(arg0, more) : false;
   }


   /**
    * Convenience method, fire the event to all listeners for that eventID, with more = empty array
    *
    * @param  eventID non-null
    * @param  arg0    1st arg to listener
    * @return true    if eventID had listeners
    */
   @Override
   public <A0> boolean emit(Object eventID, A0 arg0) {
      return emit(eventID, arg0, Emit.NO_MORE);
   }

   @Override
   public <A0> boolean fireEvent(Object eventID, A0 arg0, Object...more) {
      return emit(eventID, arg0, more);
   }


   /**
    * Return a shallow copy of listeners for a given eventID
    *
    * @param eventID    if null, collects ALL listeners
    * @param  <A0>      what listener expects as arg0
    * @return  Collection, non-null, may be empty if there are none
    */
   @Override
   public <A0> Collection<Emit.IListener<A0>> listeners(Object eventID) {
      if (eventID == null) {
         ArrayList<Emit.IListener<A0>> all = new ArrayList<Emit.IListener<A0>>();
         if (idMap != null)
            for (Emit.IListenerList e1 : idMap.values())
               all.addAll(e1.listeners());

         return all;
      }

      Emit.IListenerList<A0> listenersForID = getListenerList(eventID, false);
      if (listenersForID != null)
         return new ArrayList<Emit.IListener<A0>>(listenersForID.listeners());
      else
         return Collections.emptyList();
   }


   @Override
   public int listenerCount(Object eventID) {
       return listeners(eventID).size();
   }


   @Override
   public Emitter setMaxListeners(int count) {
      maxListeners = (count == 0) ? count = Integer.MAX_VALUE : count;

      return this;
   }



   /**
    * This is a good implementation, but subclasses might want to override...
    * @return Emit.IListenerList
    */
   protected <A0> Emit.IListenerList<A0> createListenerList() {
      return new EmitListenerList(allowDuplicates);
   }


   /**
    * Get the ListenerList for the given eventID
    *
    * @param eventID        non-null
    * @param forceCreation  if true, forces creation of classMap and the IListenerList
    * @param  <A0>          what listener expects as arg0
    * @return   may be null if !forceCreation
    */
   protected <A0> Emit.IListenerList<A0> getListenerList(Object eventID, boolean forceCreation) {

      if (eventID == null)
         throw new IllegalArgumentException("an eventID may not be null");

      Emit.IListenerList<A0> listenersForID = idMap.get(eventID);
      if (!forceCreation || (listenersForID != null))
         return listenersForID;

      Emit.IListenerList<A0> newListeners = createListenerList();

      // icky cast but this lets subclasses have more flexibility
      ConcurrentHashMap<Object,Emit.IListenerList> ccm = (ConcurrentHashMap<Object,Emit.IListenerList>)idMap;
      listenersForID = ccm.putIfAbsent(eventID, newListeners);

      return listenersForID != null ? listenersForID : newListeners;
   }



}
