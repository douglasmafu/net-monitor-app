package com.local.netmonitor;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private TextView txtNetworkType, txtSpeedResult;
    private Button btnTestSpeed;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtNetworkType = findViewById(R.id.txtNetworkType);
        txtSpeedResult = findViewById(R.id.txtSpeedResult);
        btnTestSpeed = findViewById(R.id.btnTestSpeed);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainExecutor());
        checkNetworkType();
        btnTestSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtSpeedResult.setText("Midiendo velocidad...");
                checkNetworkType();
                measureDownloadSpeed();
            }
        });
    }

    private void checkNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(network);
                if (caps != null) {
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        txtNetworkType.setText("Tipo de Conexión: Wi-Fi");
                    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        txtNetworkType.setText("Tipo de Conexión: Datos Móviles");
                    } else {
                        txtNetworkType.setText("Tipo de Conexión: Otra Red");
                    }
                    return;
                }
            }
        }
        txtNetworkType.setText("Tipo de Conexión: Desconectado");
    }

    private void measureDownloadSpeed() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                long totalBytes = 0;
                try {
                    URL url = new URL("https://www.gstatic.com/generate_204");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytes += bytesRead;
                    }
                    inputStream.close();
                    connection.disconnect();
                    long endTime = System.currentTimeMillis();
                    long durationMs = endTime - startTime;
                    if (durationMs == 0) durationMs = 1;
                    double speedBps = (totalBytes * 8.0) / (durationMs / 1000.0);
                    double speedMbps = (speedBps / 1024.0) / 1024.0;
                    final String resultText = String.format("Velocidad estimada: %.2f Mbps", speedMbps);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            txtSpeedResult.setText(resultText);
                        }
                    });
                } catch (Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            txtSpeedResult.setText("Error en la prueba.");
                        }
                    });
                }
            }
        });
    }
                  }
