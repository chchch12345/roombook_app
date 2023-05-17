package com.example.roombookingsignage;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.roombookingsignage.databinding.ActivityMainBinding;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import org.java_websocket.client.WebSocketClient;

public class MainActivity extends AppCompatActivity {
    private WebSocketClient mWebSocketClient;

    private HubConnection hubConnection;
    Button button_first;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private RelativeLayout loadingScreen;
    private WebView webView;
    private Handler handlerinp = new Handler();
    private Handler handlerav = new Handler();

    Button buttonUnpin;

    View togleShowButton;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable unpinRunnable;
    private Runnable hideNavRunnable;
    private static final long DEBOUNCE_DELAY_MS = 2000;
    Boolean justone = false;

    public String adbcommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        String excresult = "";
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line + " ");
            }
            excresult = stringBuffer.toString();
            Log.d("Jessica2 ", excresult);


            os.close();
            // System.out.println(excresult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return excresult;
    }

    String Dstatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            // You can hide the caption bar even when the other system bars are visible.
            // To account for this, explicitly check the visibility of navigationBars()
            // and statusBars() rather than checking the visibility of systemBars().
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                        || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                    // Hide both the status bar and the navigation bar.

                    if (hideNavRunnable != null) {
                        // debounce: remove any previously scheduled execution
                        mHandler.removeCallbacks(hideNavRunnable);
                    }

                    // create a new Runnable to execute the code after the delay
                    hideNavRunnable = new Runnable() {
                        @Override
                        public void run() {
                            // code to execute after the delay
                            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
                        }
                    };

                    // post the message with the delay to the main thread's message queue
                    mHandler.postDelayed(hideNavRunnable, DEBOUNCE_DELAY_MS);
                }
            }
            return view.onApplyWindowInsets(windowInsets);
        });


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startLockTask();
//        adbcommand("echo w 0x04 > ./sys/devices/platform/led_con_h/zigbee_reset");
        adbcommand("echo w 0x07 > ./sys/devices/platform/led_con_h/zigbee_reset");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        button_first = findViewById(R.id.button_first);
        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
//        http://119.73.206.38/meetingroom/SMVL3Board https://www.google.com/
        webView.loadUrl("http://119.73.206.38/meetingroom/SMVL3Board");
        webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        buttonUnpin = findViewById(R.id.buttonUnpin);
        buttonUnpin.setVisibility(View.GONE);
        buttonUnpin.setOnClickListener(v -> {
            stopLockTask();
        });
        togleShowButton = findViewById(R.id.v_show_button);
        togleShowButton.setOnClickListener(v -> {
            buttonUnpin.setVisibility(View.VISIBLE);
            if (unpinRunnable != null) {
                // debounce: remove any previously scheduled execution
                mHandler.removeCallbacks(unpinRunnable);
            }

            // create a new Runnable to execute the code after the delay
            unpinRunnable = new Runnable() {
                @Override
                public void run() {
                    // code to execute after the delay
                    buttonUnpin.setVisibility(View.GONE);
                }
            };

            // post the message with the delay to the main thread's message queue
            mHandler.postDelayed(unpinRunnable, DEBOUNCE_DELAY_MS);
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.e("WebView", consoleMessage.message());

                if (consoleMessage.message().toString().contains("GMT+0800 (CST)")) {
                    Log.e("WebView", "incOMING !!!!!");

                    if (!justone) {
                        justone = true;
                        webView.evaluateJavascript(
                                "document.getElementsByClassName('status')[0].innerHTML",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String html) {
                                        Log.e("HTMLonConsoleMessage", html);
                                        if (html != null && html.indexOf("In Progress") > -1) {
//                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
                                            adbcommand("echo w 0x04 > ./sys/devices/platform/led_con_h/zigbee_reset");
                                        } else if (html != null && html.indexOf("Available") > -1) {
//                                            Toast.makeText(getApplicationContext(), "Available", Toast.LENGTH_SHORT).show();
                                            adbcommand("echo w 0x05 > ./sys/devices/platform/led_con_h/zigbee_reset");
                                        }
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                justone = false;
                                            }
                                        }, 5000);
                                    }
                                });
