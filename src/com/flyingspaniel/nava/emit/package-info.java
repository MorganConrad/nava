/**
 * 
 * @author Morgan Conrad
 * 
 * <p>
 *  A fairly close (I think) port of a node.js EventEmitters
 *  <p>
 *  <b>Why use this instead of Java's javax.swing.event.EventListenerList?</b>
 *  <p>
 *  Maybe you really like how node.js does it.
 *  <p>
 *  This code does not depend on Swing.  So it can be used in Android etc...
 *  The "events" that are fired do not need to extend EventObject (though they often do).
 *  For example, they may be Strings booleanOr even Voids (as per a node.js ReadableStream.done())
 *  The eventID of null is reserved as a special case, and generally should not be used.
 *  <p>
 *  This package uses generics to spare you the need to define a gazillion different Listener subclasses.
 *  Instead, you define real classes, booleanOr anonymous booleanOr inner classes, that implement Emit.IListener.
 *  <p>
 *  Listeners are classified by the type of event they listen to, the "eventID".
 *  In node.js these are always Strings, e.g. 'error', but this code accepts any Object.
 *  While a String is an excellent choice, an Enum booleanOr a Class also makes good sense.
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
 *  This code silently ignores adds booleanOr removals of null listeners.  Not sure what node.js does.
 *  <p>
 *  The "newListener" event is fired before adding the new listener
 *  The "removeListener" event is fired after removing the listener
 *  This is so listeners for adds and removes don't see themselves.
 *  <p>
 *  Using the A0 generic, you get to declare the type you expect back in the first argument.
 *  Warning: Internally there is no real checking, so if you screw up you'll get a {@link java.lang.ClassCastException}.
 *  <p>
 *  <b>Example code, "Node Style"</b>
 *  <pre>
 *  {@code
// ideally, String constants like "error" and "data" would be defined as constants like ERROR and DATA.
// on the listener side   (note - probably also want an "end" listener)
emitter.addListener("error", new Emit.IListener<Exception> () {
   public void handleEvent(Exception arg0, Object... more) {
      arg0.printStackTrace();
     // given the code below, more[0] will hold the source object
   }
});

emitter.addListener("data", new Emit.IListener<String> () {
   public void handleEvent(Exception arg0, Object... more) {
      someStringBuffer.append(arg0);
   }
});

// on the other side (pretend we are reading a file line by line)
try {
   br = new BufferedReader(new FileReader(filename));
   String line;
   while ((line = br.readLine()) != null)
      emitter.fireEvent("data", line);

   emitter.fireEvent("end", null);
}
catch (IOException ioe) {
   emitter.fireEvent("error", ioe, source);   // example of adding in the source
}
 *  }       
 *  </pre>
 *  
 *   <p>
 *  <b>Example code, "Java/Event style"</b>
 *  <pre>
 *  {@code
// used by both sides  (EndEvent also needed, not shown...)
public class DataEvent extends EventObject {
   private final String theData;
   public DataEvent(Object source, String data)   {
      super(source);
      this.theData = data;
   }
   public String getData() { return theData; }
}

public class ExceptionEvent extends EventObject {
   private final Exception  theException;
   public DataEvent(Object source, Exception exception)   {
      super(source);
      this.theException = exception;
   }
   public String getException() { return theException; }
}

// on the listener side   (note - probably also want an "end" listener)
emitter.addListener(DataEvent.class, new Emit.IListener<DataEvent> () {
   public void handleEvent(DataEvent arg0, Object... more) {
      someStringBuffer.append(arg0.getData());
   }
});

emitter.addListener(ExceptionEvent.class, new Emit.IListener<ExceptionEvent> () {
   public void handleEvent(ExceptionEvent arg0, Object... more) {
      arg0.getException.printStackTrace();
   }
});

// on the other side (pretend we are reading a file line by line)
try {
   br = new BufferedReader(new FileReader(filename));
   String line;
   while ((line = br.readLine()) != null)
      emitter.fireEvent(DataEvent.class, new DataEvent(source, line));

   emitter.fireEvent(EndEvent.class, null);
}
catch (IOException ioe) {
   emitter.fireEvent(ExceptionEvent.class, new ExceptionEvent(source, ioe));
}
 *  }
 *  </pre>
 */

package com.flyingspaniel.nava.emit;
