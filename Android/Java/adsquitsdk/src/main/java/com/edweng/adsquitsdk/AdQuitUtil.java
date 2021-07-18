package com.edweng.adsquitsdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.impressionData.ImpressionData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public static JSONObject getEventParams(Activity activity, String event_name) throws PackageManager.NameNotFoundException, JSONException {
        JSONObject dictionary = new JSONObject();
        PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);

        dictionary.put("package_name", pInfo.packageName);
        dictionary.put("platform", "android");
        dictionary.put("os_version", Build.VERSION.SDK_INT);
        dictionary.put("app_version", pInfo.versionCode); /* Should we also capture version name? */
        dictionary.put("device_id", IronSource.getAdvertiserId(activity.getApplicationContext()));
        dictionary.put("uuid", null);
        dictionary.put("user_id", fetchUserId(activity));
        dictionary.put("event_name", event_name);

        return dictionary;
    }

    public static String fetchUserId(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        String prefKey = activity.getResources().getString(R.string.user_id);
        String userId = sharedPref.getString(prefKey, null);
        return userId;
    }

    public static void pingAdInfo(Activity activity, ImpressionData impressionData) {
        ping(activity, "ad_impression_data", impressionData.getAllData());
    }

    public static void ping(Activity activity, String event_name, JSONObject data) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject dictionary = null;
                try {
                    dictionary = getEventParams(activity, event_name);
                    if (data != null) {
                        dictionary.put("data", data);
                    }
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
            }
        });

    }

    public static void pingAppOpen(Activity activity) {
        ping(activity, "app_open", null);
    }

    public static void pingAppBackground(Activity activity) {
        ping(activity, "app_background", null);
    }

    public static void pingAppQuit(Activity activity) {
        ping(activity, "app_quit", null);
    }
}