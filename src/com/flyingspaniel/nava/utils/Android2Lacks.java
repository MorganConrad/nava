package com.flyingspaniel.nava.utils;

import java.util.Map;

/**
 * Helper classes for some useful things lacking in Android 2
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class Android2Lacks {

   /**
    * Android 2.2 lacks a chainable IOException with message AND cause
    * @param   message
    * @param   cause
    * @return  java.io.IOException
    */
   public static java.io.IOException IOException(String message, Throwable cause) {
      java.io.IOException ioe = new java.io.IOException(message);
      ioe.initCause(cause);
      return ioe;
   }


   /**
    * Immutable Map.Entry class: Andrdoid 2.2 doesn't have AbstractMap.Simple...
    * @param <K>
    * @param <V>
    */
   public static class ImmutableMapEntry<K, V> implements Map.Entry<K, V> {

      private final K k;
      private final V v;

      public ImmutableMapEntry(K key, V value) {
         this.k = key;
         this.v = value;
      }

      @Override public K getKey() {
         return k;
      }

      @Override public V getValue() {
         return v;
      }

      @Override public V setValue(V v) {
         throw new UnsupportedOperationException();
      }

   }


   /**
    * Mutable Map.Entry class: Andrdoid 2.2 doesn't have AbstractMap.Simple...
    * @param <K>
    * @param <V>
    */
   public static class MutableMapEntry<K, V> implements Map.Entry<K, V> {
      private final K k;
      private V v;

      public MutableMapEntry(K key, V value) {
         this.k = key;
         this.v = value;
      }

      @Override public K getKey() {
         return k;
      }

      @Override public V getValue() {
         return v;
      }

      @Override public V setValue(V v) {
         V was = this.v;
         this.v = v;
         return was;
      }

   }
}