//                    }

                    }
                }
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Do something when the page finishes loading
                Log.e("haha", "finish loading");
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        view.evaluateJavascript(
//                                "document.getElementsByClassName('status')[0].innerHTML",
//                                new ValueCallback<String>() {
//                                    @Override
//                                    public void onReceiveValue(String html) {
//                                        Log.e("HTML666", html);
//                                        handlerinp.removeMessages(0);
//                                        handlerav.removeMessages(0);
//                                        // code here
//                                        if (html != null && html.indexOf("In Progress") > -1) {
//                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
//                                            adbcommand("echo w 0x04 > ./sys/devices/platform/led_con_h/zigbee_reset");
//                                            Dstatus = "inp";
//
//                                            Runnable runnable = new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    view.evaluateJavascript(
//                                                            "document.getElementsByClassName('status')[0].innerHTML",
//                                                            new ValueCallback<String>() {
//                                                                @Override
//                                                                public void onReceiveValue(String html) {
//                                                                    Log.e("HTML332", html);
//                                                                    // code here
//                                                                    if (Dstatus.equals("av")) {
//                                                                        if (html != null && html.indexOf("In Progress") > -1) {
//                                                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
//                                                                            adbcommand("echo w 0x04 > ./sys/devices/platform/led_con_h/zigbee_reset");
//                                                                            Dstatus = "inp";
//                                                                        }
//                                                                    }
//
//                                                                    if (Dstatus.equals("inp")) {
//                                                                        if (html != null && html.indexOf("Available") > -1) {
//                                                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
//                                                                            adbcommand("echo w 0x05 > ./sys/devices/platform/led_con_h/zigbee_reset");
//                                                                            Dstatus = "av";
//                                                                        }
//                                                                    }
//                                                                }
//                                                            });
//
//                                                    handlerinp.postDelayed(this, 10 * 1000);
//                                                }
//                                            };
//
//                                            handlerinp.postDelayed(runnable, 10 * 1000);
//
//                                        } else if (html != null && html.indexOf("Available") > -1) {
//                                            Toast.makeText(getApplicationContext(), "Available", Toast.LENGTH_SHORT).show();
//                                            adbcommand("echo w 0x05 > ./sys/devices/platform/led_con_h/zigbee_reset");
//                                            Dstatus = "av";
//
//                                            Runnable runnable = new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    view.evaluateJavascript(
//                                                            "document.getElementsByClassName('status')[0].innerHTML",
//                                                            new ValueCallback<String>() {
//                                                                @Override
//                                                                public void onReceiveValue(String html) {
//                                                                    Log.e("HTML331", html);
//                                                                    // code here
//                                                                    if (Dstatus.equals("av")) {
//                                                                        if (html != null && html.indexOf("In Progress") > -1) {
//                                                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
//                                                                            adbcommand("echo w 0x04 > ./sys/devices/platform/led_con_h/zigbee_reset");
//                                                                            Dstatus = "inp";
//                                                                        }
//                                                                    }
//
//                                                                    if (Dstatus.equals("inp")) {
//                                                                        if (html != null && html.indexOf("Available") > -1) {
//                                                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
//                                                                            adbcommand("echo w 0x05 > ./sys/devices/platform/led_con_h/zigbee_reset");
//                                                                            Dstatus = "av";
//                                                                        }
//                                                                    }
//                                                                }
//                                                            });
//                                                    handlerav.postDelayed(this, 10 * 1000);
//
//                                                }
//                                            };
//
//                                            handlerav.postDelayed(runnable, 10 * 1000);
//                                        }
//                                    }
//                                });
//
//                    }
//
//                }, 4000);

            }
        });
//        connectWebSocket();

        button_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("clickssdsds");

                webView.evaluateJavascript(
                        "document.getElementsByClassName('status')[0].innerHTML",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String html) {
                                Log.e("HTML", html);
                                // code here
                                if (html != null && html.indexOf("In Progress") > -1) {
                                    Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
                                } else if (html != null && html.indexOf("Available") > -1) {
                                    Toast.makeText(getApplicationContext(), "Available", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

            }
        });
//        startLockTask();
    }


    @Override
    protected void onResume() {
        super.onResume();
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);
//        webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void connectWebSocket() {
        hubConnection = HubConnectionBuilder.create("http://119.73.206.38/hub/meetingroom?location_id=2211").build();
//        http://119.73.206.38/hub/frsdevice?device_id=2211

        hubConnection.on("RefreshDisplay", (msg1, msg2) -> {
            Log.e("dataCheck", "-> " + msg1);
            Log.e("dataCheck", "-> " + msg2);
            if (hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED)
                hubConnection.start();

        }, String.class, String.class);


        hubConnection.start();
//        URI uri;
//        try {
////            http://119.73.206.38/hub/frsdevice?device_id=3110
//            uri = new URI("ws://119.73.206.38/hub/frsdevice?device_id=3110/");
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        mWebSocketClient = new WebSocketClient(uri) {
//            @Override
//            public void onOpen(ServerHandshake serverHandshake) {
//                Log.i("Websocket", "Opened");
//                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
//            }
//
//            @Override
//            public void onMessage(String s) {
//                final String message = s;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.e("haha",message);
//                    }
//                });
//            }
//
//            @Override
//            public void onClose(int i, String s, boolean b) {
//                Log.i("Websocket", "Closed " + s);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.i("Websocket", "Error " + e.getMessage());
//            }
//        };
//        mWebSocketClient.connect();
    }


    @Override
    protected void onDestroy() {
        adbcommand("echo w 0x07 > ./sys/devices/platform/led_con_h/zigbee_reset");
        super.onDestroy();
        stopLockTask();
    }


}