/**
 *
 * @author Morgan Conrad
 *
 * <p>
 *  Code for using and linking together callbacks, inspired by node.js / javascript
 *  Due to language differences (this isn't written for Java 8) it is only a quasi-port.
 *  Though many of the classes are generified as aids to documentation, ultimately there are unchecked
 *  casts involved, so if you listen for a String but get a Date bad things will happen.
 *  <p>
 *  Typical Usage:   
 *  <ol>
 *     <li>Implement Callback, perhaps by extending AbstractCallback.  Usually the more... part is ignored.</li>
 *     <li>You <b>must</b> test for an incoming Exception.  And easy way is to call failFast() or failSlow()</li>
 *     <li>If your callback returns lots of "optional extra" information, (rare) extend Callback.ProducesMore</li>
 *     <li>If it produces multiple results that can be processed in parallel, extend Callback.ProducesMultiple</li>
 *     <li>Hook up your callbacks, either manually, or by using Callbacks.chainUp()</li>
 *     <li>You can run them synchronously (nice for testing) using Callbacks.runSync()</li>
 *     <li>Run them asynchronously ("fire and forget" mode) using CallbackExecutor.submitCallback()</li>
 *     <li>There are many examples in AbstractCallback, or the code in CallbacksTest</li>
 *  </ol>
 *
 * Notes:
 * <ul>
 *    <li>Multiple callback options, choosing according to status, was considered and, for now, rejected.
 *    It added a lot of complexity, is not a feature in JavaScript, and is more suited for an Emitter.</li>
 *    <li>Putting the link to the next callback somewhere else (e.g. in a Collection of Callbacks) was
 *    strongly considered and, for now, rejected.  It has advantages and disadvantages.</li>
 *    <li>The ProducesMore and ProducesMultiple options likely introduce tricky state in your Callbacks.  Be wary.</li>
 * </ul>
 */

package com.flyingspaniel.nava.callback;
