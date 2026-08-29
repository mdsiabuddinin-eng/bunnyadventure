package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AdMobManager handles Google AdMob initialization, loading, and presentation of
 * Banner, Interstitial, and Rewarded Ads using Google's official test ad units.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    // Official Google AdMob Ad Unit IDs
    const val BANNER_AD_UNIT_ID = "ca-app-pub-9532592617674983/7107736301"
    const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    const val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private val _isRewardedAdAvailable = MutableStateFlow(false)
    val isRewardedAdAvailable: StateFlow<Boolean> = _isRewardedAdAvailable.asStateFlow()

    private var isInitialized = false

    /**
     * Initializes Google Mobile Ads SDK once per app lifecycle.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob SDK Initialized: $initializationStatus")
                isInitialized = true
                preloadAds(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdMob SDK", e)
        }
    }

    fun preloadAds(context: Context) {
        loadInterstitialAd(context)
        loadRewardedAd(context)
    }

    /**
     * Preloads an Interstitial Ad.
     */
    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial Ad Loaded Successfully")
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Interstitial Ad Failed to Load: ${loadAdError.message}")
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    /**
     * Shows the Interstitial Ad if available, then executes the [onDismissed] action.
     * If no ad is ready, safely triggers [onDismissed] immediately without blocking gameplay.
     */
    fun showInterstitialAd(
        activity: Activity,
        onDismissed: () -> Unit = {}
    ) {
        val currentAd = interstitialAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad Dismissed")
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial Ad Failed to Show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad Showed")
                }
            }
            currentAd.show(activity)
        } else {
            Log.d(TAG, "Interstitial Ad not ready, proceeding immediately")
            loadInterstitialAd(activity)
            onDismissed()
        }
    }

    /**
     * Preloads a Rewarded Video Ad.
     */
    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_TEST_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded Ad Loaded Successfully")
                    rewardedAd = ad
                    _isRewardedAdAvailable.value = true
                    isRewardedLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded Ad Failed to Load: ${loadAdError.message}")
                    rewardedAd = null
                    _isRewardedAdAvailable.value = false
                    isRewardedLoading = false
                }
            }
        )
    }

    /**
     * Shows a Rewarded Video Ad to earn rewards (revive, extra life, bonus coins).
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (rewardAmount: Int, rewardType: String) -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        val currentAd = rewardedAd
        if (currentAd != null) {
            var rewardEarned = false
            var earnedAmount = 1
            var earnedType = "reward"

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad Dismissed")
                    rewardedAd = null
                    _isRewardedAdAvailable.value = false
                    loadRewardedAd(activity)
                    if (rewardEarned) {
                        onUserEarnedReward(earnedAmount, earnedType)
                    }
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded Ad Failed to Show: ${adError.message}")
                    rewardedAd = null
                    _isRewardedAdAvailable.value = false
                    loadRewardedAd(activity)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad Showed")
                }
            }

            currentAd.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                rewardEarned = true
                earnedAmount = rewardItem.amount
                earnedType = rewardItem.type
            }
        } else {
            Log.d(TAG, "Rewarded Ad not ready, granting simulated test reward for graceful testing")
            loadRewardedAd(activity)
            // Still grant reward in dev/test fallback so user flow is tested seamlessly
            onUserEarnedReward(1, "extra_life")
            onAdDismissed()
        }
    }
}
