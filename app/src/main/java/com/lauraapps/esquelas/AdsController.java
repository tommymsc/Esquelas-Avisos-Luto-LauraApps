package com.lauraapps.esquelas;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ads are completely isolated from the memorial renderer and photo storage.
 * Debug builds use Google's sample IDs; release builds require this app's own IDs.
 * Consent is refreshed before requests. Ads never block editing, saving or sharing.
 */
final class AdsController {
    private final Activity activity;
    private final Runnable privacyUiChanged;
    private ConsentInformation consent;
    private FrameLayout bannerContainer;
    private AdView banner;
    private InterstitialAd interstitial;
    private final AtomicBoolean initializing = new AtomicBoolean(false);
    private boolean ready = false, destroyed = false, interstitialLoading = false;
    private final long startedAt = SystemClock.elapsedRealtime();
    private long lastInterstitialAt = 0L;
    private int completedDownloads = 0;
    private static final long MIN_DELAY_MS = 180_000L;

    AdsController(Activity activity, Runnable privacyUiChanged) {
        this.activity = activity;
        this.privacyUiChanged = privacyUiChanged;
    }

    private boolean validConfiguration() {
        boolean bannerValid = BuildConfig.BANNER_AD_UNIT.matches("ca-app-pub-[0-9]{16}/[0-9]{10}");
        boolean fullValid = BuildConfig.INTERSTITIAL_AD_UNIT.matches("ca-app-pub-[0-9]{16}/[0-9]{10}");
        return bannerValid || fullValid;
    }

    void start() {
        if (!validConfiguration()) return;
        consent = UserMessagingPlatform.getConsentInformation(activity);
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        consent.requestConsentInfoUpdate(activity, params, () -> {
            if (destroyed) return;
            privacyUiChanged.run();
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, formError -> {
                if (!destroyed) initializeIfAllowed();
            });
            initializeIfAllowed();
        }, error -> {
            if (!destroyed) initializeIfAllowed(); // UMP may allow previous consent.
        });
    }

    boolean privacyOptionsRequired() {
        return consent != null &&
            consent.getPrivacyOptionsRequirementStatus() ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    void showPrivacyOptions() {
        if (consent == null || !privacyOptionsRequired()) return;
        UserMessagingPlatform.showPrivacyOptionsForm(activity, formError -> {
            if (destroyed) return;
            // Never load new ads when UMP no longer allows requests.
            if (!consent.canRequestAds()) {
                ready = false;
                destroyBanner();
                interstitial = null;
            } else initializeIfAllowed();
            privacyUiChanged.run();
        });
    }

    private void initializeIfAllowed() {
        if (destroyed || consent == null || !consent.canRequestAds() ||
            !initializing.compareAndSet(false,true)) return;
        new Thread(() -> {
            try {
                MobileAds.initialize(activity, status -> activity.runOnUiThread(() -> {
                    if (destroyed) return;
                    ready = true;
                    createBannerIfAppropriate();
                    loadInterstitial();
                }));
            } catch (RuntimeException ignored) {
                initializing.set(false); // App still works completely without ads.
            }
        }, "admob-init").start();
    }

    void attachBanner(FrameLayout container) {
        if (bannerContainer != container) destroyBanner();
        bannerContainer = container;
        if (container != null) container.setVisibility(View.GONE);
        createBannerIfAppropriate();
    }

    private void createBannerIfAppropriate() {
        if (!ready || destroyed || bannerContainer == null || banner != null ||
            !consent.canRequestAds() || BuildConfig.BANNER_AD_UNIT.isEmpty()) return;
        try {
            AdView ad = new AdView(activity);
            ad.setAdUnitId(BuildConfig.BANNER_AD_UNIT);
            int widthDp = Math.max(260,
                (int)(activity.getResources().getDisplayMetrics().widthPixels /
                    activity.getResources().getDisplayMetrics().density) - 24);
            ad.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                activity, widthDp));
            banner = ad;
            bannerContainer.addView(ad, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ad.setAdListener(new AdListener() {
                @Override public void onAdLoaded() {
                    if (!destroyed && banner == ad && bannerContainer != null)
                        bannerContainer.setVisibility(View.VISIBLE);
                }
                @Override public void onAdFailedToLoad(LoadAdError error) {
                    if (!destroyed && bannerContainer != null)
                        bannerContainer.setVisibility(View.GONE);
                }
            });
            ad.loadAd(new AdRequest.Builder().build());
        } catch (RuntimeException ignored) {
            destroyBanner();
        }
    }

    void detachBanner() {
        bannerContainer = null;
        destroyBanner();
    }

    private void destroyBanner() {
        if (banner == null) return;
        AdView old = banner;
        banner = null;
        if (old.getParent() instanceof ViewGroup)
            ((ViewGroup)old.getParent()).removeView(old);
        old.destroy();
    }

    private void loadInterstitial() {
        if (!ready || destroyed || !consent.canRequestAds() ||
            BuildConfig.INTERSTITIAL_AD_UNIT.isEmpty() ||
            interstitialLoading || interstitial != null) return;
        interstitialLoading = true;
        InterstitialAd.load(activity, BuildConfig.INTERSTITIAL_AD_UNIT,
            new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override public void onAdLoaded(InterstitialAd loaded) {
                    interstitialLoading = false;
                    if (!destroyed) interstitial = loaded;
                }
                @Override public void onAdFailedToLoad(LoadAdError error) {
                    interstitialLoading = false;
                    interstitial = null;
                }
            });
    }

    void completedImageDownload() {
        completedDownloads = Math.min(3, completedDownloads + 1);
    }

    /** Called only when returning home, not while editing or sharing. */
    void onHomeAfterExport() {
        long now = SystemClock.elapsedRealtime();
        if (!ready || destroyed || consent == null || !consent.canRequestAds() ||
            completedDownloads < 3 || interstitial == null ||
            now-startedAt < MIN_DELAY_MS ||
            (lastInterstitialAt != 0 && now-lastInterstitialAt < MIN_DELAY_MS)) return;
        InterstitialAd ad = interstitial;
        interstitial = null;
        completedDownloads = 0;
        lastInterstitialAt = now;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                if (!destroyed) loadInterstitial();
            }
            @Override public void onAdFailedToShowFullScreenContent(AdError error) {
                if (!destroyed) loadInterstitial();
            }
        });
        try { ad.show(activity); } catch (RuntimeException ignored) { loadInterstitial(); }
    }

    void destroy() {
        destroyed = true;
        destroyBanner();
        bannerContainer = null;
        interstitial = null;
    }
}
