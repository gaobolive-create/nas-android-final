package com.example.nasclient;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoPlayerActivity extends AppCompatActivity {

    private String server;
    private String token;
    private String filename;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        server = getIntent().getStringExtra("server");
        token = getIntent().getStringExtra("token");
        filename = getIntent().getStringExtra("filename");

        executor = Executors.newSingleThreadExecutor();

        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText(filename);

        // 注意：这里需要使用第三方视频播放库如ExoPlayer
        // 这里只是一个简化示例
        Toast.makeText(this, "播放: " + filename + "\nURL: http://" + server + "/api/file/" + filename, Toast.LENGTH_LONG).show();
    }
}
