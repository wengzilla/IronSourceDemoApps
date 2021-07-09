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

    private static AdQuitUtil sharedAdQuitUtil = null;

    public static AdQuitUtil getInstance() {
        if(sharedAdQuitUtil == null) {
            sharedAdQuitUtil = new AdQuitUtil();
        }
        return sharedAdQuitUtil;
    }

    public class PingTask extends AsyncTask<PingTask.DataParams, Void, String> {
        public class DataParams {
            public String eventName;
            public Context context;
            public JSONObject withData;
        }

        @Override
        protected String doInBackground(DataParams... params) {
            OkHttpClient client = new OkHttpClient();
            JSONObject dictionary = new JSONObject();
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

                dictionary.put("package_name", pInfo.packageName);
                dictionary.put("os_version", Build.VERSION.SDK_INT);
                dictionary.put("app_version", pInfo.versionCode); /* Should we also capture version name? */
                dictionary.put("device_id", IronSource.getAdvertiserId(context));
                dictionary.put("uuid", UUID.randomUUID().toString());
                dictionary.put("user_id", "1");
                dictionary.put("event_name", eventName);
                dictionary.put("data", withData);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.toString());
            }

            RequestBody body = RequestBody.create(JSON, dictionary.toString());
            Request request = new Request.Builder()
                    .url(URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) Log.e(TAG, "Request was unsuccessful");
                System.out.println(response.body().string());
//            Log.d(TAG, response.body().string());
            } catch(IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public static void pingAdInfo(Context context, ImpressionData impressionData) throws IOException {
        PingTask.DataParams params = new PingTask.DataParams();

        AdQuitUtil.PingTask.execute()
    }
}
