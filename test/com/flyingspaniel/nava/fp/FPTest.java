package com.flyingspaniel.nava.fp;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class FPTest extends TestCase {
   
   static ArrayList<Number> numbers = new ArrayList<Number>();
   static {
      for (int i=1; i<=6; i++)
         numbers.add(i);
   }
   static final double[] doubles = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
   static final int[] ints = new int[] { 1, 2, 3, 4, 5, 6};
   
   
   public void testFold() throws Exception {
      
      Object foo = Fns.MIN.fn2(1.2, 3.4);
      
       assertEquals(6.0, FP.fold(Double.NEGATIVE_INFINITY, numbers, Fns.MAX).doubleValue());
       assertEquals(1.0, FP.fold(Double.MAX_VALUE, numbers, Fns.MIN).doubleValue());
       assertEquals(21.0, FP.fold(0.0, numbers, Fns.SUM).doubleValue());
       assertEquals(720.0, FP.fold(1.0, numbers, Fns.PRODUCT).doubleValue());
   }

   public void testFoldP() throws Exception {
      assertEquals(6.0, FP.foldp(Double.NEGATIVE_INFINITY, doubles, Fns.MAXpdouble));
      assertEquals(1.0, FP.foldp(Double.MAX_VALUE, doubles, Fns.MINpdouble));
      assertEquals(21.0, FP.foldp(0.0, doubles, Fns.SUMpdouble));
      assertEquals(720.0, FP.foldp(1.0, doubles, Fns.PRODUCTpdouble));

      assertEquals(6, FP.foldp(Integer.MIN_VALUE, ints, Fns.MAXpint));
      assertEquals(1, FP.foldp(Integer.MAX_VALUE, ints, Fns.MINpint));
      assertEquals(21, FP.foldp(0, ints, Fns.SUMpint));
      assertEquals(720, FP.foldp(1, ints, Fns.PRODUCTpint));
   }
   
   

   public void testReduce() throws Exception {
      assertEquals(6.0, FP.reduce(numbers, Fns.MAX).doubleValue());
      assertEquals(1.0, FP.reduce(numbers, Fns.MIN).doubleValue());
      assertEquals(21.0, FP.reduce(numbers, Fns.SUM).doubleValue());
      assertEquals(720.0, FP.reduce(numbers, Fns.PRODUCT).doubleValue());
   }

   public void testReduceP() throws Exception {
      assertEquals(6.0, FP.reducep(doubles, Fns.MAXpdouble));
      assertEquals(1.0, FP.reducep(doubles, Fns.MINpdouble));
      assertEquals(21.0, FP.reducep(doubles, Fns.SUMpdouble));
      assertEquals(720.0, FP.reducep(doubles, Fns.PRODUCTpdouble));

      assertEquals(6, FP.reducep(ints, Fns.MAXpint));
      assertEquals(1, FP.reducep(ints, Fns.MINpint));
      assertEquals(21, FP.reducep(ints, Fns.SUMpint));
      assertEquals(720, FP.reducep(ints, Fns.PRODUCTpint));
   }

   public void testFilter() throws Exception {
      ArrayList l = FP.filter(numbers, new GT(3.3));
      assertEquals("[4, 5, 6]", l.toString());
      l = FP.filter(numbers, new GT(4.4));
      assertEquals("[5, 6]", l.toString());
   }

   public void testMap() throws Exception {
      ArrayList l = FP.map(numbers, new Times(2.0));
      assertEquals("[2.0, 4.0, 6.0, 8.0, 10.0, 12.0]", l.toString());
   }

   public void testEvery() throws Exception {
      assertTrue(FP.every(numbers, new GT(-3.3)));
      assertFalse(FP.forAll(numbers, new GT(3.3)));
   }

   public void testExists() throws Exception {
      assertFalse(FP.exists(numbers, new GT(99.9)));
      assertTrue(FP.some(numbers, new GT(3.3)));
   }


   static class Times extends Fn.Base<Number, Number> {
      final double multiplier;
      Times(double d) { multiplier = d; }

      @Override
      public Number fn1(Number n1) {
         return n1.doubleValue() * multiplier;
      }

   }
   
   static class GT extends Fn.Base<Number, Number> {
      final double gt;
      GT(double d) { gt = d; }
      
      @Override
      public Number fn1(Number n1) {
         return (n1.doubleValue() > gt) ? n1 : null;
      }

   }
}
