package com.example.fooddeliveryapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Edge-to-edge + animation helpers.
 * Works on every phone: notch, punch-hole, gesture bar, 3-button nav bar.
 */
public final class UiUtils {

    private UiUtils() {}

    public static float dp(View v, float value) {
        return value * v.getResources().getDisplayMetrics().density;
    }

    // ============================================================
    // EDGE TO EDGE  (call BEFORE setContentView)
    // ============================================================

    public static void enableEdgeToEdge(Activity activity) {

        Window window = activity.getWindow();

        WindowCompat.setDecorFitsSystemWindows(window, false);

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= 28) {
            window.getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if (Build.VERSION.SDK_INT >= 29) {
            window.setNavigationBarContrastEnforced(false);
        }

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());

        controller.setAppearanceLightStatusBars(false);     // white icons on the orange header
        controller.setAppearanceLightNavigationBars(true);  // dark icons on the light nav bar
    }

    // ============================================================
    // INSETS
    // ============================================================

    /** Adds status-bar / notch height to the view's top padding (and cutout to left/right). */
    public static void applyTopInset(final View v) {

        final int pl = v.getPaddingLeft();
        final int pt = v.getPaddingTop();
        final int pr = v.getPaddingRight();
        final int pb = v.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(pl + bars.left, pt + bars.top, pr + bars.right, pb);
            return insets;
        });

        ViewCompat.requestApplyInsets(v);
    }

    /** Adds navigation-bar height to the view's bottom padding. */
    public static void applyBottomPadding(final View v) {

        final int pl = v.getPaddingLeft();
        final int pt = v.getPaddingTop();
        final int pr = v.getPaddingRight();
        final int pb = v.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(pl, pt, pr, pb + bars.bottom);
            return insets;
        });

        ViewCompat.requestApplyInsets(v);
    }

    /** Adds navigation-bar height to the view's bottom margin (floating bars). */
    public static void applyBottomMargin(final View v) {

        final ViewGroup.MarginLayoutParams lp =
                (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        final int base = lp.bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.bottomMargin = base + bars.bottom;
            view.setLayoutParams(params);
            return insets;
        });

        ViewCompat.requestApplyInsets(v);
    }

    // ============================================================
    // ANIMATIONS
    // ============================================================

    /** Fade + slide-up entrance, one after another. */
    public static void staggerIn(View... views) {

        long delay = 0;

        for (View v : views) {

            if (v == null) continue;

            v.setAlpha(0f);
            v.setTranslationY(dp(v, 24));

            v.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(480)
                    .setInterpolator(new DecelerateInterpolator(1.8f))
                    .start();

            delay += 90;
        }
    }

    /** Springy "press" feedback. Does not consume the touch, so clicks still work. */
    public static void attachPress(View v) {

        v.setOnTouchListener((view, event) -> {

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    view.animate().scaleX(0.96f).scaleY(0.96f)
                            .setDuration(120).setInterpolator(new DecelerateInterpolator()).start();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.animate().scaleX(1f).scaleY(1f)
                            .setDuration(260).setInterpolator(new OvershootInterpolator(2.2f)).start();
                    break;
            }

            return false;
        });
    }

    /** Quick bounce, e.g. when the cart count or an item quantity changes. */
    public static void pop(View v) {

        v.animate().cancel();

        v.setScaleX(1f);
        v.setScaleY(1f);

        v.animate().scaleX(1.18f).scaleY(1.18f).setDuration(110)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f)
                        .setDuration(220).setInterpolator(new OvershootInterpolator(3f)).start())
                .start();
    }

    /** Slide the floating cart bar up into view. */
    public static void slideUpShow(View v) {

        if (v.getVisibility() == View.VISIBLE && v.getAlpha() == 1f) return;

        v.animate().cancel();

        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.setTranslationY(dp(v, 90));

        v.animate().alpha(1f).translationY(0f)
                .setDuration(380)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .start();
    }

    /** Slide the floating cart bar down and hide it. */
    public static void slideDownHide(final View v) {

        if (v.getVisibility() != View.VISIBLE) return;

        v.animate().cancel();

        v.animate().alpha(0f).translationY(dp(v, 90))
                .setDuration(240)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> v.setVisibility(View.GONE))
                .start();
    }
}