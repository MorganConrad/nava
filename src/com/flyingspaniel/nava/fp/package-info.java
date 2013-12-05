/**
 *
 * @author Morgan Conrad
 * <p>
 *  Code for using and linking together functions, inspired by javascript/Scala/Groovy.
 *  Functions may have an Object version, using Objects as inputs/outputs (generified by IN, OUT)
 *  For efficiency, there are specific versions dealing with primitive doubles and ints
 *  <p>
 *  Typical usage:   
 *  <ol>
 *     <li>Implement Fn (or Fn.Pdouble or Fn.Pint), perhaps by extending one of the Base classes.</li>
 *     <li>Check out the Fns class (and FPTest) for examples</li>
 *     <li>Combine your Fn with one of the utility methods from FP.</li>
 *  </ol>
 *
 *  TODO - combine these somehow with Callbacks and CallbackExecutor for multi-threaded processing.
 * 
 *  Warning: Internally there is no real checking, so if you screw up you'll get a {@link java.lang.ClassCastException}.
 *
 */

package com.flyingspaniel.nava.fp;
