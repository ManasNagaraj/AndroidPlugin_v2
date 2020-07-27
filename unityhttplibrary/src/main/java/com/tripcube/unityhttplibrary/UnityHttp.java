package com.tripcube.unityhttplibrary;

//json stuff for the request buidling
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class UnityHttp implements TargetStatusListener{
    public void LogNativeAndroidLogcatMessage(){
        Log.d("unity","native logcat message");

    }

    //Server Keys
    private String accessKey = "cdf37fbbc40961117cab23ee50cd4f7ed234cce4";
    private String secretKey = "328631ca96ab4eca03e289196c91d3e700580e4f";

    private String url = "https://vws.vuforia.com";
    private String targetName = "Dynamic Space" + System.currentTimeMillis() + "" ;


    private TargetStatusPoller targetStatusPoller;

    private final float pollingIntervalMinutes = 60;//poll at 1-hour interval

    private String postTarget(File file,String pin) throws URISyntaxException, ClientProtocolException, IOException, JSONException {
        HttpPost postRequest = new HttpPost();
        HttpClient client = new DefaultHttpClient();
        postRequest.setURI(new URI(url + "/targets"));
        JSONObject requestBody = new JSONObject();

        setRequestBody(requestBody,file,pin);
        postRequest.setEntity(new StringEntity(requestBody.toString()));
        setHeaders(postRequest); // Must be done after setting the body

        HttpResponse response = client.execute(postRequest);
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);

        JSONObject jobj = new JSONObject(responseBody);

        String uniqueTargetId = jobj.has("target_id") ? jobj.getString("target_id") : "";
        System.out.println("\nCreated target with id: " + uniqueTargetId);

        return uniqueTargetId;
    }

    private void setRequestBody(JSONObject requestBody,File file,String pin) throws IOException, JSONException {
        if(!file.exists()) {
            System.out.println("File location does not exist!");
            System.exit(1);
        }


        //bitmap conversion
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap resized = Bitmap.createScaledBitmap(bitmap,1080,810,true);
        resized.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        byte[] image = byteArrayOutputStream .toByteArray();


        byte[] meta_data = pin.getBytes("UTF-8");
        String encodedString = new String(Base64.encodeBase64(image));
        String metaData = new String(Base64.encodeBase64(meta_data));
        requestBody.put("name", targetName); // Mandatory
        requestBody.put("width", 0.32); // Mandatory
        requestBody.put("image", encodedString); // Mandatory
        // requestBody.put("active_flag", 1); // Optional
        requestBody.put("application_metadata", metaData); // Optional
    }

    private void setHeaders(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader(new BasicHeader("Content-Type", "application/json"));
        request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }

    /**
     * Posts a new target to the Cloud database;
     * then starts a periodic polling until 'status' of created target is reported as 'success'.
     */
    public int postTargetThenPollStatus(File file,String pin) {
        String createdTargetId = "";
        try {
            createdTargetId = postTarget(file,pin);
        } catch (URISyntaxException | IOException | JSONException e) {
            e.printStackTrace();
            return 0;
        }

        // Poll the target status until the 'status' is 'success'
        // The TargetState will be passed to the OnTargetStatusUpdate callback
        if (createdTargetId != null && !createdTargetId.isEmpty()) {
            targetStatusPoller = new TargetStatusPoller(pollingIntervalMinutes, createdTargetId, accessKey, secretKey, this );
            targetStatusPoller.startPolling();
            return 1;}
        return 0;

    }

    // Called with each update of the target status received by the TargetStatusPoller
    @Override
    public void OnTargetStatusUpdate(TargetState target_state) {
        if (target_state.hasState) {

            String status = target_state.getStatus();

            System.out.println("Target status is: " + (status != null ? status : "unknown"));

            if (target_state.getActiveFlag() == true && "success".equalsIgnoreCase(status)) {

                targetStatusPoller.stopPolling();

                System.out.println("Target is now in 'success' status");
            }
        }
    }


    public static void main(String[] args) throws URISyntaxException, ClientProtocolException, IOException, JSONException {
        //PostNewTarget p = new PostNewTarget();
        //p.postTargetThenPollStatus();
    }

}
