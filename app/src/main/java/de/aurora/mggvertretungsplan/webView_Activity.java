package de.aurora.mggvertretungsplan;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import android.support.v7.app.AppCompatActivity;
 
public class webView_Activity extends AppCompatActivity {
 
	private WebView webView;
	Toolbar toolbar;
	
	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		
		getWindow().setContentView(R.layout.webview);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		
		toolbar = (Toolbar) findViewById(R.id.webview_toolbar);
		setSupportActionBar(toolbar);
		//toolbar.setAlpha(20);
		toolbar.setTitle("Website");
		//toolbar.setLogo(R.drawable.ic_launcher);
		if(Build.VERSION.SDK_INT >= 21){
			toolbar.setElevation(25);
		}
		
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		
		web();
	}
	
	
	public void web(){
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBuiltInZoomControls(true);
		//webView.loadUrl(getString(R.string.Url1));
		
		final Activity activity = this;
		 webView.setWebChromeClient(new WebChromeClient() {
		   public void onProgressChanged(WebView view, int progress) {
		     // Activities and WebViews measure progress with different scales.
		     // The progress meter will automatically disappear when we reach 100%
		     activity.setProgress(progress * 1000);
		   }
		 });
		 webView.setWebViewClient(new WebViewClient() {
		   public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		     Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
		   }
		 });

		 webView.loadUrl(getString(R.string.Url1));
	 }
	


	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	case android.R.id.home:
    	Log.v("Vertretungsplan","ActionBar zurück!");
    	finish();
    break;
    default: break;
    }
    return true;
	}

	public webView_Activity() {
		// TODO Auto-generated constructor stub
	}

}
