package com.flyingspaniel.nava.hash;

import junit.framework.TestCase;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class HashTest extends TestCase {

   static final Hash.Wrapper map = new Hash.Wrapper();

   public void setUp() throws Exception {
      super.setUp();
      map.put("integer", 3);
      map.put("double", 3.3);
      map.put("true", Boolean.TRUE);
      map.put("String", "4");

   }

   public void test() {
       assertEquals(3, map.getInt("integer"));
       assertEquals(4, map.getInt("notthere", 4));
       assertEquals(4, map.getInt("String"));
       assertEquals(3.3, map.getDouble("double"));
       assertEquals(4.4, map.getDouble("notthere", 4.4));
       assertEquals(4.0, map.getDouble("String", 4.4));
       assertTrue(map.getBoolean("true"));
       assertFalse(map.getBoolean("notthere"));
       assertTrue(map.getBoolean("notthere", true));
       assertTrue(map.getBoolean("integer"));

       assertEquals("4", map.getString("String"));
       assertEquals("foobar", map.getString("notthere", "foobar"));

       assertEquals("4", map.getObject("String", null));
       assertEquals("4", map.getObject("notthere", "4"));
   }

   public void testExceptions() {
          try {
             map.getInt("notthere") ;
          }
          catch (Hash.NoSuchKeyException expected) {
             assertTrue(expected.getMessage().endsWith("notthere"));
          }
   }
}

