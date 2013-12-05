package com.flyingspaniel.nava.callback;

/**
 * Interface for a javascript/node.js like callback (and chain)
 *
 * Warning: The generics <IN, OUT, MORE> are mainly for documentation and decoration.
 * Internally there are unchecked casts and if you screw up you will get ClassCastExceptions
 * Also, currently there is limited support for more...
 *
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 *
 */
public interface Callback<IN, OUT> {

   /**
    * The callback method
    * @param ex    null, or an Exception thrown from an earlier callback.  You should test for this!
    * @param in    input data, probably coming from a previous callback
    * @param more  additional input data (rarely used)
    * @return      result of processing
    * @throws Exception
    */
   public OUT callback(Exception ex, IN in, Object...more) throws Exception;

   /**
    * Set the callback to follow this one.
    * @param nextCallback  null means end of the line
    */
   public void setNextCallback(Callback nextCallback);

   /**
    * Get the callback to follow this one. 
    * @return  null means end of the line
    */
   public Callback getNextCallback();


   /**
    * Implement this (rare) if your callback produces "more" than a single output
    * This will be used by a single followup callback as its more... argument
    */
   public static interface ProducesMore<IN, OUT, MORE> extends Callback<IN, OUT> {
      public MORE[] getMore();
   }

   /**
    * Implement this if your callback produces multiple single outputs
    * to be used by multiple followup callbacks as their IN argument.
    *
    * @param <IN>
    * @param <OUT>
    */
   public static interface ProducesMultiple<IN, OUT> extends Callback<IN, OUT> {
      public OUT[] getMultiple();
   }
}
