package com.funplus.sdk.facebookdata;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.funplus.sdk.FunPlusData;
import com.funplus.sdk.FunPlusSDK;

import org.json.JSONException;
import org.json.JSONObject;

public class FacebookData implements FunPlusData.EventTracedListener {
    private static final String LOG_TAG = "FacebookData";

    private static FacebookData instance;

    @NonNull private final AppEventsLogger logger;

    public static void install(@NonNull Application application, @NonNull FunPlusData dataTracer) {
        if (instance == null) {
            FacebookSdk.sdkInitialize(application.getApplicationContext());
            AppEventsLogger.activateApp(application);
            instance = new FacebookData(application);
            dataTracer.registerEventTracedListener(instance);

            Log.i(LOG_TAG, "FacebookData ready to work");
        } else {
            Log.w(LOG_TAG, "FunPlusData has been installed, there's no need to install it again");
        }
    }

    private FacebookData(@NonNull Context context) {
        logger = AppEventsLogger.newLogger(context);
    }

    @Override
    public void onKpiEventTraced(JSONObject dataEvent) {
        convertDataEventToFacebookEvent(dataEvent);

        try {
            Log.i(LOG_TAG, "Received KPI event: " + dataEvent.getString("event"));
        } catch (JSONException e) {
            // do nothing
        }
    }

    @Override
    public void onCustomEventTraced(@NonNull JSONObject dataEvent) {
        convertDataEventToFacebookEvent(dataEvent);

        try {
            Log.i(LOG_TAG, "Received custom event: " + dataEvent.getString("event"));
        } catch (JSONException e) {
            // do nothing
        }
    }

    private void convertDataEventToFacebookEvent(JSONObject dataEvent) {
        try {
            String eventName = dataEvent.getString("event");
            JSONObject properties = dataEvent.getJSONObject("properties");

            switch (eventName) {
                case "tutorial":
                    logCompletedTutorial(properties.getInt("success"), properties.getString("content_id"));
                    break;
                case "level":
                    logAchievedLevel(properties.getString("level"));
                    break;
                default:
                    // Do nothing.
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to convert data event to Facebook event: " + dataEvent);
        }
    }

    private void logCompletedTutorial(int success, String contentId) {
        Bundle parameters = new Bundle();
        parameters.putInt(AppEventsConstants.EVENT_PARAM_SUCCESS, success);
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId);
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL, parameters);
    }

    private void logAchievedLevel(@Nullable String level) {
        Bundle parameters = new Bundle();
        parameters.putString(AppEventsConstants.EVENT_PARAM_LEVEL, level);
        logger.logEvent(AppEventsConstants.EVENT_NAME_ACHIEVED_LEVEL, parameters);
    }
}
