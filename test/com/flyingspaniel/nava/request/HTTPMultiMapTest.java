package com.flyingspaniel.nava.request;

import java.util.HashMap;
import java.util.Map;

import com.flyingspaniel.nava.net.URLEncoding;
import junit.framework.TestCase;

public class HTTPMultiMapTest extends TestCase {

   public void testNormal() {
      HTTPMultiMap hmm = new HTTPMultiMap();
      assertEquals("?", hmm.toString());

      hmm.addFieldValuePairs("fieldFoo", "value Foo", "integer", 4, "fieldNoValue", null);
      assertEquals("fieldFoo=value Foo&integer=4&fieldNoValue=", hmm.toQueryString(URLEncoding.Impl.NONE));
      assertFalse(hmm.add("fieldFoo", "value Foo")); // already there

      assertEquals("&", hmm.useSemicolonQuerySeparator(true));
      assertEquals("fieldFoo=value+Foo;integer=4;fieldNoValue=", hmm.toQueryString(URLEncoding.Impl.JAVA));
      assertEquals(";", hmm.useSemicolonQuerySeparator(false));

      hmm.addQueryString(null, "bar1=bar&fieldFoo=foo2nd&noValue2");
      assertEquals("?fieldFoo=value Foo&fieldFoo=foo2nd&integer=4&fieldNoValue=&bar1=bar&noValue2=", hmm.toString());

      assertEquals("value Foo,foo2nd", hmm.getHeaderString("fieldFoo", URLEncoding.Impl.NONE));
      assertEquals("4", hmm.getHeaderString("integer", URLEncoding.Impl.NONE));
      assertEquals("", hmm.getHeaderString("does not exist", URLEncoding.Impl.NONE));

      assertTrue(hmm.remove("fieldFoo", "foo2nd"));
      assertFalse(hmm.remove("fieldFoo", "foo2nd"));
      assertFalse(hmm.remove("field not there", "foo2nd"));
      assertEquals("[value Foo]", hmm.remove("fieldFoo").toString());
      assertEquals("?integer=4&fieldNoValue=&bar1=bar&noValue2=", hmm.toString());

   }


   public void testSingleMap() {
      HashMap<String, String> inSingleMap = new HashMap<String, String>();
      inSingleMap.put("field1", "value1");
      inSingleMap.put("field2", "value2");

      HTTPMultiMap hmm = new HTTPMultiMap();
      hmm.addSingleMap(inSingleMap);
      assertEquals("field2=value2&field1=value1", hmm.toQueryString(URLEncoding.Impl.NONE));

      hmm.add("field1", "anotherValue1");
      Map<String, String> outSingleMap = hmm.toSingleMap(true, true);
      assertEquals("{field1=value1,anotherValue1, field2=value2}", outSingleMap.toString());

      try {
         outSingleMap = hmm.toSingleMap(true, false);
         fail();
      }
      catch (IllegalStateException expected) {
         ;
      }
   }


   public void testStatics() {
      assertEquals("nothing to strip", HTTPMultiMap.stripLeadingAmpOrQuestion("nothing to strip"));
      assertEquals("oneleadingamp", HTTPMultiMap.stripLeadingAmpOrQuestion("&oneleadingamp"));
      assertEquals("?twoleadingquestions", HTTPMultiMap.stripLeadingAmpOrQuestion("??twoleadingquestions"));
   }

   public void testSortedAndRFC3986() {
      HTTPMultiMap hmm = new HTTPMultiMap(true, null);
      hmm.addFieldValuePairs("fieldFoo", "value Foo", "integer", 4, "fieldNoValue", null);
      hmm.addQueryString(null, "bar1=bar&fieldFoo=foo2nd&noValue2");

      assertEquals("bar1=bar&fieldFoo=value%20Foo&fieldFoo=foo2nd&fieldNoValue=&integer=4&noValue2=", hmm.toQueryString(URLEncoding.Impl.RFC3986));
   }


   public void testBad() {
      HTTPMultiMap hmm = new HTTPMultiMap();
      hmm.addFieldValuePairs();
      assertEquals("?", hmm.toString());

      try {
         hmm.addFieldValuePairs("only one");
         fail();
      }
      catch (IllegalArgumentException expected) {
         ;
      }


   }
}
