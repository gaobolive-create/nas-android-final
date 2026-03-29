package com.example.nasclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ListView listViewFiles;
    private TextView textViewWelcome;
    private Button buttonRefresh, buttonLogout;
    private SharedPreferences sharedPreferences;
    private ExecutorService executor;
    private List<FileInfo> fileList;
    private FileAdapter adapter;
    private String server;
    private String token;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewFiles = findViewById(R.id.listViewFiles);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        buttonRefresh = findViewById(R.id.buttonRefresh);
        buttonLogout = findViewById(R.id.buttonLogout);

        sharedPreferences = getSharedPreferences("NASClient", MODE_PRIVATE);
        executor = Executors.newSingleThreadExecutor();

        server = sharedPreferences.getString("server", "");
        token = sharedPreferences.getString("token", "");
        username = sharedPreferences.getString("username", "");

        textViewWelcome.setText("欢迎, " + username);

        fileList = new ArrayList<>();
        adapter = new FileAdapter();
        listViewFiles.setAdapter(adapter);

        listViewFiles.setOnItemClickListener((parent, view, position, id) -> {
            FileInfo file = fileList.get(position);
            openFile(file);
        });

        buttonRefresh.setOnClickListener(v -> loadFiles());
        buttonLogout.setOnClickListener(v -> logout());

        loadFiles();
    }

    private void loadFiles() {
        executor.execute(() -> {
            try {
                URL url = new URL("http://" + server + "/api/list");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream());
                    scanner.useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();

                    JSONObject result = new JSONObject(response);
                    if (result.getBoolean("success")) {
                        JSONArray files = result.getJSONArray("files");
                        fileList.clear();

                        for (int i = 0; i < files.length(); i++) {
                            JSONObject f = files.getJSONObject(i);
                            fileList.add(new FileInfo(
                                f.getString("name"),
                                f.getLong("size"),
                                f.getString("type")
                            ));
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                } else if (responseCode == 401) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
                        logout();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void openFile(FileInfo file) {
        // 判断文件类型
        if (file.type.startsWith("video")) {
            // 打开视频播放
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("server", server);
            intent.putExtra("token", token);
            intent.putExtra("filename", file.name);
            startActivity(intent);
        } else {
            new AlertDialog.Builder(this)
                .setTitle("文件信息")
                .setMessage("文件名: " + file.name + "\n大小: " + formatSize(file.size) + "\n类型: " + file.type)
                .setPositiveButton("确定", null)
                .show();
        }
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }

    private void logout() {
        sharedPreferences.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private static class FileInfo {
        String name;
        long size;
        String type;

        FileInfo(String name, long size, String type) {
            this.name = name;
            this.size = size;
            this.type = type;
        }
    }

    private class FileAdapter extends ArrayAdapter<FileInfo> {
        FileAdapter() {
            super(MainActivity.this, android.R.layout.simple_list_item_2, fileList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            FileInfo file = fileList.get(position);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(file.name);
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(formatSize(file.size) + " - " + file.type);

            return convertView;
        }
    }
}
