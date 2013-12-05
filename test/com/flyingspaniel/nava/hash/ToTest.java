package com.flyingspaniel.nava.hash;

import junit.framework.TestCase;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class ToTest extends TestCase {

   static final Double D_THREE = new Double(3.0);
   static final String S_THREE = "3";

   public void testToDouble() {
      assertEquals(3.0, To.doubleFrom(D_THREE)) ;
      assertEquals(3.0, To.doubleFrom(S_THREE)) ;
   }

   public void testToInteger() {
      assertEquals(3, To.intFrom(D_THREE)) ;
      assertEquals(3, To.intFrom(S_THREE)) ;
   }

   public void testFalsyBoolean() {
      assertTrue(To.isFalsy(null));
      assertFalse(To.isFalsy(D_THREE));

      assertTrue(To.booleanFrom(true));
      assertFalse(To.booleanFrom(Boolean.FALSE));

      assertFalse(To.booleanFrom(0));
      assertFalse(To.booleanFrom(Double.NaN));

      assertFalse(To.booleanFrom(""));
      assertFalse(To.booleanFrom("0"));
      assertFalse(To.booleanFrom("fAlSe"));
      assertTrue(To.booleanFrom("I am not false"));

   }

   public void testOr() {
      assertEquals(3.0, To.doubleOr(D_THREE, 2.2)) ;
      assertEquals(3.0, To.doubleOr(null, 3.0)) ;
      assertEquals(3, To.intOr(S_THREE, 2)) ;
      assertEquals(3, To.intOr(null, 3)) ;
      assertTrue(To.booleanOr(Boolean.TRUE, false));
      assertTrue(To.booleanOr(null, true));

      assertEquals("3.0", To.StringOr(D_THREE, "foo"));
      assertEquals(D_THREE, To.ObjectOr(null, null, D_THREE, S_THREE));
      assertNull(To.ObjectOr(null, null, null));
   }
}
