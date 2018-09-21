package com.app.dlike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.app.dlike.activities.ProfileViewActivity;
import com.app.dlike.api.Steem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by hardip on 12/2/18.
 */

public class Tools {
    private static final String PREFERENCE_ACCESS_TOKEN = "access_token";
    private static final String PREFERENCE_USERNAME = "username";
    private static final String PREFERENCE_EXPIRES_AT = "expires_at";
    private static final String PREFERENCE_REFRESH_TOKEN = "refresh_token";

    public static OkHttpClient getClient(final Context context) {
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        okhttp3.Request.Builder ongoing = chain.request().newBuilder();
                        if (isLoggedIn(context)) {
                            ongoing.addHeader("Authorization", "Bearer " + getAccessToken(context));
                        }
                        return chain.proceed(ongoing.build());
                    }
                }).build();
    }

    public static Retrofit createRetrofit(Context context) {
        return new Retrofit.Builder()
                .baseUrl("https://v2.steemconnect.com/api/")
                .client(getClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Steem getSteem(Context context) {
        return createRetrofit(context).create(Steem.class);
    }

    public static boolean isLoggedIn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .contains(PREFERENCE_REFRESH_TOKEN);
    }

    public static boolean tokenExpired(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(PREFERENCE_EXPIRES_AT, System.currentTimeMillis()) <= System.currentTimeMillis();
    }

    public static String getAccessToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREFERENCE_ACCESS_TOKEN, "");
    }

    public static String getRefreshToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREFERENCE_REFRESH_TOKEN, "");
    }

    public static String getUsername(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREFERENCE_USERNAME, "");
    }

    public static void showProfile(final View view, final String username) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(view.getContext(), ProfileViewActivity.class);
                intent.putExtra(ProfileViewActivity.EXTRA_USERNAME, username);
                view.getContext().startActivity(intent);
            }
        });
    }

    public static void setAuthentication(Context context, String accessToken, String refreshToken, String username) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(PREFERENCE_ACCESS_TOKEN, accessToken);
        editor.putString(PREFERENCE_USERNAME, username);
        editor.putString(PREFERENCE_REFRESH_TOKEN, refreshToken);
        long expiry = 604800000;
        editor.putLong(PREFERENCE_EXPIRES_AT, System.currentTimeMillis() + expiry);
        editor.apply();
    }

    public static void clearAuthentication(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit()
                .remove(PREFERENCE_ACCESS_TOKEN)
                .remove(PREFERENCE_USERNAME)
                .remove(PREFERENCE_EXPIRES_AT)
                .remove(PREFERENCE_REFRESH_TOKEN)
                .apply();
    }

    public static void showToast(final Context context, final String str) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    public static boolean isNetAvail(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if ((connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
                || (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPermissionGranted(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static boolean isPermissionGrantedgLOC(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]"
                + "+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void openSettings(Activity context, String msg) {
        showToast(context, msg);

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public static String getStringFile(File f) {
        InputStream inputStream = null;
        String encodedFile = "", lastVal;
        try {
            inputStream = new FileInputStream(f.getAbsolutePath());

            byte[] buffer = new byte[10240];//specify the size to allow
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }
            output64.close();
            encodedFile = output.toString();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastVal = encodedFile;
        Log.d("lastv", lastVal);
        return lastVal;
    }

    public static boolean isGPSEnabled(Context context) {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        if (mLocationManager != null) {
            return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } else {
            return false;
        }
    }

    public static Calendar getDateFromMillis(String milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(milliSeconds));
        return calendar;
//        return formatter.format(calendar.getTime());
    }

    public static String getDateFromMillisString(String milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(milliSeconds));
//        return calendar;
        return formatter.format(calendar.getTime());
    }
}
