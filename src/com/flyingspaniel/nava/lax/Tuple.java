package com.flyingspaniel.nava.lax;

import com.flyingspaniel.nava.fp.FP;

import java.util.Arrays;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2014 by Morgan Conrad
 */
public class Tuple {

   public final Object[] tuple;

   public Tuple(Object...in) {
      tuple = Arrays.copyOf(in, in.length);
   }

   public Tuple(Tuple tpl) {
      tuple = Arrays.copyOf(tpl.tuple, tpl.len());
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(tuple);
   }

   @Override
   public boolean equals(Object obj) {
      return (obj instanceof Tuple) &&
              (Arrays.equals(tuple, ((Tuple)obj).tuple));
   }


   public int indexOf(Object obj) {
      return FP.indexOf(obj, tuple);
   }


   public int len() {
      return tuple.length;
   }

   public Object get(int idx) {
      if (idx < 0)
         idx = tuple.length + idx;

      return tuple[idx];
   }
}
