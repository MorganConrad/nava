package com.flyingspaniel.nava.emit;

import java.io.*;
import java.util.ArrayList;

/**
 * Demo "main" program for usage of Emit classes with multiple listeners
 *
 * @author: Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 */
public class EmitterDemo implements Emit.IListener<String> {

   ArrayList<String> oddLines;
   int lineCount = 0;

   Emitter emitter;
   PrintStream printer = System.out;

   @Override   // this listener collects all the odd numbered lines
   public void handleEvent(String arg0, Object... more) {
      lineCount++;
      if ((lineCount & 1) == 1)
         oddLines.add(arg0);
   }

   void setup() {

      this.emitter = new Emitter();
      this.oddLines = new ArrayList<String>();

      // one listener is this
      emitter.addListener("data", this);

      // one listener is an inner class
      emitter.addListener("data", new AllLinesListener());

      // one listener is an anonymous class
      emitter.addListener("end", new Emit.IListener<Void> () {
         @Override
         public void handleEvent(Void arg0, Object... more) {
            for (String s : oddLines)
               printer.println("odd lines: " + s);

            printer.println("[end]");
         }
      });

      emitter.addListener("error", new Emit.IListener<Exception> () {
         @Override
         public void handleEvent(Exception arg0, Object... more) {
            printer.println("error:");
            arg0.printStackTrace(printer);
         }
      });
   }

   void readFile(String filename) throws IOException {
      BufferedReader br = null;
      try {
         br = new BufferedReader(new FileReader(filename));
         String line;
         while ((line = br.readLine()) != null)
            emitter.fireEvent("data", line);

         emitter.fireEvent("end", null);
      }
      catch (IOException ioe) {
         emitter.fireEvent("error", ioe);
      }
      finally {
         if (br != null)
            br.close();
      }
   }


   public static void main(String[] args) throws IOException {

      String filename = ((args != null) && args.length > 0) ? args[0] : "com/flyingspaniel/nava/emit/EmitterDemo.java";

      EmitterDemo demo = new EmitterDemo();
      demo.setup();
      demo.readFile(filename);

      demo.readFile("this file does not exist");
   }



   class AllLinesListener implements Emit.IListener<String> {

      @Override
      public void handleEvent(String arg0, Object... more) {
         printer.println("all data: " + arg0);
      }
   }
}
