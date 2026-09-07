package go.pemkott.appsandroidmobiletebingtinggi.helpdesk.ajukan;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import go.pemkott.appsandroidmobiletebingtinggi.R;

public class MediaViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_media_viewer);

        ImageView ivFullImage = findViewById(R.id.ivFullImage);
        VideoView vvFullVideo = findViewById(R.id.vvFullVideo);
        ProgressBar pbLoading = findViewById(R.id.pbLoading);

        Uri mediaUri = getIntent().getData();
        if (mediaUri == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mediaUri = getIntent().getParcelableExtra("uri", Uri.class);
            } else {
                mediaUri = getIntent().getParcelableExtra("uri");
            }
        }

        if (mediaUri == null) {
            Toast.makeText(this, "Gagal memuat media.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String mimeType = null;
        String scheme = mediaUri.getScheme();
        if ("content".equals(scheme)) {
            mimeType = getContentResolver().getType(mediaUri);
        } else if ("http".equals(scheme) || "https".equals(scheme)) {
            String url = mediaUri.toString().toLowerCase();
            if (url.endsWith(".mp4") || url.endsWith(".mkv") || url.endsWith(".mov") || url.endsWith(".avi")) {
                mimeType = "video/*";
            }
        }

        findViewById(R.id.cvBack).setOnClickListener(v -> finish());

        if (mimeType != null && mimeType.startsWith("video")) {
            vvFullVideo.setVisibility(View.VISIBLE);
            pbLoading.setVisibility(View.VISIBLE);

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(vvFullVideo);
            vvFullVideo.setMediaController(mediaController);
            vvFullVideo.setVideoURI(mediaUri);

            vvFullVideo.setOnPreparedListener(mp -> {
                pbLoading.setVisibility(View.GONE);
                vvFullVideo.start();
            });

            vvFullVideo.setOnErrorListener((mp, what, extra) -> {
                pbLoading.setVisibility(View.GONE);
                return false;
            });
        } else {
            ivFullImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(mediaUri).into(ivFullImage);
        }
    }
}
