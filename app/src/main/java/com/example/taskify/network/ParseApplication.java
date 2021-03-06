package com.example.taskify.network;

import android.app.Application;

import com.example.taskify.BuildConfig;
import com.example.taskify.R;
import com.example.taskify.models.Alarm;
import com.example.taskify.models.Reward;
import com.example.taskify.models.Task;
import com.example.taskify.models.TaskifyUser;
import com.facebook.appevents.AppEventsLogger;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Use for troubleshooting -- remove this line for production
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Use for monitoring Parse OkHttp traffic
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See https://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        ParseObject.registerSubclass(Reward.class);
        ParseObject.registerSubclass(Task.class);
        ParseUser.registerSubclass(TaskifyUser.class);
        ParseObject.registerSubclass(Alarm.class);

        // set applicationId, and server server based on the values in the back4app settings.
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(BuildConfig.BACK4APP_APP_ID) // should correspond to Application Id env variable
                .clientKey(BuildConfig.BACK4APP_CLIENT_KEY)  // should correspond to Client key env variable
                .server(getString(R.string.back4app_server_url)).build());
        ParseUser.enableRevocableSessionInBackground();

        ParseFacebookUtils.initialize(this);
        AppEventsLogger.activateApp(this);
    }
}

