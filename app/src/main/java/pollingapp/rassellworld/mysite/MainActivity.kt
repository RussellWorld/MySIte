package pollingapp.rassellworld.mysite

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import pollingapp.rassellworld.mysite.databinding.ActivityMainBinding

var networkAviable = false

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val mWebView = binding.appBarMain.content.webView
        var url = getString(R.string.website_home)
        binding.appBarMain.content.webView.webViewClient = WebViewClient()
        binding.appBarMain.content.webView.loadUrl(url)

        binding.appBarMain.content.swipeRefreshLayout.setColorSchemeColors(
            R.color.design_default_color_background,
            R.color.design_default_color_error,
            R.color.purple_500
        )
        binding.appBarMain.content.swipeRefreshLayout.apply {
            setOnRefreshListener {
                if (mWebView.url != null) url = mWebView.url!!
                loadWebSite(mWebView, url, applicationContext)
            }
            setOnChildScrollUpCallback { parent, child -> mWebView.scrollY > 0 }
        }

        loadWebSite(mWebView, url, applicationContext)
        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

    }

    private fun loadWebSite(mWebView: WebView, url: String, context: Context) {
        binding.appBarMain.content.progressBar.visibility = View.VISIBLE
        networkAviable = isNetworkAvailable(context)
        mWebView.clearCache(true)
        if (networkAviable) {
            wvVisible(mWebView)
            mWebView.webViewClient = MyWevViewClient()
            mWebView.loadUrl(url)
        } else {
            wvGone(mWebView)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun wvVisible(mWebView: WebView) {
        mWebView.visibility = View.VISIBLE
        binding.appBarMain.content.tvCheckConnection.visibility = View.GONE
    }

    private fun wvGone(mWebView: WebView) {
        mWebView.visibility = View.GONE
        binding.appBarMain.content.tvCheckConnection.visibility = View.VISIBLE
        binding.appBarMain.content.progressBar.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    private fun isNetworkAvailable(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT > 22) {
                val an = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(an) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                val a = cm.activeNetworkInfo ?: return false
                a.isConnected && (a.type == ConnectivityManager.TYPE_WIFI || a.type == ConnectivityManager.TYPE_MOBILE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    private fun onLoadComplete() {
        binding.appBarMain.content.swipeRefreshLayout.isRefreshing = false
        binding.appBarMain.content.progressBar.visibility = View.GONE
    }

    private inner class MyWevViewClient : WebViewClient() {

        @RequiresApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url.toString()
            return urlOverride(url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            return urlOverride(url)
        }

        private fun urlOverride(url: String): Boolean {
            binding.appBarMain.content.progressBar.visibility = View.VISIBLE
            networkAviable = isNetworkAvailable(applicationContext)

            if (networkAviable) {
                if (Uri.parse(url).host == getString(R.string.website_home)) return false
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                return true
            } else {
                wvGone(binding.appBarMain.content.webView)
                return false
            }
        }

        @Suppress("DEPRECATION")
        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUri: String?
        ) {
            super.onReceivedError(view, errorCode, description, failingUri)

            if (errorCode == 0) {
                view?.visibility = View.GONE
                binding.appBarMain.content.tvCheckConnection.visibility = View.GONE
                onLoadComplete()
            }
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(
                view,
                error!!.errorCode,
                error.description.toString(),
                request!!.url.toString()
            )
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLoadComplete()
        }
    }


}