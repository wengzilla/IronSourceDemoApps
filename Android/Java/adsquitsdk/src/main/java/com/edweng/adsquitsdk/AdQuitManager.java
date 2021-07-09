package com.edweng.adsquitsdk;

import android.app.Activity;
import android.util.Log;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceObject;
import com.ironsource.mediationsdk.impressionData.ImpressionData;
import com.ironsource.mediationsdk.impressionData.ImpressionDataListener;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InitializationListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

import java.io.IOException;

public class AdQuitManager implements InterstitialListener, ImpressionDataListener {
    private static AdQuitManager sharedAdQuitManager = null;
    private static final String TAG="AdQuitManager";
    InterstitialListener interstitialListener;
    Activity activity;
    String appKey;

    private AdQuitManager() {
    }

    public static void init(Activity activity, String appKey) {
        IronSource.init(activity, appKey, (IronSource.AD_UNIT[])null);
        AdQuitManager.getInstance().activity = activity;
        AdQuitManager.getInstance().appKey = appKey;
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
        try {
            AdQuitUtil.pingAdInfo(activity.getApplicationContext(), impressionData);
        } catch(IOException e) {
            Log.e(TAG, e.toString());
        }
    }

}
