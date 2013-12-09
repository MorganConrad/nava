package com.flyingspaniel.nava.request;


import com.flyingspaniel.nava.fp.FP;
import com.flyingspaniel.nava.hash.To;
import com.flyingspaniel.nava.net.URLEncoding;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * HTTP uses multimaps for both query parameters and headers (a.k.a. Request Properties),
 * though with different rules.  This class internally covers both.
 *
 * Caller must either use toQueryString or getHeaderString etc... as required
 *
 * Querys with multiple values come out as multiple field=value pairs, e.g.
 * <pre>  field1=valueA&field1=valueB </pre>
 * but RequestProperties come out as comma separated "arrays" after a single key, e.g.
 * <pre>  Accept: text/html,application/xhtml+xml </pre>
 *
 * Internally (within this.map), the values are NOT encoded.
 * Encoding is performed at the "last minute" (in toQueryString)
 *
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 */
public class HTTPMultiMap {

   static final String EXISTS = "";
   static final String DEFAULT_QUERY_SEPARATOR = "&";

   // the underlying Map we use
   final protected Map<String, List<String>> map;

   /**
    * alternative is ';', @see http://www.w3.org/TR/1999/REC-html401-19991224/appendix/notes.html#h-B.2.2
    */
   protected String querySeparator = DEFAULT_QUERY_SEPARATOR;


   /**
    * Detailed constructor
    * @param sorted
    * @param copyFrom   if not null, copy values from this map   (e.g. as a copy-constructor)
    */
   public HTTPMultiMap(boolean sorted, Map<String, List<String>> copyFrom) {
      map = sorted ? new TreeMap<String, List<String>>() : new LinkedHashMap<String, List<String>>();
      if (copyFrom != null)
         map.putAll(copyFrom);
      this.querySeparator = querySeparator;
   }

   /**
    * Default (most common) constructor - results are not sorted...
    */
   public HTTPMultiMap() {
      this(false, null);
   }


   /**
    * Tries to convert this to a "single map" (Map<String, String>)
    *
    * @param sorted   whether result should be sorted by field
    * @param concatMultiple   if true, multiple values get concatenated (using List.toString() sans square brackets)
    * @return Map<String, String>
    * @throws IllegalStateException  if we have multiple values for a key and !concatMultiple
    */
   public Map<String, String> toSingleMap(boolean sorted, boolean concatMultiple) {
      Map<String, String> singleMap = sorted ? new TreeMap<String, String>() : new LinkedHashMap<String, String>();
      for (Map.Entry<String, List<String>> entry : map.entrySet()) {
         List<String> values = entry.getValue();
         if (values.isEmpty())
            continue;
         if (values.size() > 1) {
            if (concatMultiple) {
               singleMap.put(entry.getKey(), FP.join(values));
            }
            else
               throw new IllegalStateException("Cannot convert to a SingleMap cause there are some multiple values for " + entry);
         }
         else
            singleMap.put(entry.getKey(), values.get(0));
      }

      return singleMap;
   }


   /**
    * Add the field=value pair if not already present
    * synchronized so as to be slightly thread-safe
    *
    * @param field  non-null
    * @param value  may be null, in which case EXISTS ("") will be used
    */
   public synchronized boolean add(Object field, Object value) {
      String fieldS = field.toString();

      List<String> mapValue = map.get(fieldS);
      if (mapValue == null) {
         mapValue = newList();
         map.put(fieldS, mapValue);
      }

      String valueS = value != null ? value.toString() : EXISTS;

      if (!mapValue.contains(valueS))
         return mapValue.add(valueS);

      return false;  // field/value was already there
   }



   /**
    * Sets the field=value pairs, removing all previous values
    *
    * @param pairs   may be null, if not must have an even length
    */
   public void setFieldValuePairs(Object... pairs) {
      if (pairs == null)
         return;

      if ((pairs.length & 1) == 1)
         throw new IllegalArgumentException("pairs must have an even number of elements");

      for (int i = 0; i < pairs.length; i += 2) {
         map.remove(pairs[i]);
         add(pairs[i], pairs[i + 1]);
      }
   }


   /**
    * Add the entire contents of a map of field=value pairs
    *
    * @param singleMap  May be null.  Should be Map<Object, Object>  (not Map<Object, List<>>!!!)
    */
   public void addSingleMap(Map<?,?> singleMap) {
      if (singleMap != null)
         for (Map.Entry<?,?> entry : singleMap.entrySet())
            add(entry.getKey(), entry.getValue());
   }


