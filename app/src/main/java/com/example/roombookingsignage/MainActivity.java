package com.example.roombookingsignage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.roombookingsignage.databinding.ActivityMainBinding;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startLockTask();
//        adbcommand("echo w 0x04 > ./sys/devices/platform/led_con_h/zigbee_reset");
        adbcommand("echo w 0x07 > ./sys/devices/platform/led_con_h/zigbee_reset");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        button_first = findViewById(R.id.button_first);
        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
//        http://119.73.206.38/meetingroom/SMVL3Board https://www.google.com/
        webView.loadUrl("http://119.73.206.38/meetingroom/SMVL3Board");
        webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Do something when the page finishes loading
                Log.e("haha", "finish loading");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript(
                                "document.getElementsByClassName('status')[0].innerHTML",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String html) {
                                        Log.e("HTML666", html);
                                        handlerinp.removeMessages(0);
                                        handlerav.removeMessages(0);
                                        // code here
                                        if (html != null && html.indexOf("In Progress") > -1) {
                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
                                            adbcommand("echo w 0x04 > ./sys/devices/platform/led_con_h/zigbee_reset");
                                            Dstatus = "inp";

                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    view.evaluateJavascript(
                                                            "document.getElementsByClassName('status')[0].innerHTML",
                                                            new ValueCallback<String>() {
                                                                @Override
                                                                public void onReceiveValue(String html) {
                                                                    Log.e("HTML332", html);
                                                                    // code here
                                                                    if (Dstatus.equals("av")) {
                                                                        if (html != null && html.indexOf("In Progress") > -1) {
                                                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
                                                                            adbcommand("echo w 0x04 > ./sys/devices/platform/led_con_h/zigbee_reset");
                                                                            Dstatus = "inp";
                                                                        }
                                                                    }

                                                                    if (Dstatus.equals("inp")) {
                                                                        if (html != null && html.indexOf("Available") > -1) {
                                                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
                                                                            adbcommand("echo w 0x05 > ./sys/devices/platform/led_con_h/zigbee_reset");
                                                                            Dstatus = "av";
                                                                        }
                                                                    }
                                                                }
                                                            });

                                                    handlerinp.postDelayed(this, 10 * 1000);
                                                }
                                            };

                                            handlerinp.postDelayed(runnable, 10 * 1000);

                                        } else if (html != null && html.indexOf("Available") > -1) {
                                            Toast.makeText(getApplicationContext(), "Available", Toast.LENGTH_SHORT).show();
                                            adbcommand("echo w 0x05 > ./sys/devices/platform/led_con_h/zigbee_reset");
                                            Dstatus = "av";

                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    view.evaluateJavascript(
                                                            "document.getElementsByClassName('status')[0].innerHTML",
                                                            new ValueCallback<String>() {
                                                                @Override
                                                                public void onReceiveValue(String html) {
                                                                    Log.e("HTML331", html);
                                                                    // code here
                                                                    if (Dstatus.equals("av")) {
                                                                        if (html != null && html.indexOf("In Progress") > -1) {
                                                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
                                                                            adbcommand("echo w 0x04 > ./sys/devices/platform/led_con_h/zigbee_reset");
                                                                            Dstatus = "inp";
                                                                        }
                                                                    }

                                                                    if (Dstatus.equals("inp")) {
                                                                        if (html != null && html.indexOf("Available") > -1) {
                                                                            Toast.makeText(getApplicationContext(), "In Progress", Toast.LENGTH_SHORT).show();
                                                                            adbcommand("echo w 0x05 > ./sys/devices/platform/led_con_h/zigbee_reset");
                                                                            Dstatus = "av";
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                    handlerav.postDelayed(this, 10 * 1000);

                                                }
                                            };

                                            handlerav.postDelayed(runnable, 10 * 1000);
                                        }
                                    }
                                });

                    }

                }, 4000);

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
    }


    @Override
    protected void onResume() {
        super.onResume();

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
        stopLockTask();
        super.onDestroy();
    }


}