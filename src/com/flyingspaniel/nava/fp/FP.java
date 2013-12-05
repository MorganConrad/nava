package com.flyingspaniel.nava.fp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Static utilities for Functional Programming
 * 
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class FP {

   /**
    * Fold objects using a generified Fn function
    */
   public static <IN> IN fold(IN seed, Iterable<IN> ins, Fn<IN,IN> fn) {
       for (IN in : ins)
          seed = fn.fn2(seed, in);
      
      return seed;
   }

   /**
    * Fold doubles using a primitive double (Fn.Pdouble) function
    */
   public static double foldp(double seed, double[] ins, Fn.Pdouble fn) {     
      for (double in : ins)
         seed = fn.fn2(seed, in);

      return seed;
   }

   /**
    * Fold ints using a primitive int (Fn.Pint) function
    */
   public static int foldp(int seed, int[] ins, Fn.Pint fn) {
      for (int in : ins)
         seed = fn.fn2(seed, in);

      return seed;
   }

   
   /**
    * FoldRight objects using a generified Fn function
    */
   public static <IN> IN foldRight(IN seed, List<IN> ins, Fn<IN,IN> fn) {
      int idx = ins.size();
      while (--idx >= 0)
         seed = fn.fn2(seed, ins.get(idx));

      return seed;
   }

   /**
    * FoldRight doubles using a primitive double (Fn.Pdouble) function
    */
   public static double foldRightp(double seed, double[] ins, Fn.Pdouble fn) {
      int idx = ins.length;
      while (--idx >= 0)
         seed = fn.fn2(seed, ins[idx]);

      return seed;
   }

   /**
    * FoldRight ints using a primitive int (Fn.Pint) function
    */
   public static int foldRightp(int seed, int[] ins, Fn.Pint fn) {
      int idx = ins.length;
      while (--idx >= 0)
         seed = fn.fn2(seed, ins[idx]);

      return seed;
   }

   /**
    * Reduce Objects using a generified Fn function
    */
   public static <IN> IN reduce(Iterable<IN> ins, Fn<IN,IN> fn) {
      Iterator<IN> iter = ins.iterator();
      IN in = iter.next();
      while (iter.hasNext())
         in = fn.fn2(in, iter.next());
      
      return in;
   }

   /**
    * Reduce doubles using a primitive double (Fn.Pdouble) function
    */
   public static double reducep(double[] ins, Fn.Pdouble fn) {
      if (fn instanceof Fn.Pdouble.N)
         return ((Fn.Pdouble.N)fn).fnN(ins);
      
      double v = ins[0];
      for (int i=1; i<ins.length; i++)
         v = fn.fn2(v, ins[i]);

      return v;
   }

   /**
    * Reduce ints using a primitive int (Fn.Pint) function
    */
   public static int reducep(int[] ins, Fn.Pint fn) {
      if (fn instanceof Fn.Pint.N)
         return ((Fn.Pint.N)fn).fnN(ins);

      int v = ins[0];
      for (int i=1; i<ins.length; i++)
         v = fn.fn2(v, ins[i]);

      return v;
   }


   /**
    * Filter (keep) Objects accepted by Fn to a new ArrayList.  Shallow copy.
    * 
    * @param ins      inputObjects
    * @param filterFn return null or Boolean.FALSE to eliminate Objects
    * @param <IN>     type of Objects 
    * @return         ArrayList, may be empty
    */
   public static<IN> ArrayList<IN> filter(Iterable<IN> ins, Fn<IN,?> filterFn) {
      ArrayList<IN> passed = new ArrayList<IN>();
      for (IN in : ins)
         if (isTrue(filterFn.fn1(in)))
            passed.add(in);

      return passed;
   }

   /**
    * Map all Objects to a new List, transforming them via Fn
    * @param ins   inputObjects
    * @param fn    maps from IN to OUT
    * @param <IN>  type of input Objects 
    * @param <OUT> type of result Objects
    * @return      ArrayList of OUTs, same length as ins
    */
   public static<IN,OUT> ArrayList<OUT> map(Iterable<IN> ins, Fn<IN,OUT> fn) {
      ArrayList<OUT> mapped = new ArrayList<OUT>();
      for (IN in : ins)
         mapped.add(fn.fn1(in));

      return mapped;
   }


   /**
    * Check if every Object passes Fn  (same as forAll())
    * @param ins      if empty returns true
    * @param filterFn Return null or Boolean.FALSE to reject a value
    * @param <IN>
    * @return         true if all were accepted by fn
    */
   public static<IN> boolean every(Iterable<IN> ins, Fn<IN,?> filterFn) {
      for (IN in : ins)
         if (isFalse(filterFn.fn1(in)))
            return false;
      
      return true;
   }

   /**
    * Check if any Object passes Fn (same as some() and Groovy's any())
    * @param ins       if empty returns false
    * @param filterFn  Return null or Boolean.FALSE to reject a value
    * @param <IN>
    * @return          true if all were accepted by fn
    */
   public static<IN>  boolean exists(Iterable<IN> ins, Fn<IN,?> filterFn) {
      for (IN in : ins)
         if (isTrue(filterFn.fn1(in)))
            return true;

      return false;
   }


   /**
    * Check if every Object passes Fn  (same as every())
    * @param ins       if empty returns true
    * @param filterFn  Return null or Boolean.FALSE to reject a value
    * @param <IN>
    * @return          true if all were accepted by fn
    */
   public static<IN>  boolean forAll(Iterable<IN> ins, Fn<IN,?> filterFn) {
      return every(ins, filterFn);
   }

   /**
    * Check if any Object passes Fn (same as exists())
    * @param ins  if empty returns false
    * @param fn   Return null or Boolean.FALSE to reject a value
    * @param <IN>
    * @return     true if all were accepted by fn
    */
   public static<IN>  boolean some(Iterable<IN> ins, Fn<IN,?> fn) {
      return exists(ins, fn);
   }


   /**
    * For a filter function (filterFn in formal arguments above) converts their output to true/false
    * @param o
    * @return true    if o is not null and not Boolean.FALSE
    */
   public static boolean isTrue(Object o) {
      return (o != null) && !(Boolean.FALSE == o);
   }

   /**
    * For a filter function (filterFn in formal arguments above) converts their output to true/false
    * @param o
    * @return  true    if o is null or Boolean.FALSE
    */
   public static boolean isFalse(Object o) {
      return (o == null) || (Boolean.FALSE == o);
   }


   
}
