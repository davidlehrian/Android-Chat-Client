package com.leridiandynamics.chatclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import com.leridiandynamics.chatclient.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    NsdManager.DiscoveryListener discoveryListener;
    NsdManager nsdManager=null;
    NsdManager.ResolveListener resolveListener;
    NsdServiceInfo mService;
    String SERVICE_TYPE="_ChatServer._tcp.";
    String serviceName="ChatServer";

    OutputStream outputStream;
    TextView outputTextView;
    ScrollView scrollView;
    EditText inputEditText;
    InputStream input;
    byte[] readdata = new byte[256];
    Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.outputTextView = binding.outputTextView;
        this.scrollView = binding.scrollView;
        this.inputEditText = binding.inputEditText;
        inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendText();
                    return true;
                }
                return false;
            }
        });

        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendText();
            }
        });
        this.mainHandler = new Handler(this.getMainLooper());

        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                Log.e(TAG, "Resolve failed: " + errorCode);
            }
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(serviceName)) {
                    Log.d(TAG, "Same IP. " + (Build.VERSION.SDK_INT >= 34 ? serviceInfo.getHostAddresses() : serviceInfo.getHost()));
                    mService = serviceInfo;
                    nsdManager.stopServiceDiscovery(discoveryListener);
                    try{
                        Socket socket = new Socket(Build.VERSION.SDK_INT >= 34 ? mService.getHostAddresses().get(0) : mService.getHost(),8584);
                        outputStream = socket.getOutputStream();
                        InputStream input = socket.getInputStream();
                        new Thread(() -> {
                            while(true) {
                                try {
                                    int lenRead = input.read(readdata);
                                    if (lenRead >0) {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                outputTextView.append(new String(readdata, 0, lenRead));
                                                scrollView.fullScroll(View.FOCUS_DOWN);
                                            }
                                        });
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Socket Error: " + e);
                                }
                            }
                        }).start();

                    } catch (IOException e) {
                        Log.e(TAG,"Socket Error: " + e);
                    }
                }
            }
        };
        this.initializeDiscoveryListener();
        this.nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        this.nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    private void sendText(){
        String aString = this.inputEditText.getText().toString() + '\n';
        new Thread(() -> {
            try {
                this.outputStream.write(aString.getBytes() );
            } catch (IOException e) {
                Log.e(TAG,"Socket Error: " + e);
            }
        }).start();
        this.inputEditText.setText("");
        this.outputTextView.append(aString);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    protected void talk(){
        try {
            Socket socket = new Socket(Build.VERSION.SDK_INT >= 34 ? mService.getHostAddresses().get(0) : mService.getHost(),8584);
            OutputStream output = socket.getOutputStream();
            byte[] data = "Hello there\n".getBytes();
            output.write(data);
            InputStream input = socket.getInputStream();
            byte[] readdata = new byte[256];
            input.read(readdata);
            Log.d(TAG,"Returned :" + new String(readdata));
            socket.close();
        } catch (IOException e) {
            Log.e(TAG,"Socket Error: " + e);
        }

    }

    protected void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started: " + regType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(serviceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + serviceName);
                    nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }

}