   /**
    * Add another MultiMap
    * @param mm    May be null
    */
   public void addMultiMap(HTTPMultiMap mm) {
      if (mm != null) {
         for (Map.Entry<String,List<String>> entry : mm.map.entrySet()) {
            for (String value : entry.getValue())
               add(entry.getKey(), value);
         }
      }
   }


   /**
    * Add pairs of field=value
    * @param pairs   may be null, if not must have an even length
    */
   public void addFieldValuePairs(Object...pairs) {
      if (pairs == null)
         return;

      if ((pairs.length & 1) == 1)
         throw new IllegalArgumentException("pairs must have an even number of elements");

      for (int i=0; i<pairs.length; i += 2)
         add(pairs[i], pairs[i+1]);
   }



   /**
    * More for convenience, adds an existing query string to our map
    *
    * @param wasEncoded  existing encoding, null will become NONE
    * @param queryString non-null
    */
   public void addQueryString(URLEncoding wasEncoded, String queryString) {
      if (queryString.length() == 0)
         return;

      if (wasEncoded == null)
         wasEncoded = URLEncoding.Impl.NONE;

      queryString = stripLeadingAmpOrQuestion(queryString);
      String[] split1 = queryString.split(querySeparator);

      for (String keyValues : split1) {
         Map.Entry<String, String[]> decodedKVs = wasEncoded.decodeKVs(keyValues);
         String key = decodedKVs.getKey();
         String[] values = decodedKVs.getValue();
         if (values.length == 0)
            add(key, EXISTS);
         else for (String value : values)
            add(key, value);
      }
   }


   /**
    * Switches to semicolon instead of ampersand
    * @param useSemiColon
    * @return previous value
    */
   public String useSemicolonQuerySeparator(boolean useSemiColon) {
      String was = querySeparator;
      querySeparator = useSemiColon ? ";" : DEFAULT_QUERY_SEPARATOR;
      return was;
   }


   /**
    * Creates a String representing queries for all the fields.  Does not include any leading "?"
    * Multiple values get written multiple times, e.g. field=value1&field=value2
    * @param encoding
    * @return           will be "" if map is empty.
    */
   public String toQueryString(URLEncoding encoding) {
      if (map.isEmpty())
         return "";

      StringBuilder sb = new StringBuilder();

      for (String field: map.keySet()) {
         List<String> valueList = map.get(field);
         String[] values = To.stringsFrom(valueList, "");

         sb.append(querySeparator);
         if (values.length == 0)
            sb.append(encoding.encode(field));
         else for (int i=0; i<values.length; i++) {
            if (i > 0)
               sb.append("&");
            sb.append(encoding.encodeKVs(field, values[i]));
         }
      }

      return sb.substring(querySeparator.length());
   }



   @Override
   public String toString() {
      return toQueryString(URLEncoding.Impl.NONE);
   }


   /**
    * Get the String representing value(s) for a single field
    * Formatted as for a Header, with multiple values comma separated, e.g field=val1,val2
    *
    * @param field   if not present, "" will be returned
    * @param encoding
    * @return header string
    */
   public String getHeaderString(String field, URLEncoding encoding) {
      List<String> valueList = map.get(field);
      if (valueList == null)
         return "";
      String[] values = To.stringsFrom(valueList, "");
      return encoding.encodeKVs("", values);
   }




   /**
    * Adds all the field=value pairs to the URLConnection
    * If a field has multiple values, makes multiple calls to conn.addRequestProperty()
    *
    * @param conn
    */
   public void applyHeadersToConnection(URLConnection conn) {
      for (Map.Entry<String, List<String>> entry : map.entrySet()) {
         for (String s : entry.getValue()) {
            conn.addRequestProperty(entry.getKey(), s);
         }
      }
   }


   /**
    * remove an entire field from the map
    * @param field
    * @return previous value (if present)
    */
   public List<String> remove(String field) {
      return map.remove(field);
   }


   /**
    * remove a single field/value from the map.  Neither should be null
    *
    * @param field
    * @param value
    * @return  if something was removed
    */
   public boolean remove(String field, String value) {
      List<String> list = map.get(field);
      if (list != null)
         return list.remove(value);

      return false;
   }


   /**
    * direct access to the underlying map for advanced operations
    * should not be needed, use with caution
    *
    * @return  the underlying Map, never-null
    */
   public Map<String, List<String>> getMap() {
      return map;
   }


   /**
    * Removes a leading ampersand or question mark from a String
    *
    * @param in   non-null
    * @return possible shortened input
    */
   public static String stripLeadingAmpOrQuestion(String in) {
      if (in.length() == 0)
         return in;

      char first = in.charAt(0);
      return (first == '&' || first == '?') ? in.substring(1) : in;
   }



   protected List<String> newList() {
      return new ArrayList<String>();
   }


}
