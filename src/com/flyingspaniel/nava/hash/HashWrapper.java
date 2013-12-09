package com.flyingspaniel.nava.hash;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public interface HashWrapper extends Map<String,Object> {

   public boolean getBoolean(String key, boolean...or);
   public double getDouble(String key, double...or);
   public int getInt(String key, int...or);
   public List<?> getList(String key, boolean create);
   public Map<String,?> getMap(String key, boolean create);
   public String getString(String key);
   public String getString(String key, String...or);
   public Object getObject(String key, Object defaultObject);


   public static class Linked extends LinkedHashMap<String, Object> implements HashWrapper {

      public Linked() { super(); }

      public Linked(Map<String, ?> map) { super(map); }

      @Override public boolean getBoolean(String key, boolean...or) {
         // special case, default to false if nothing is found
         return (or.length > 0) ? Hash.getBoolean(this, key, or) : Hash.getBoolean(this, key, false);
      }

      @Override public double getDouble(String key, double...or) {
         return Hash.getDouble(this, key, or);
      }
      @Override public int getInt(String key, int...or) {
         return Hash.getInt(this, key, or);
      }
      @Override public List<Object> getList(String key, boolean create){
         return Hash.getList(this, key, create);
      }
      @Override public Map<String,?> getMap(String key, boolean create){
         return Hash.getMap(this, key, create);
      }
      @Override public String getString(String key) {
         return Hash.getString(this, key);
      }
      @Override public String getString(String key, String...or) {
         return Hash.getString(this, key, or);
      }
      @Override public Object getObject(String key, Object defaultObject) {
         return Hash.getObject(this, key, defaultObject);
      }
   }
}
