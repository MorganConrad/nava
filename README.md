nava
====

Bringing good ideas from node.js into Java.

[JavaDocs are here](http://morganconrad.github.io/nava/javadocs/)


nava/callback
-------------

A rough approximation of callbacks.  Which can also serve as Emit.IListener.  Due to language differences (this isn't written for Java 8) it
is only a quasi-port.  Though many of the classes are generified as aids to documentation,
ultimately there are unchecked casts involved, so if you listen for a String but get a Date bad things will happen.

nava/emit
---------

A fairly close (I think) port of a [node.js EventEmitter](http://nodejs.org/api/events.html#events_class_events_eventemitter)

The behavior should match that of node.js, with these exceptions:

 1. A listener may only be added one time for each type of event.
    Unless you set allowDuplicates to true, adding a second time is ignored.
 2. Adding or removing null listeners is quietly ignored.
 3. In node, all "events" are defined by a unique String, e.g. 'err'.  nava.emit allows any non-null Object as a unique key.
    Strings are a good choice, but an Enum or the Class of the Event (if you use traditional Java Event objects) makes a lot of sense too.


#### Why use this instead of Java's javax.swing.event.EventListenerList?

Perhaps because you really like how node.js does it.   But there are other advantages over classic Swing EventListeners.

 1. This code does not depend on Swing.  So it can be used in Android etc...
 2. The "events" that are fired do not need to extend EventObject (though they may).
    For example, they may be Strings or even Voids (as per a node.js ReadableStream.done()).
    This spares you from defining a gazillion event classes like FooEvent, BarEvent, ...
 3. This package uses generics to spare you the need to define a gazillion different Listener subclasses,
    and their respective methods addFooListener(), addBarListener(), removeFooListener()...
    Instead, you define classes, or anonymous or inner classes, that implement Emit.IListener<A0>.

With some possible disadvantages:

 1. Since you don't have a specific Event ot Listener class to guide event dispatch, events are IDed by a unique ID, which may be any Object
    This can be a cause of confusion of incompatibility,
    Node has some standard conventions, consider using them.
    Or, if you choose to use EventObjects, an "id" of their class would be appropriate.
 2. Without an EventObject you don't have a source.  This could be passed as one of the function arguments.
 3. Due to erasure, the same class may not implement multiple Listeners.  But you can use inner classes or anonymous classes.
 4. Ultimately there are unchecked casts involved, so if you listen for a String but get a Date bad things will happen.

nava/fp
---------
 
A start at Functional Programming.  To use:
 
 1. Implement Fn (or Fn.Pdouble or Fn.Pint), perhaps by extending one of the Base classes
 2. Check out the Fns class (and FPTest) for examples
 3. Combine your Fn with one of the utility methods from FP, like every(), filter(), fold()...
 
nava/hash
---------
  
Utility code for pulling values from Maps.  Useful because JavaScript/node.js usually use { } "objects" or "hashes" to pass settings and options.
 
To.java converts a raw Object "to" a different type, with two main method signatures:  
`type typeFrom(Object in)` converts a single Object to a type  
`type typeOr(Object in, type...or)` mimics the JavaScript || operator, returning the `or[0]` if the 1st is null</li>
   

Hash.java gets the value from a Map.  The major method signature is `getType(Map map, String key, Type...or)`  
  If the key is not in the map, and or is present, `or[0]` is returned, else a Hash.NoSuchKeyException is thrown    
  
### Notes

 1. I'm not a node.js guru
 2. I believe this most of this code to be thread-safe but there could be improvements.
