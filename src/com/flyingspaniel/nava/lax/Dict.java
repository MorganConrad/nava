package com.flyingspaniel.nava.lax;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2014 by Morgan Conrad
 */
public class Dict implements Map<String, Object> {

   public final Map<String,Object> map;


   public Dict() {
      this(new HashMap());
   }

   public Dict(Map<String, Object> inMap) {
        this.map = inMap;
   }

   public Dict(Class<Map> clazz) {
      try {
         map = clazz.newInstance();
      } catch (IllegalAccessException e) {
         throw new IllegalArgumentException(e);
      } catch (InstantiationException e) {
         throw new IllegalArgumentException(e);
      }
   }


   public Object object(String key) {
      return  Maps.objectOr(map, key, Maps.MUST_BE_PRESENT);
   }


   public Object objectOr(String key, Object or) {
      return Maps.objectOr(map, key, or);
   }


   public boolean bool(String key) {
      return Maps.bool(map, key);
   }


   public boolean boolOr(String key, boolean or) {
      return Maps.boolOr(map, key, or);
   }


   public double real(String key) {
      return Maps.real(map, key);
   }


   public double realOr(String key, double or) {
      return Maps.realOr(map, key, or);
   }


   public int integer(String key) {
      return Maps.integer(map, key);
   }


   public int integerOr(String key, int or) {
      return Maps.integerOr(map, key, or);
   }

   /*
     Below are all overrides of Map methods
    */
   @Override
   public void clear() {
       map.clear();
   }

   @Override
   public boolean containsKey(Object o) {
      return map.containsKey(o);
   }

   @Override
   public boolean containsValue(Object o) {
      return map.containsValue(o);
   }

   @Override
   public Set<Entry<String, Object>> entrySet() {
      return map.entrySet();
   }

   @Override
   public Object get(Object o) {
      return map.get(o);
   }

   @Override
   public boolean isEmpty() {
      return map.isEmpty();
   }

   @Override
   public Set<String> keySet() {
      return map.keySet();
   }

   @Override
   public Object put(String s, Object o) {
      return map.put(s, o);
   }

   @Override
   public void putAll(Map<? extends String, ?> map) {
       this.map.putAll(map);
   }

   @Override
   public Object remove(Object o) {
      return map.remove(o);
   }

   @Override
   public int size() {
      return map.size();
   }

   @Override
   public Collection<Object> values() {
      return map.values();
   }
}
