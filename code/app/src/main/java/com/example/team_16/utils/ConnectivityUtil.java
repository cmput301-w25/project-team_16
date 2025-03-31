package com.example.team_16.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Utility class to monitor and check network connectivity
 */
public class ConnectivityUtil {
    private static final String TAG = "ConnectivityUtil";

    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final ConnectivityCallback callback;
    private boolean isNetworkAvailable;

    /**
     * Interface for connectivity callbacks
     */
    public interface ConnectivityCallback {
        void onNetworkAvailable();

        void onNetworkLost();
    }

    /**
     * Constructor for ConnectivityUtil
     *
     * @param context  The application context
     * @param callback Callback to receive network state changes
     */
    public ConnectivityUtil(Context context, ConnectivityCallback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.isNetworkAvailable = isConnected();
    }

    /**
     * Start monitoring network changes
     */
    public void startMonitoring() {
        if (connectivityManager == null) return;

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Log.d(TAG, "Network connected");
                isNetworkAvailable = true;
                if (callback != null) {
                    callback.onNetworkAvailable();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                Log.d(TAG, "Network disconnected");
                isNetworkAvailable = false;
                if (callback != null) {
                    callback.onNetworkLost();
                }
            }
        };

        // Register the callback
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    /**
     * Stop monitoring network changes
     */
    public void stopMonitoring() {
        if (connectivityManager == null) return;

        try {
            connectivityManager.unregisterNetworkCallback(
                    new ConnectivityManager.NetworkCallback() {
                    }
            );
        } catch (IllegalArgumentException e) {
            // This exception is thrown if the callback was not registered
            Log.e(TAG, "Error unregistering network callback", e);
        }
    }

    /**
     * Check if the device is currently connected to a network
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            // For older Android versions
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

    /**
     * Get the current network state
     *
     * @return true if network is available, false otherwise
     */
    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }
}