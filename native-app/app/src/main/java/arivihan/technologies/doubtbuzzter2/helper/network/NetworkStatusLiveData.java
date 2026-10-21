package arivihan.technologies.doubtbuzzter2.helper.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public class NetworkStatusLiveData extends LiveData<Boolean> {

    private static final String TAG = NetworkStatusLiveData.class.getSimpleName();
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Context context;
    private boolean isInitialCheck = true;

    public NetworkStatusLiveData(Context context) {
        this.context = context.getApplicationContext(); // Use application context
        connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public NetworkStatusLiveData(Context context, boolean isInitialCheck) {
        this.context = context.getApplicationContext(); // Use application context
        connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.isInitialCheck = isInitialCheck;
    }

    @Override
    protected void onActive() {
        super.onActive();
        // Register callback when an observer becomes active
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Log.d(TAG, "Network onAvailable: " + network);
                updateNetworkStatus();
            }

            @Override
            public void onLost(@NonNull Network network) {
                Log.d(TAG, "Network onLost: " + network);
                updateNetworkStatus();
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                Log.d(TAG, "Network onCapabilitiesChanged: " + network);
                updateNetworkStatus();
            }
        };
        connectivityManager.registerNetworkCallback(request, networkCallback);

        // Initial check - checking internet initially
        if(isInitialCheck) {
            updateNetworkStatus();
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        // Unregister callback when no observers are active
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    public void updateNetworkStatus() {
        Network currentNetwork = connectivityManager.getActiveNetwork();
        boolean isConnected = false;
        if (currentNetwork != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(currentNetwork);
            isConnected = capabilities != null &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }
        postValue(isConnected);
    }
}
