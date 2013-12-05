package com.flyingspaniel.nava.fp;

/**
 * A bunch of implementations of basic Fns 
 * Mimics Scala's collection : min, max, sum, product
 * Suffixes of "pdouble" or "pint" indicate that they deal with primitives
 * 
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class Fns {
   
   public static final Fn MIN = new Fn.Base<Number, Number>() {

      @Override
      public Number fn2(Number n1, Number n2) {
         double d1 = n1.doubleValue();
         double d2 = n2.doubleValue();
         return (d1 < d2) ? d1 : d2;
      }
      
      
      @Override
      public Number fnN(Number...numbers) {
         double min = numbers[0].doubleValue();
         for (int i=0; i<numbers.length; i++) {
            double d = numbers[i].doubleValue();
            if (d < min)
               min = d;
         }
         
         return min;
      }
      
      // for speed could implement a special fn2() method but not done...
   };

   public static final Fn.Pdouble.AndN MINpdouble = new Fn.Pdouble.Base() {
      
      @Override public double fnN(double[] ds) {
         double min = ds[0];
         for (int i=0; i<ds.length; i++) {
            if (ds[i] < min)
               min = ds[i];
         }

         return min;
      }
   };

   public static final Fn.Pint.AndN MINpint = new Fn.Pint.Base() {

      @Override public int fnN(int[] ds) {
         int min = ds[0];
         for (int i=0; i<ds.length; i++) {
            if (ds[i] < min)
               min = ds[i];
         }

         return min;
      }
   };
   

   public static final Fn.Base<Number, Number> MAX = new Fn.Base<Number, Number>() {

      @Override
      public Number fn2(Number n1, Number n2) {
         double d1 = n1.doubleValue();
         double d2 = n2.doubleValue();
         return (d1 > d2) ? d1 : d2;
      }
      
      @Override
      public Number fnN(Number...numbers) {
         double max = numbers[0].doubleValue();
         for (int i=0; i<numbers.length; i++) {
            double d = numbers[i].doubleValue();
            if (d > max)
               max = d;
         }

         return max;
      }

   };

   public static final Fn.Pdouble.AndN MAXpdouble = new Fn.Pdouble.Base() {

      @Override public double fnN(double[] ds) {
         double max = ds[0];
         for (int i=0; i<ds.length; i++) {
            if (ds[i] > max)
               max = ds[i];
         }

         return max;
      }
   };

   public static final Fn.Pint.AndN MAXpint = new Fn.Pint.Base() {

      @Override public int fnN(int[] ds) {
         int max = ds[0];
         for (int i=0; i<ds.length; i++) {
            if (ds[i] > max)
               max = ds[i];
         }

         return max;
      }
   };
   
   
   public static final Fn<Number, Number> SUM = new Fn.Base<Number, Number>() {

      @Override public Number fn1(Number n1) { return n1; }

      @Override
      public Number fn2(Number n1, Number n2) {
         return n1.doubleValue() + n2.doubleValue();
      }
      
      @Override
      public Number fnN(Number...numbers) {
         double sum = numbers[0].doubleValue();
         for (int i=0; i<numbers.length; i++) {
            sum += numbers[i].doubleValue();
         }

         return sum;
      }

   };

   public static final Fn.Pdouble.AndN SUMpdouble = new Fn.Pdouble.Base() {

      @Override public double fn1(double n1) { return n1; }

      @Override
      public double fn2(double n1, double n2) { return n1 + n2; }

      @Override
      public double fnN(double...doubles) {
         double sum = 0.0;
         for (double d : doubles)
            sum += d;
         return sum;
      }
   };

   public static final Fn.Pint.AndN SUMpint = new Fn.Pint.Base() {

      @Override public int fn1(int n1) { return n1; }

      @Override
      public int fn2(int n1, int n2) { return n1 + n2; }

      @Override
      public int fnN(int...ints) {
         int sum = 0;
         for (int i : ints)
            sum += i;
         return sum;
      }
   };

   
   public static final Fn<Number, Number> PRODUCT = new Fn.Base<Number, Number>()  {

      @Override public Number fn1(Number n1) { return n1; }

      @Override
      public Number fn2(Number n1, Number n2) {
         return n1.doubleValue() * n2.doubleValue();
      }

      @Override
      public Number fnN(Number...numbers) {
         double prod = numbers[0].doubleValue();
         for (int i=0; i<numbers.length; i++) {
            prod *= numbers[i].doubleValue();
         }

         return prod;
      }

   };

   public static final Fn.Pdouble.AndN PRODUCTpdouble = new Fn.Pdouble.Base() {

      @Override public double fn1(double n1) { return n1; }

      @Override
      public double fn2(double n1, double n2) { return n1 * n2; }

      @Override
      public double fnN(double...doubles) {
         double prod = 1.0;
         for (double d : doubles)
            prod *= d;
         return prod;
      }
   };

   public static final Fn.Pint.AndN PRODUCTpint = new Fn.Pint.Base() {

      @Override public int fn1(int n1) { return n1; }

      @Override
      public int fn2(int n1, int n2) { return n1 * n2; }

      @Override
      public int fnN(int...ints) {
         int prod = 1;
         for (int i : ints)
            prod *= i;
         return prod;
      }
   };


}
