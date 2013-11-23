/**
 * 
 * @author Morgan Conrad
 * 
 * <p>
 *  Code for firing "events" inspired by node.js Emitters
 *  <p>
 *  <b>Why use this instead of Java's javax.swing.event.EventListenerList?</b>
 *  <p>
 *  Maybe you really like how node.js does it.
 *  <p>
 *  This code does not depend on Swing.  So it can be used in Android etc...
 *  The "events" that are fired do not need to extend EventObject (though they often do).
 *  For example, they may be Strings or even Voids (as per a node.js ReadableStream.done())
 *  The eventID of null is reserved as a special case, and generally should not be used.
 *  <p>
 *  This package uses generics to spare you the need to define a gazillion different Listener subclasses.
 *  Instead, you define real classes, or anonymous or inner classes, that implement Emit.IListener.
 *  <p>
 *  Listeners are classified by the type of event they listen to, the "eventID".
 *  In node.js these are always Strings, e.g. 'error', but this code accepts any Object.
 *  While a String is an excellent choice, an Enum or a Class also makes good sense.
*  <p>
 *  If there are multiple listeners, it uses {@link java.util.concurrent.CopyOnWriteArrayList} to hold the listener list.
 *  This is a perfect application of CopyOnWrite collections, thread-safe and efficient.
 *  <p>
 *  <b>Where this code may differ from node.js</b>
 *  <p>
 *  listenerCount() is both a static method on Emit, but also an instance method of IEmitter.
 *  <p>
 *  By default, (see {@link EmitOneID#allowDuplicates}), a listener will only be added once to each kind of eventID.
 *  Adding a second time does nothing.  However, it will still be added to once.
 *  If you call on(aListener), followed by once(aListener), it will be treated as a "once" listener
 *  <p>
 *  This code silently ignores adds or removals of null listeners.  Not sure what node.js does.
 *  <p>
 *  The "newListener" event is fired before adding the new listener
 *  The "removeListener" event is fired after removing the listener
 *  This is so listeners for adds and removes don't see themselves.
 *  <p>
 *  Using the A0 generic, you get to declare the type you expect back in the first argument.
 *  Warning: Internally there is no real checking, so if you screw up you'll get a {@link java.lang.ClassCastException}.
 *  
 */

package com.flyingspaniel.nava.emit;
