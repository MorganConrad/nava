nava
====

Bringing good ideas from node.js into Java.

[JavaDocs are here](http://morganconrad.github.io/nava/javadocs/)

nava/emit
---------

The first package, nava.emit, is a port of a [node.js EventEmitter](http://nodejs.org/api/events.html#events_class_events_eventemitter)

The behavior should match that of node.js, with these exceptions:

 1. A listener may only be added one time for each type of event.
    Unless you set allowDuplicates to true, adding a second time is ignored.
 2. Adding or removing null listeners is quietly ignored.
 3. In node, all "events" are defined by a unique String, e.g. 'err'.  nava.emit allows any non-null Object as a unique key.
    Strings are a good choice, but an Enum or the Class of the Event (if you use traditional Java Event objects) makes a lot of sense too.


####Why use this instead of Java's javax.swing.event.EventListenerList?

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

####Example Usage, "Node Style"

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

####Example Usage, "Java/Event Style"

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

###Notes

 1. I'm not a node.js guru
 2. I believe this code to be thread-safe but there could be improvements.