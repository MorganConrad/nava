package com.flyingspaniel.nava.fp;

/**
 * Interfaces for functional programming
 * 
 * The main interface, Fn, is genericized by an input IN and output OUT.  It has 1arg and 2arg methods fn1 and fn2,
 * It accepts an extension interface, Fn.N, if you support a varargs method.
 * 
 * For efficiency, two "inner sub-interfaces" are provided <ul>
 *    <li>Fn.Pdouble for dealing with primitive doubles</li>
 *    <li>Fn.Pint for dealing with primitive ints</li>
 * </ul>
 * 
 * "Abstract" Base classes that throw UnsupportedOperationExceptions for all functions are provided for all three types.
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public interface Fn<IN, OUT> {

   /**
    * Apply the function to a single argument
    * @param    in  input
    * @return  OUT
    */
   public OUT fn1(IN in);

   /**
    * Apply the function to a two arguments
    * @param in1
    * @param in2
    * @return OUT
    */
   public OUT fn2(IN in1, IN in2);

   /**
    * Optional additional interface if we can take an array / varargs
    */
   public static interface N<IN, OUT> {
      public OUT fnN(IN...ins);
   }

   public static interface AndN<IN, OUT> extends Fn<IN, OUT>, Fn.N<IN, OUT> {}


   /**
    * Base class that throws UnsupportedOperationExceptions for all methods.  Subclass this as needed.
    */
   public static class Base<IN, OUT> implements Fn.AndN<IN, OUT> {

      protected Base() {}   // not to be directly instantiated
       
      @Override public OUT fn1(IN in) { throw new UnsupportedOperationException(); }

      // note - you cannot just call fnN(in1, in2).  See StackOverflow question #20389858
      @Override public OUT fn2(IN in1, IN in2) { throw new UnsupportedOperationException(); }

      @Override public OUT fnN(IN...ins) { throw new UnsupportedOperationException(); }
   }


   /**
    * "Efficiency interface for dealing with primitive doubles
    */
   public static interface Pdouble {
      public double fn1(double d1);
      public double fn2(double d1, double d2);

      /**
       *  Optional additional interface if we can take an array / varargs
       */
      public static interface N {
         public double fnN(double...ds);
      }

      public static interface AndN extends Pdouble, Pdouble.N {}

      /**
       * Base class that throws UnsupportedOperationExceptions for all methods.  Subclass this as needed.
       */
      public static class Base implements Pdouble.AndN {

         protected Base() {}    // not to be directly instantiated

         // unlike the generic version, here calling the varags should work
         @Override public double fn1(double d1) { return fnN(d1); }
         
         @Override public double fn2(double d1, double d2) { return fnN(d1, d2); }

         @Override public double fnN(double...doubles) { throw new UnsupportedOperationException(); }
      }
   }

   /**
    * "Efficiency interface for dealing with primitive ints
    */
   public static interface Pint {
      public int fn1(int i1);
      public int fn2(int i1, int i2);

      /**
       *  Optional additional interface if we can take an array / varargs
       */
      public static interface N {
         public int fnN(int...ints);
      }

      public static interface AndN extends Pint, Pint.N {}

      /**
       * Base class that throws UnsupportedOperationExceptions for all methods.  Subclass this as needed.
       */
      public static class Base implements Pint.AndN {

         protected Base() {}  // not to be directly instantiated

         // unlike the generic version, here calling the varags should work
         @Override public int fn1(int i1) { return fnN(i1); }

         @Override public int fn2(int i1, int i2) { return fnN(i1, i2); }

         @Override public int fnN(int...ints) { throw new UnsupportedOperationException(); }
      }
      
   }

   
   
}
