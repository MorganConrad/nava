package com.flyingspaniel.nava.request;

import com.flyingspaniel.nava.hash.Hash;
import com.flyingspaniel.nava.hash.HashWrapper;
import com.flyingspaniel.nava.hash.To;
import com.flyingspaniel.nava.lib3rdparty.Base64Coder;
import com.flyingspaniel.nava.net.JRFC2617;
import com.flyingspaniel.nava.net.URLEncoding;
import com.flyingspaniel.nava.net.aws.AWSSignedRequestsHelper;
import com.flyingspaniel.nava.utils.Android2Lacks;
import com.flyingspaniel.nava.utils.EmittingCallbackFn;
import com.flyingspaniel.nava.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Morgan Conrad
 * @see <a href="http://opensource.org/licenses/MIT">This software is released under the MIT License</a>
 * @since Copyright (c) 2013 by Morgan Conrad
 */
public class Request extends EmittingCallbackFn<String,Response> implements Callable<Response> {

   public static final String FORM_DATA = "${FORM_DATA}";
   public static final String HEADER_DATA = "${HEADER_DATA}";
   public static final String QUERY_DATA = "${QUERY_DATA}";

   static final String ACCEPT_CHARSET = "Accept-Charset";
   static final String AUTHORIZATION = "Authorization";
   static final String BASIC_ = "Basic ";
   static final String DIGEST_ = "Digest ";


   protected boolean useStrictRFC3986 = false;
   protected boolean methodMustUpload = false;
   protected int maxRetries  = 3;

   protected HashWrapper.Linked options = new HashWrapper.Linked();

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
   protected Response response401 = null;

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

   @Override
   public Response callback(Exception ex, String urlOrOptions, Object... more) throws Exception {
      failSlow(ex, urlOrOptions, more);
      if (Utils.smellsLikeJSON(urlOrOptions)) {
         this.baseUrl = "";
         options(urlOrOptions);
      }
      else
         this.baseUrl= urlOrOptions;

      return call();
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


   public Request cookie(String value) {
      if (value != null)
         headersMMap.add("Cookie", value);

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


   public Response call() {
      int retryCount = 0;
      Response response = null;

      do {
         response = call1();
         response401 = null;
         if (response.getResponseCode() == 401) {
            response401 = response;
         }
      } while ((++retryCount < maxRetries) && (response401 != null));

      return response;
   }


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
    * Converts JSON String to a Map.  I'm using org.json.simple but subclasses could change
    * @param jsonString if null or empty return empty map
    * @return  Map, may be empty
    */
   public Map<String,Object> JSONtoMap(String jsonString) {
      if ((jsonString == null) || (jsonString.length() == 0))
         return Collections.emptyMap();

      return (JSONObject) JSONValue.parse(jsonString);
   }

   /**
    * Converts Object to a String for uploading
    * Subclasses might want to override and do JSON, XML, etc...
    */
   protected String objectToStringForUpload(Object object) {
      return To.stringOr(object, "");
   }


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
      if (method == null) {
         method = methodMustUpload ? HTTPMethod.POST : HTTPMethod.GET;
      }

      URL url = new URL(fullURL());
      return (HttpURLConnection) url.openConnection();
   }


   /**
    * Split off for testing, but a subclass may want to override
    * @return  full URL + and query/form stuff
    */
   protected String fullURL() throws IOException {

      Map awsMap = options.getMap("aws", false);
      if (awsMap != null)
         return doAWS(awsMap);

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



   protected HttpURLConnection prepareConnection(HttpURLConnection conn) {
      conn.setRequestProperty(ACCEPT_CHARSET, options.getString("acceptCharset", URLEncoding.UTF8_CHARSET));
      conn.setInstanceFollowRedirects(options.getBoolean("followRedirect", true));
      conn.setUseCaches(options.getBoolean("useCaches", true));

      doAuth(conn);
      headersMMap.applyHeadersToConnection(conn);

      int timeout = options.getInt("timeout", 2000);
      conn.setConnectTimeout(timeout);
      conn.setReadTimeout(options.getInt("readTimeout", timeout));

      conn.setDoInput(method.doesInput());
      conn.setDoOutput(method.doesOutput());

      // Since HTTPMethod is an enum with an acceptable protocol, this exception "cannot happen"
      try {
         conn.setRequestMethod(method.getMethodName());
      } catch (ProtocolException e) {
         throw new RuntimeException(e);  // cannot happen
      }


      return conn;
   }


   protected void doAuth(HttpURLConnection conn) {
      headersMMap.remove(AUTHORIZATION);   // remove???
      Map authMap = options.getMap("auth", false);
      if (authMap != null) {
         String user = Hash.getString(authMap, "user");
         String pass = Hash.getString(authMap, "pass");
         if (Hash.getBoolean(authMap, "sendImmediately", false)) {   // must be basic
            String encodedCredential = Base64Coder.encodeString(user + ":" + pass);
            headersMMap.add(AUTHORIZATION, BASIC_ + encodedCredential);
         }
         else if (response401 != null) {
            String challenge = response401.connection.getHeaderField("WWW-Authenticate");
            if (challenge.startsWith(BASIC_)) {
               String encodedCredential = Base64Coder.encodeString(user + ":" + pass);
               headersMMap.add(AUTHORIZATION, BASIC_ + encodedCredential);
            } else if (challenge.startsWith(DIGEST_)) {
               JRFC2617 authenticator = new JRFC2617(challenge);
               String path = conn.getURL().getPath();
               String encodedCredential = authenticator.createResponse(user, pass, null, method.name(), path);
               headersMMap.setFieldValuePairs(AUTHORIZATION, DIGEST_ + encodedCredential);
            } else
               throw new UnsupportedOperationException(challenge);
         }
      }
   }


   protected String doAWS(Map awsMap) throws IOException {
      // collect all query and form params
      HTTPMultiMap all = new HTTPMultiMap(false, queryMMap.getMap());
      all.addMultiMap(formDataMMap);
      Map<String, String> params = all.toSingleMap(true, true);
      try {
         AWSSignedRequestsHelper awsSigner = new AWSSignedRequestsHelper(Hash.getString(awsMap, "secret"));
         return awsSigner.sign(params, Hash.getString(awsMap, "key"), method.getMethodName());
      }
      catch (GeneralSecurityException gse) {
         throw Android2Lacks.IOException("AWSSignedRequestsHelper failure", gse);
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
            else {
               String s = objectToStringForUpload(uploadMe);
               doSubstituteMultiMapData(s);
               outputStream.write(s.getBytes());
            }

         }
         finally {
            Utils.closeQuietly(inputStream);
         }
      }

      // no upload data was provided, use form data
      else {
         String s = formDataMMap.toQueryString(getEncoding());
         outputStream.write(s.getBytes());
      }

      outputStream.flush();
   }



}
