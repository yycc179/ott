package com.webtv.youporn;

import com.ott.webtv.core.CoreHandler;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivity(CoreHandler.setUp(MainActivity.this, com.ott.webtv.VideoBrowser.class, new Parser()));
//		startActivity(CoreHandler.setUp(MainActivity.this, null, null));
		finish();
	}

}
