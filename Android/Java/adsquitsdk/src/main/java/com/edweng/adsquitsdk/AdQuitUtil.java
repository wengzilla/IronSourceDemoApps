package com.edweng.adsquitsdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

    public static JSONObject getEventParams(Activity activity) throws PackageManager.NameNotFoundException, JSONException {
        JSONObject dictionary = new JSONObject();
        PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);

        dictionary.put("package_name", pInfo.packageName);
        dictionary.put("platform", "android");
        dictionary.put("os_version", Build.VERSION.SDK_INT);
        dictionary.put("app_version", pInfo.versionCode); /* Should we also capture version name? */
        dictionary.put("device_id", IronSource.getAdvertiserId(activity.getApplicationContext()));
        dictionary.put("uuid", null);
        dictionary.put("user_id", fetchUserId(activity));
        dictionary.put("event_name", "ad_impression_data");

        return dictionary;
    }

    public static String fetchUserId(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        String prefKey = activity.getResources().getString(R.string.user_id);
        String userId = sharedPref.getString(prefKey, null);
        return userId;
    }

    public static void pingAdInfo(Activity activity, ImpressionData impressionData) throws IOException {

        // TODO: Refactor AsyncTask out of this static method to make it reusable...

        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
            JSONObject dictionary;

            @Override
            protected String doInBackground(String... params) {
                try {
                    dictionary = getEventParams(activity);
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

    public static void pingAppOpen(Context context, ImpressionData impressionData) {

    }

    public static void pingAppBackground(Context context, ImpressionData impressionData) {

    }

    public static void pingAppQuit(Context context, ImpressionData impressionData) {

    }
}