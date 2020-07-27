package com.tripcube.unityhttplibrary;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


class TargetStatusPoller {
    //Server Keys
    private String accessKey;
    private String secretKey;

    private String targetId;
    private String url = "https://vws.vuforia.com";

    private TargetStatusListener targetStatusListener;

    private Timer timer;
    private PollerTask pollerTask;
    private boolean continuePolling = true;
    private float intervalInMinutes;

    public TargetStatusPoller(float interval_minutes, String target_id, String server_access_key, String server_secret_key, TargetStatusListener target_listener){
        intervalInMinutes   = interval_minutes;
        targetId 			= target_id;
        accessKey 			= server_access_key;
        secretKey 			= server_secret_key;
        targetStatusListener = target_listener;

        timer = new Timer();
    }

    public void startPolling() {
        System.out.println("Initiate polling for target: " +  targetId);

        pollerTask = new PollerTask();
        timer.schedule(pollerTask , (long)(intervalInMinutes * 60 * 1000));
    }

    public void stopPolling() {
        continuePolling = false;
        pollerTask.cancel();// Terminate pending tasks
        timer.cancel();//Terminate the timer thread
    }


    private void pollAgain() {
        if (continuePolling){
            System.out.println(".. polling again .. ");

            pollerTask = new PollerTask();
            timer.schedule(pollerTask , (long)(intervalInMinutes * 60 * 1000));
        }
    }


    class PollerTask extends TimerTask {
        public void run() {
            try {
                TargetState targetState = TargetState.createFromJSON( getTarget() );

                targetStatusListener.OnTargetStatusUpdate( targetState );

            } catch ( URISyntaxException | IOException e) {
                timer.cancel();
                e.printStackTrace();
                return;
            }

            pollAgain();
        }
    }


    private JSONObject getTarget() throws URISyntaxException, ClientProtocolException, IOException {

        System.out.println("Getting target record for " + targetId );

        HttpGet getRequest = new HttpGet();
        HttpClient client = new DefaultHttpClient();
        getRequest.setURI(new URI(url + "/targets/" + targetId));
        setHeaders(getRequest);

        HttpResponse response = client.execute(getRequest);

        JSONObject getTargetResultJSON = null;

        if( response != null ){

            try {

                getTargetResultJSON =  new JSONObject( EntityUtils.toString(response.getEntity()) );

            } catch (ParseException | JSONException e) {
                e.printStackTrace();
            }

        }

        System.out.println("getTarget() = "+ getTargetResultJSON.toString());

        return getTargetResultJSON;
    }


    private void setHeaders(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }
}
