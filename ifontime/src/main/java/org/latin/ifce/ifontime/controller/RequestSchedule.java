package org.latin.ifce.ifontime.controller;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by atila on 06/08/13.
 */
public class RequestSchedule {

   private InputStream stream;
   private JSONObject obj;
   private String answer;
   // TODO: colocar URL em SharedPreferences
   private static final String URL = "http://10.50.40.248:8080/get?";

   public JSONObject getAnswer(String hash) throws Exception {
      send(hash);
      readAnswer();
      parseJSON();
      return obj;
   }

   private void parseJSON() throws JSONException {
      try {
         obj = new JSONObject(answer);
      } catch (JSONException e) {
         Log.e("RequestSchedule", "Error while parsing json", e);
         throw e;
      }
   }

   private void readAnswer() throws IOException {
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
         StringBuilder builder = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) {
            builder.append(line);
         }
         stream.close();
         answer = builder.toString();
      } catch (IOException e) {
         Log.e("RequestSchedule", "Error while reading answer", e);
         throw e;
      }
   }

   private void send(String hash) throws Exception {
      try {
         HttpParams httpparams = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(httpparams, 30000);
         HttpConnectionParams.setSoTimeout(httpparams, 1);
         DefaultHttpClient httpClient = new DefaultHttpClient(httpparams);
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair("hash", hash));
         String paramString = URLEncodedUtils.format(params, "utf-8");

         HttpGet get = new HttpGet(URL + paramString);
         Log.d("[DEBUG]", get.getURI().toURL().toString());

         HttpResponse response = httpClient.execute(get);
         HttpEntity entity = response.getEntity();
         stream = entity.getContent();
      } catch (HttpHostConnectException e) {
         throw e;
      } catch (Exception e) {
         Log.e("RequestSchedule", "Error while sending requisition", e);
         throw e;
      }
   }
}
