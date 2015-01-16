package com.example.infotempo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.about_layout);
	    TextView tv = (TextView)findViewById(R.id.textViewAbout1);
	    PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			tv.setMovementMethod(LinkMovementMethod.getInstance());
		    tv.setText(Html.fromHtml("<b>"+getResources().getString(R.string.app_name)+"</b><br>version "+pInfo.versionName
		    		+"<br><br><br>Application réalisée par<br><font size=\"+1\"><a href=\"http://www.example.com\">xxx</a></font>" ));
		} catch (NameNotFoundException e) {
		}
		tv = (TextView)findViewById(R.id.textViewAbout2);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(Html.fromHtml("Contact: <a href=\"mailto:contact@example.com?subject="
				+getResources().getString(R.string.app_name)+"\">contact@example.com</a>"));
		
		ImageView img = (ImageView)findViewById(R.id.imageViewAbout1);
		img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.example.infotempo"));
		    	startActivity(browserIntent);
		    }
		});
		img = (ImageView)findViewById(R.id.imageViewAbout2);
		img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
		    	startActivity(browserIntent);
		    }
		});
	}

}
