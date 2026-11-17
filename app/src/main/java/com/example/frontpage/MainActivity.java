package com.example.frontpage;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textView = findViewById(R.id.textView);

        // Apply logo colors: "Cyclone" in dark navy blue (#052A4A), "Safe" in bright cyan blue (#008CD7)
        String text = "CycloneSafe";
        SpannableString spannableString = new SpannableString(text);
        int cycloneColor = ContextCompat.getColor(this, R.color.cyclone_navy);
        int safeColor = ContextCompat.getColor(this, R.color.safe_cyan);

        spannableString.setSpan(new ForegroundColorSpan(cycloneColor), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(safeColor), 7, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);

        // Entrance + Continuous Pulse Animation
        textView.setAlpha(0f);
        textView.setTranslationY(-50f);
        textView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    // Start continuous breathing/pulsing animation
                    ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(textView, "scaleX", 1f, 1.06f, 1f);
                    scaleXAnim.setDuration(1400);
                    scaleXAnim.setRepeatCount(ValueAnimator.INFINITE);
                    scaleXAnim.setRepeatMode(ValueAnimator.REVERSE);

                    ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 1.06f, 1f);
                    scaleYAnim.setDuration(1400);
                    scaleYAnim.setRepeatCount(ValueAnimator.INFINITE);
                    scaleYAnim.setRepeatMode(ValueAnimator.REVERSE);

                    AnimatorSet pulseAnim = new AnimatorSet();
                    pulseAnim.playTogether(scaleXAnim, scaleYAnim);
                    pulseAnim.start();
                })
                .start();

        // Animate ImageView (Emblem)
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setAlpha(0f);
        imageView.setScaleX(0.8f);
        imageView.setScaleY(0.8f);
        imageView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setStartDelay(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
