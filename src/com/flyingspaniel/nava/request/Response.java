package com.flyingspaniel.nava.request;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Response {

   // backlink to the Request that triggered this HTTPResponse
   protected final Request request;

   /*
    * these are always taken from the HttpURLConnection, or else are null
    */
   public final String contentType;
   public final Map<String, List<String>> headerFields;
   public final URL url;

   public final Object body;

   /*
    * if there were no IOExceptions, these come from the HttpURLConnection
    * otherwise, these are small negative numbers taken from Request.Milestone
    */
   public final int responseCode;
   public final String responseMessage;

   public final IOException ioException;


   /**
    * Constructor for a success
    * Reads various fields from the HttpURLConnection
    *
    * @param request       the HTTP Request that initiated this response
    * @param connection    non-null
    * @param body  may be null (came from connection.getInputStream())
    */
   public Response(Request request, HttpURLConnection connection, Object body) {
      this.request = request;
      contentType = connection.getContentType();
      headerFields = connection.getHeaderFields();
      url = connection.getURL();
      this.body = body != null ? body : "";

      // these local vars are because our fields are final, get assigned at the end
      int localResponseCode = Request.Milestone.Cleanup.errorCode();  // prepare for the worst
      String localResponseMessage = "";
      IOException localIOE = null;

      try {  // unlikely to fail because by now, presumably, somebody already read the responseData
         localResponseCode = connection.getResponseCode();
         localResponseMessage = connection.getResponseMessage();
      }
      catch (IOException ioe) {
         localIOE = ioe;
         localResponseMessage = ioe.getMessage();
      }

      responseCode = localResponseCode;
      responseMessage = localResponseMessage;
      ioException = localIOE;
   }



   /**
    * Constructor when there was an IOException
    *
    * @param request        the HTTP Request that initiated this response
    * @param connection     may be null if connection failed to open.
    * @param ioe            the IOException (non-null)
    * @param milestoneCode  small negative number taken from Request.Milestone
    */
   public Response(Request request, HttpURLConnection connection, IOException ioe, int milestoneCode) {
      this.request = request;
      if (connection != null) {
         contentType = connection.getContentType();
         headerFields = connection.getHeaderFields();
         url = connection.getURL();
      }
      else {
         contentType = null;
         headerFields = null;
         url = null;
      }

      body = "";
      responseCode = milestoneCode;
      responseMessage = ioe.getMessage();
      ioException = ioe;
   }


   /**
    * Content-Type
    *
    * @see HttpURLConnection#getContentType()
    * @return String, null if the connection never opened
    */
   public String getContentType() {
      return contentType;
   }

   /**
    * Return the Headers from the response
    *
    * @see HttpURLConnection#getHeaderFields()
    * @return a Map<String, List<String>>, null if the connection never opened
    */
   public Map<String, List<String>> getHeaderFields() {
      return headerFields;
   }

   /**
    * Returns the positive HTTP status code,
    * or else one of Request.Milestone.gerErrorCode() (which are negative)
    *
    * @see HttpURLConnection#getResponseCode()
    * @see Request.Milestone#errorCode()
    * @link http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
    * @return int HTTP code, e.g. 200
    */
   public int getResponseCode() {
      return responseCode;
   }


   /**
    * Message corresponding to the HTTP status code,
    * or else the message from the IOException
    *
    * @see HttpURLConnection#getResponseMessage()
    * @return e.g. "OK"
    */
   public String getResponseMessage() {
      return responseMessage;
   }



   /**
    * The response data (generally taken from HttpURLConnection.getInputStream())
    *
    * @return Object, never null, but may be ""
    */
   public Object getBody() {
      return body;
   }


   /**
    * The URL we talked to
    *
    * @see HttpURLConnection#getURL()
    * @return URL
    */
   public URL getURL() {
      return url;
   }


   /**
    * If there was an IOException, return it here
    *
    * @return  the IOException (or null)
    */
   public IOException getIOException() {
      return ioException;
   }


   /**
    * Which "hundreds" the response was in, e.g. good responses are 200
    * @return a multiple of 100
    */
   public int responseGroup() {
      return (responseCode / 100) * 100;
   }


   public String getRedirect() {
      if ((responseGroup() == 300) && request.method.followsRedirects()) {
         List<String> newLoc = headerFields.get("location");
         if (newLoc != null)
            return newLoc.get(0);
      }

      return null;
   }


   /**
    * If you like to throw the IOException
    *
    * @throws IOException
    */
   public void throwIOException() throws IOException {
      if (ioException != null)
         throw ioException;
   }


   /**
    * Get the request that triggered this response.
    * @return  never-null
    */
   public Request getRequest() {
      return request;
   }


   public boolean wasSuccessful() {
      return ioException == null; // TODO check HTTP codes...
   }



   /**
    * Nicely formatted summary of the response...
    */
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("" + responseCode + " " + responseMessage);
      sb.append("\nContent-Type: " + contentType + "\n");
      sb.append(formatHeaders(headerFields));
      sb.append("\n" + body);

      return sb.toString();
   }



   /**
    * Formats the HTTP headers nicely...
    * @param headers
    * @return StringBuilder, never null but may be ""
    */
   public static StringBuilder formatHeaders(Map<String, List<String>> headers) {
      StringBuilder sb = new StringBuilder();
      for (Entry<String, List<String>> entry : headers.entrySet()) {
         sb.append(entry.getKey());
         sb.append(" : ");
         List<String> value = entry.getValue();
         if (value.size() == 1)
            sb.append(value.get(0));
         else
            sb.append(value.toString());

         sb.append('\n');
      }

      return sb;
   }



}
