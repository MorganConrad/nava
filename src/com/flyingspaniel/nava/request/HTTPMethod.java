package com.flyingspaniel.nava.request;


/**
 * Enums representing the main HTTP Methods
 *
 * @author Morgan Conrad
 * @since Copyright(c) 2013  Morgan Conrad
 *
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 */
public enum HTTPMethod {

   // for simplicity when using .getMethodName(), these exactly match the HTTP commands
   // does  input  output
   POST(    true,  true),
   GET(     true,  false),
   PUT(     true,  true),
   DELETE(  true, false),
   HEAD(   false, false),
   PATCH(  false,  true);

   public final boolean doesInput;
   public final boolean doesOutput;


   /**
    * Constructor
    *
    * @param doesInput
    * @param doesOutput
    */
   HTTPMethod(boolean doesInput, boolean doesOutput) {
      this.doesInput = doesInput;
      this.doesOutput = doesOutput;
   }


   public boolean doesInput() {
      return doesInput;
   }

   public boolean doesOutput() {
      return doesOutput;
   }

   public boolean followsRedirects() {
      return (this ==  GET) || (this == HEAD);
   }

   public boolean canDo(boolean mustDoInput, boolean mustDoOutput) {
      return  (doesInput || !mustDoInput) && (doesOutput || !mustDoOutput);
   }

   /**
    * Use this instead of name(), so if the names happen to change (unlikely!) we can fix it here
    *
    * @return the name, e.g. "PUT"
    */
   public String getMethodName() {
      return name();
   }


}
