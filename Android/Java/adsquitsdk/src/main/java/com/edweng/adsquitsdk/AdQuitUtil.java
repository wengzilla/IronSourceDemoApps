package com.edweng.adsquitsdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.impressionData.ImpressionData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class AdQuitUtil {
    public static final String URL = "https://ads-quit.herokuapp.com/logEvent";
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private static final String TAG="AdQuitUtil";

    public static JSONObject getEventParams(Context context) throws PackageManager.NameNotFoundException, JSONException {
        JSONObject dictionary = new JSONObject();
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

        dictionary.put("package_name", pInfo.packageName);
        dictionary.put("platform", "android");
        dictionary.put("os_version", Build.VERSION.SDK_INT);
        dictionary.put("app_version", pInfo.versionCode); /* Should we also capture version name? */
        dictionary.put("device_id", IronSource.getAdvertiserId(context));
        dictionary.put("uuid", UUID.randomUUID().toString());
        dictionary.put("user_id", "1");
        dictionary.put("event_name", "ad_impression_data");

        return dictionary;
    }

    public static void pingAdInfo(Context context, ImpressionData impressionData) throws IOException {

        // TODO: Refactor AsyncTask out of this static method to make it reusable...

        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
            JSONObject dictionary;

            @Override
            protected String doInBackground(String... params) {
                try {
                    dictionary = getEventParams(context);
                    dictionary.put("data", impressionData.getAllData());
                } catch (JSONException | PackageManager.NameNotFoundException e) {
                    Log.e(TAG, e.toString());
                }

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(JSON, dictionary.toString());
                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) Log.e(TAG, "Request was unsuccessful");
                    Log.d(TAG, response.body().string());
                } catch(IOException e) {
                    Log.e(TAG, e.toString());
                }

                return null;
            }
        };

        asyncTask.execute();
    }
}