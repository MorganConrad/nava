package com.flyingspaniel.nava.request;

import com.flyingspaniel.nava.emit.Emitter;
import com.flyingspaniel.nava.hash.Hash;
import com.flyingspaniel.nava.hash.HashWrapper;
import com.flyingspaniel.nava.lib3rdparty.Base64Coder;
import com.flyingspaniel.nava.net.URLEncoding;
import com.flyingspaniel.nava.net.aws.AWSSignedRequestsHelper;
import com.flyingspaniel.nava.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class Request extends Emitter implements Callable<Response> {

   public static final String FORM_DATA = "${FORM_DATA}";
   public static final String HEADER_DATA = "${HEADER_DATA}";
   public static final String QUERY_DATA = "${QUERY_DATA}";

   static final String ACCEPT_CHARSET = "Accept-Charset";
   static final String AUTHORIZATION = "Authorization";

   /**
    * These defaults should be reasonable, but are changeable via setters
    *
    * @see #acceptCharset(String)
    * @see #uploadSubstitution(boolean)
    * @see #useStrictRFC3986(boolean)
    */
//   protected String acceptCharset = URLEncoding.UTF8_CHARSET;
//   protected boolean uploadSubstitution = true;
     protected boolean useStrictRFC3986 = false;
   protected boolean methodMustUpload = false;
//   protected int maxRedirects  = 10;

   protected HashWrapper.Linked options = new HashWrapper.Linked();

   // TODO
//   protected List uploadList;

  // protected Map<String,String> awsMap = null;

   /**
    * Three possible multimaps used for the query, header, and formData
    *
    * @see #form(Object...)
    * @see #headers(Object...)
    * @see #query(Object...)
    */
   protected final HTTPMultiMap queryMMap;
   protected final HTTPMultiMap headersMMap;  // a.k.a. request parameters
   protected final HTTPMultiMap formDataMMap;

   protected String baseUrl;
   protected HTTPMethod method;

   protected OutputStream pipeOut = null;
   protected InputStream pipeIn = null;
   protected Object uploadMe = null;

   protected OutputStream pipeTo = null;

   protected volatile Milestone attempting = Milestone.None;

   /**
    * Enum for the steps in the HTTPRequest.call() method, used for error-tracking...
    */
   public enum Milestone {

      None,
      OpenConnection,
      Connect,
      Upload,
      Download,
      Cleanup,
      AllDone;

      /**
       * Direct use of ordinal() is discouraged
       * Use this method to get the error code in case things change in the future
       *
       * @return usually a negative number
       */
      public int errorCode() {
         return -this.ordinal();
      }
   };


   /**
    * Full constructor
    * @param baseUrl  may include a query part but you have to encode it yourself
    * @param method
    * @param options  may be null
    */
   public Request(String baseUrl, HTTPMethod method, Map<String,Object> options) {
      this.baseUrl = baseUrl;
      this.method = method;

      queryMMap = new HTTPMultiMap();
      headersMMap = new HTTPMultiMap();
      formDataMMap = new HTTPMultiMap();

      options(options);
   }


   public Request(String baseUrl, HTTPMethod method) {
      this(baseUrl, method, null);
   }

   public Request(String baseUrl, Map<String,Object> options){
      this(baseUrl, null, options);
   }

   public Request(String urlOrOptions){
      this(urlOrOptions, null, null);
      if (Utils.smellsLikeJSON(urlOrOptions)) {
         this.baseUrl = "";
         options(urlOrOptions);
      }
   }

   public Request() {
      this("", null, null);
   }


   /**
    * Set all options from a Map.
    * @param optionMap  if null nothing happens
    * @return   this
    */
   public Request options(Map<String,Object> optionMap) {
      if (optionMap != null) {

         form(Hash.getMap(optionMap, "form", false));
         headers(Hash.getMap(optionMap, "headers", false));
         query(Hash.getMap(optionMap, "query", false));
//         auth(getMap(optionMap, "auth"));
//         oauth(getMap(optionMap, "oauth"));

         String methodS = Hash.getString(optionMap, "method");
         if (methodS != null)
            this.method = HTTPMethod.valueOf(methodS);
         useStrictRFC3986(Hash.getBoolean(optionMap, "useStrictRFC3986", useStrictRFC3986));

         options.putAll(optionMap);
      }

      return this;
   }

   /**
    * Set all options based on a JSON String
    * @param  jsonString if null or empty nothing happens
    * @return this
    */
   public Request options(String jsonString) {
      return options(JSONtoMap(jsonString));
   }


   public Request acceptCharset(String charset) {
      options.put("acceptCharset", charset);
      return this;
   }


   public Request auth(String user, String pass, boolean sendImmediately) {
      if (user == null)
         options.remove("auth");
      else {
         Map authMap = options.getMap("auth", true);
         authMap.put("user", user);
         authMap.put("pass", pass);
         authMap.put("sendImmediately", sendImmediately);
      }

      return this;
   }



   /**
    * Adds form data
    * @param fieldValuePairs  should be an even number of field/value pairs
    * @return  this
    */
   public Request form(Object...fieldValuePairs) {
      this.formDataMMap.addFieldValuePairs(fieldValuePairs);
      return this;
   }

   /**
    * Adds form data from a map
    * @param map may be null or empty
    * @return  this
    */
   public Request form(Map<String,Object> map) {
      this.formDataMMap.addSingleMap(map);
      return this;
   }

   /**
    * Adds form data from a JSON String
    * @param  jsonString   if null or empty nothing happens
    * @return this
    */
   public Request form(String jsonString) {
       return form(JSONtoMap(jsonString));
   }

   /**
    * Sets form data, replacing any previous data for th field
    * @param fieldValuePairs  if null or [], deletes all previous form data
    * @return this
    */
   public Request setForm(Object...fieldValuePairs) {
      if (Utils.nullOrEmpty(fieldValuePairs))
         formDataMMap.getMap().clear();
      else
         formDataMMap.setFieldValuePairs(fieldValuePairs);

      return this;
   }


   /**
    * Adds header data
    * @param fieldValuePairs
    * @return this
    */
   public Request headers(Object...fieldValuePairs) {
      this.headersMMap.addFieldValuePairs(fieldValuePairs);
      return this;
   }

   /**
    * Adds header data from a Map
    * @param map non-null, may be empty
    * @return this
    */
   public Request headers(Map<String,Object> map) {
      this.headersMMap.addSingleMap(map);
      return this;
   }

   /**
    * Adds header data from a JSON String
    * @param jsonString  if null or empty nothing happens
    * @return this
    */
   public Request headers(String jsonString) {
      return headers(JSONtoMap(jsonString));
   }

   /**
    * Replaces all previous header data for the given key value pairs
    * @param fieldValuePairs  if empty, clears all headers for all keys
    * @return this
    */
   public Request setHeaders(Object...fieldValuePairs) {
      if (Utils.nullOrEmpty(fieldValuePairs))
         headersMMap.getMap().clear();
      else
         headersMMap.setFieldValuePairs(fieldValuePairs);

      return this;
   }


   public Request method(HTTPMethod method) {
      this.method = method;
      return this;
   }


    // not implemented yet
   public Request oauth(String jsonString) {
      options.remove("oauth");
      if (jsonString != null)
         options.put("oauth", JSONtoMap(jsonString));
      return this;
   }

   /**
    * Sets an input stream for uploading data
    * @param  in
    * @return this
    */
   public Request pipe(InputStream in) {
      checkMethodMustUpload();
      pipeIn = in;
      return this;
   }

   /**
    * Sets an outputstream for downloading
    * @param  out
    * @return this
    */
   public Request pipe(OutputStream out) {
      if ((method != null) && !method.doesInput())
         throw new IllegalStateException(method + " does not do input");

      pipeOut = out;
      return this;
   }

   /**
    * Adds field value pairs to the query
    * @param fieldValuePairs
    * @return this
    */
   public Request query(Object...fieldValuePairs) {
      this.queryMMap.addFieldValuePairs(fieldValuePairs);
      return this;
   }

   public Request query(Map<String,Object> map) {
      this.queryMMap.addSingleMap(map);
      return this;
   }

   /**
    * Adds query data from a JSON String
    * @param jsonString  if null or empty nothing happens
    * @return this
    */
   public Request query(String jsonString) {
      return query(JSONtoMap(jsonString));
   }


   /**
    * Replaces all previous query data for the given key value pairs
    * @param fieldValuePairs  if empty, clears all query data for all keys
    * @return this
    */
   public Request setQuery(Object...fieldValuePairs) {
      if (fieldValuePairs.length == 0)
         queryMMap.getMap().clear();
      else
         queryMMap.setFieldValuePairs(fieldValuePairs);

      return this;
   }


   /**
    * Sets data to be uploaded: a File or InputStream,
    * a Map which will get converted to JSON
    * or anything else will be sent as a String
    * @param toBeUploaded
    * @return this
    */
   public Request upload(Object toBeUploaded) {
      checkMethodMustUpload();
      uploadMe = toBeUploaded;
      return this;
   }


   /**
    * If uploadSubstitution is activated, text like ${FORM_DATA} in the upload data
    * will be replaced with values from the FORM
    * @param b
    * @return this
    */
   public Request uploadSubstitution(boolean b) {
      options.put("uploadSubstitution", Boolean.valueOf(b));
      return this;
   }


   public Request useStrictRFC3986(boolean b) {
      this.useStrictRFC3986 = b;
      return this;
   }


   public Response call()  {

      int redirectCountdown = options.getInt("maxRedirects", 10);
      Response response;

      do {
         response = call1();
         String redirect = response.getRedirect();
         if (redirect != null)
            this.baseUrl = redirect;  // ???
      }
      while (--redirectCountdown > 0);

      return response;
   }


   // one try, may return a redirect...
   public Response call1()  {

      HttpURLConnection conn = null;
      OutputStream connsOutputStream = null;
      InputStream connsInputStream = null;
      noteMilestone(Milestone.OpenConnection);

      try {
         conn = openConnection();
         noteMilestone(Milestone.Connect);
         prepareConnection(conn);
         conn.connect();

         noteMilestone(Milestone.Upload);
         if (method.doesOutput()) {
            connsOutputStream = conn.getOutputStream();
            doUpload(connsOutputStream);
         }

         noteMilestone(Milestone.Download);
         Object responseData = "";
         if (method.doesInput()) {
            connsInputStream = conn.getInputStream();

            if (pipeTo == null)
               pipeTo = new ByteArrayOutputStream();

            copy(connsInputStream, pipeTo, true);
            responseData = pipeTo.toString();
         }

         noteMilestone(Milestone.Cleanup);

         Response response = new Response(this, conn, responseData);
         noteMilestone(Milestone.AllDone);
         return response;
      }

      catch (IOException ioe) {
         return new Response(this, conn, ioe, attempting.errorCode());
      }

      finally {
         Utils.closeQuietly(connsInputStream);
         Utils.closeQuietly(connsOutputStream);
         if (conn != null)
            conn.disconnect();
      }
   }


   /**
    * Converts JSON String to a Map
    * @param jsonString if null or empty return empty map
    * @return  Map, may be empty
    */
   public static Map<String,Object> JSONtoMap(String jsonString) {
      if ((jsonString == null) || (jsonString.length() == 0))
         return Collections.emptyMap();

      return (JSONObject) JSONValue.parse(jsonString);
   }


//   protected Map<String,?> getMap(Map options, String key) {
//      Object value = options.get(key);
//      return (Map<String, ?>)value;
//   }


   protected long copy(InputStream input, OutputStream output, boolean emit) throws IOException {
      byte[] buffer = new byte[4096];
      long count = 0;
      int n = 0;
      while (-1 != (n = input.read(buffer))) {
         output.write(buffer, 0, n);
         count += n;

         if (emit)
            emit("data", buffer);
      }

      return count;
   }



   /**
    * Useful for POST - replaces $FORMDATA, $HEADERS or $QUERY in the upload string
    * with the corresponding contents of the respective MultiMaps.
    *
    * @return  the String that will be uploaded
    * @see #uploadSubstitution
    * @see #FORM_DATA
    * @see #HEADER_DATA
    * @see #QUERY_DATA
    */
   protected String doSubstituteMultiMapData(String upload) {
      boolean doSubstitute = (options.getBoolean("uploadSubstitution", true));
      if (!doSubstitute)
         return upload;

      if (upload.contains(FORM_DATA))
         upload = upload.replace(FORM_DATA, formDataMMap.toQueryString(getEncoding()));
      if (upload.contains(HEADER_DATA))
         upload = upload.replace(HEADER_DATA, headersMMap.toQueryString(getEncoding()));
      if (upload.contains(QUERY_DATA))
         upload = upload.replace(QUERY_DATA, queryMMap.toQueryString(getEncoding()));

      return upload;
   }

   protected URLEncoding getEncoding() {
      return URLEncoding.Impl.getInstance(true, useStrictRFC3986);
   }


   /**
    * Subclasses who wish to log or send progress can override this
    *
    * @param attempting
    */
   protected void noteMilestone(Milestone attempting) {
      this.attempting = attempting;
   }

   /**
    * Split off for testing, but a subclass may want to override
    * @return
    * @throws IOException
    */
   protected HttpURLConnection openConnection() throws IOException {
      URL url = new URL(fullURL());
      return (HttpURLConnection) url.openConnection();
   }


   /**
    * Split off for testing, but a subclass may want to override
    * @return  full URL + and query/form stuff
    */
   protected String fullURL() {
      String query = queryMMap.toQueryString(getEncoding());
      String form = (method == HTTPMethod.GET) ? formDataMMap.toQueryString(getEncoding()) : "";

      if (query.length() + form.length() == 0)
         return baseUrl;

      StringBuilder sb = new StringBuilder(this.baseUrl);
      String leadWith = this.baseUrl.contains("?") ? "&" : "?";

      if (query.length() > 0) {
         sb.append(leadWith + query);
         leadWith = "&";
      }

      if (form.length() > 0)
         sb.append(leadWith + form);

      return sb.toString();
   }
   /**
    * Split off mainly for testing, but a subclass might want to override
    */
   protected HttpURLConnection prepareConnection(HttpURLConnection conn) {
      conn.setRequestProperty(ACCEPT_CHARSET, options.getString(ACCEPT_CHARSET, URLEncoding.UTF8_CHARSET));
      doAuth();
      headersMMap.applyHeadersToConnection(conn);

      int timeout = options.getInt("timeout", 2000);
      conn.setConnectTimeout(timeout);
      conn.setReadTimeout(options.getInt("readTimeout", timeout));

      if (method == null) {
         method = methodMustUpload ? HTTPMethod.POST : HTTPMethod.GET;
      }
      conn.setDoInput(method.doesInput());
      conn.setDoOutput(method.doesOutput());

      // Since HTTPMethod is an enum with an acceptable protocol, this exception "cannot happen"
      try {
         conn.setRequestMethod(method.getMethodName());
      } catch (ProtocolException e) {
         throw new RuntimeException(e);  // cannot happen
      }

//      if (awsMap != null) {
//         AWSSignedRequestsHelper helper = new AWSSignedRequestsHelper(awsMap.get("secret"));
//         Map<String, String> paramsForAWS = new HashMap<String,String>();
//         paramsForAWS.putAll(queryMMap.toSingleMap(true, true));
//         if (this.method == HTTPMethod.GET)
//            paramsForAWS.putAll(formDataMMap.toSingleMap(true, true));
//
//         helper.sign(paramsForAWS, awsMap.get("key"));
//      }
      return conn;
   }

   protected void doAuth() {
      headersMMap.remove(AUTHORIZATION);
      Map authMap = options.getMap("auth", false);
      if (authMap != null) {
         String user = Hash.getString(authMap, "user");
         String pass = Hash.getString(authMap, "pass");
         String encodedCredential = Base64Coder.encodeString(user + ":" + pass);
         headersMMap.add(AUTHORIZATION, "BASIC " + encodedCredential);
      }
   }


   protected void checkMethodMustUpload() {
      if ((method != null) && !method.doesOutput())
         throw new IllegalStateException(method + " cannot perform any uploads.");
      methodMustUpload = true;
   }

   protected void doUpload(OutputStream outputStream) throws IOException {

      if (pipeIn != null) {
         copy(pipeIn, outputStream, false);
      }

      else if (uploadMe != null) {
         InputStream inputStream = null;
         try {
            if (uploadMe instanceof File) {
               inputStream = new FileInputStream((File)uploadMe);
               copy(inputStream, outputStream, false);
            }
            if (uploadMe instanceof InputStream) {  // should probably use pipe() instead
               copy( (InputStream)uploadMe, outputStream, false);
            }
            else if (uploadMe instanceof Map) {
               JSONObject jsonObject = new  JSONObject((Map)uploadMe);
               String s = jsonObject.toJSONString();
               outputStream.write(s.getBytes());
            }
            else {
               String s = uploadMe.toString();
               doSubstituteMultiMapData(s);
               outputStream.write(s.getBytes());
            }

         }
         finally {
            Utils.closeQuietly(inputStream);
         }
      }

      else if (this.method.doesOutput()) {
         String s = formDataMMap.toQueryString(getEncoding());
         outputStream.write(s.getBytes());
      }

      outputStream.flush();
   }



}
