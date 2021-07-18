package com.edweng.adsquitsdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceObject;
import com.ironsource.mediationsdk.impressionData.ImpressionData;
import com.ironsource.mediationsdk.impressionData.ImpressionDataListener;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InitializationListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

import java.io.IOException;
import java.util.UUID;

import static android.provider.Settings.System.getString;

public class AdQuitManager implements InterstitialListener, ImpressionDataListener, Application.ActivityLifecycleCallbacks {
    private static AdQuitManager sharedAdQuitManager = null;
    private static final String TAG="AdQuitManager";
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    InterstitialListener interstitialListener;
    Activity activity;
    String appKey;
    String userId;

    private AdQuitManager() {
    }

    public static void init(Activity activity, String appKey) {
        IronSource.init(activity, appKey, (IronSource.AD_UNIT[])null);
        AdQuitManager.getInstance().activity = activity;
        AdQuitManager.getInstance().appKey = appKey;
        AdQuitManager.getInstance().userId = fetchUserId(activity);

        activity.getApplication().registerActivityLifecycleCallbacks(AdQuitManager.getInstance());

        Log.d(TAG, "User ID is " + AdQuitManager.getInstance().userId);
    }

    public static String fetchUserId(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String prefKey = activity.getResources().getString(R.string.user_id);
        String userId = sharedPref.getString(prefKey, null);

        if(userId == null) {
            userId = UUID.randomUUID().toString();
            editor.putString(prefKey, userId);
            editor.apply();
        }

        return userId;
    }

    public static void init(Activity activity, String appKey, IronSource.AD_UNIT... adUnits) {
        IronSourceObject.getInstance().init(activity, appKey, false, (InitializationListener)null, adUnits);
        AdQuitManager.getInstance().activity = activity;
        AdQuitManager.getInstance().appKey = appKey;
    }

    public static AdQuitManager getInstance() {
        if(sharedAdQuitManager == null) {
            sharedAdQuitManager = new AdQuitManager();

            IronSource.setInterstitialListener(sharedAdQuitManager);
            IronSource.addImpressionDataListener(sharedAdQuitManager);
        }

        return sharedAdQuitManager;
    }

    public static void setInterstitialListener(InterstitialListener listener) {
        AdQuitManager.getInstance().interstitialListener = listener;
    }

    @Override
    public void onInterstitialAdReady() {
        if(interstitialListener != null) interstitialListener.onInterstitialAdReady();
    }

    @Override
    public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
        if(interstitialListener != null) interstitialListener.onInterstitialAdLoadFailed(ironSourceError);
    }

    @Override
    public void onInterstitialAdOpened() {
        if(interstitialListener != null) interstitialListener.onInterstitialAdOpened();
    }

    @Override
    public void onInterstitialAdClosed() {
        if(interstitialListener != null) interstitialListener.onInterstitialAdClosed();
    }

    @Override
    public void onInterstitialAdShowSucceeded() {
        if(interstitialListener != null) interstitialListener.onInterstitialAdShowSucceeded();
    }

    @Override
    public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
        if(interstitialListener != null) interstitialListener.onInterstitialAdShowFailed(ironSourceError);
    }

    @Override
    public void onInterstitialAdClicked() {
        if(interstitialListener != null) interstitialListener.onInterstitialAdClicked();
    }

    @Override
    public void onImpressionSuccess (ImpressionData impressionData) {
        /* TO-DO: Send ping to backend with impression data. */
        Log.d(TAG, "onImpressionSuccess");
        AdQuitUtil.pingAdInfo(activity, impressionData);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            Log.e(TAG, "APP IN FOREGROUND " + activity.toString());
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            Log.e(TAG, "APP IN BACKGROUND " + activity.toString());
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
