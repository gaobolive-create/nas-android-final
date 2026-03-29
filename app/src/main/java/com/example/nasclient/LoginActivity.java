package com.example.nasclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextServer, editTextUsername, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private SharedPreferences sharedPreferences;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextServer = findViewById(R.id.editTextServer);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        sharedPreferences = getSharedPreferences("NASClient", MODE_PRIVATE);
        executor = Executors.newSingleThreadExecutor();

        editTextServer.setText(sharedPreferences.getString("server", "192.168.123.172:8888"));

        buttonLogin.setOnClickListener(v -> login());
        buttonRegister.setOnClickListener(v -> register());
    }

    private void login() {
        String server = editTextServer.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (server.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请填写所有信息", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedPreferences.edit().putString("server", server).apply();

        executor.execute(() -> {
            try {
                URL url = new URL("http://" + server + "/api/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", password);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream());
                    scanner.useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();

                    JSONObject result = new JSONObject(response);
                    if (result.getBoolean("success")) {
                        String token = result.getString("token");
                        JSONObject user = result.getJSONObject("user");
                        int userId = user.getInt("id");

                        sharedPreferences.edit()
                            .putString("token", token)
                            .putInt("userId", userId)
                            .putString("username", username)
                            .apply();

                        runOnUiThread(() -> {
                            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        });
                    } else {
                        String error = result.optString("error", "登录失败");
                        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void register() {
        String server = editTextServer.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (server.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请填写所有信息", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedPreferences.edit().putString("server", server).apply();

        executor.execute(() -> {
            try {
                URL url = new URL("http://" + server + "/api/register");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("password", password);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream());
                    scanner.useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();

                    JSONObject result = new JSONObject(response);
                    if (result.getBoolean("success")) {
                        String token = result.getString("token");
                        int userId = result.getInt("user_id");

                        sharedPreferences.edit()
                            .putString("token", token)
                            .putInt("userId", userId)
                            .putString("username", username)
                            .apply();

                        runOnUiThread(() -> {
                            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        });
                    } else {
                        String error = result.optString("error", "注册失败");
                        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "注册失败", Toast.LENGTH_SHORT).show());
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "连接失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
