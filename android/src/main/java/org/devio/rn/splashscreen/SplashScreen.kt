package org.devio.rn.splashscreen

import android.animation.Animator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieAnimationView
import java.lang.ref.WeakReference

/**
 * SplashScreen
 * 启动屏
 * from：http://www.devio.org
 * Author:CrazyCodeBoy
 * GitHub:https://github.com/crazycodeboy
 * Email:crazycodeboy@gmail.com
 */
object SplashScreen {
    private var mSplashDialog: Dialog? = null
    private var mActivity: WeakReference<Activity>? = null
    private var isAnimationFinished = false
    private var waiting = false


    fun getUsableScreenHeight(context: Activity): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) 이상
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val displayHeight = windowMetrics.bounds.height()
            displayHeight - insets.top - insets.bottom
        } else {
            // Android 10 (API 29) 이하
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(metrics)
            val rect = Rect()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRectSize(rect)
            rect.height()
        }
    }


    /**
     * 打开启动屏
     */
    @JvmStatic
    fun show(activity: Activity?, themeResId: Int = R.style.SplashScreen_SplashTheme, lottieId: Int , progressBarId : Int?) {
        if (activity == null) return
        mActivity = WeakReference(activity)
        activity.runOnUiThread {
            if (!activity.isFinishing) {
                mSplashDialog = Dialog(activity, themeResId)
                mSplashDialog?.setContentView(R.layout.launch_screen)
                mSplashDialog?.setCancelable(false)
                val lottie = mSplashDialog?.findViewById<LottieAnimationView>(lottieId)


                val usableHeight = getUsableScreenHeight(activity)
                val height = usableHeight /2

                lottie?.updateLayoutParams {
                    this.width = (height).toInt()
                    this.height = (height).toInt()

                    (this as ViewGroup.MarginLayoutParams).topMargin = -(height * 0.5 - height * 0.15  ).toInt()
                }

                lottie?.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        println("SplashScreen is started")
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        setAnimationFinished(true)
                    }

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}
                })

                if (mSplashDialog?.isShowing == false) {
                    mSplashDialog?.show()

                    if (progressBarId != null) {
                        val progressBar = mSplashDialog?.findViewById<ProgressBar>(progressBarId)
                        Handler().postDelayed({
                            progressBar?.visibility = View.VISIBLE
                        }, 2000)
                    }
                }

            }
        }
    }

    @JvmStatic
    fun setAnimationFinished(flag: Boolean) {
        if (mActivity == null) return

        isAnimationFinished = flag

        val _activity = mActivity?.get() ?: return

        _activity.runOnUiThread {
            if (mSplashDialog != null && mSplashDialog?.isShowing == true) {
                var isDestroyed = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    isDestroyed = _activity.isDestroyed
                }

                if (!_activity.isFinishing && !isDestroyed && waiting) {
                    mSplashDialog?.dismiss()
                    mSplashDialog = null
                }
            }
        }
    }

    fun hide(activity: Activity?) {
        var _activity = activity
        if (_activity == null) {
            _activity = mActivity?.get()
        }

        if (_activity == null) return

        waiting = true

        _activity.runOnUiThread {
            if (mSplashDialog != null && mSplashDialog?.isShowing == true) {
                var isDestroyed = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    isDestroyed = _activity.isDestroyed
                }

                if (!_activity.isFinishing && !isDestroyed && isAnimationFinished) {
                    mSplashDialog?.dismiss()
                    mSplashDialog = null
                }
            }
        }
    }
}

