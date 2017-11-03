package com.ott.webtv;

import java.util.HashMap;
import java.util.Map;

import com.ott.webtv.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginView extends Activity{

		private EditText mUserId,mPassWord;
		private Button mbuttonOk;
		private TextView mWarnInfo;
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|
		           WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

			findViewById(R.id.loginxml).setVisibility(View.VISIBLE);
			
			mUserId = (EditText)findViewById(R.id.login_userid);
			mPassWord = (EditText)findViewById(R.id.login_password);
			
			mbuttonOk = (Button)findViewById(R.id.login_ok);
			mWarnInfo = (TextView)findViewById(R.id.login_warnInfo);
			mWarnInfo.setVisibility(View.GONE);
			System.out.println(""+mUserId.length()+"   "+mUserId.getText().toString().length());
			mUserId.setSelection(mUserId.length());
			
			mbuttonOk.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					checkPassWord();
					//new HttpUtils(" ").execute().getResult();
				}
			});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent();
			intent.putExtra("LoginCancel", true);
			setResult(0,intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void checkPassWord(){
		String userid = mUserId.getText().toString();
		String passWord = mPassWord.getText().toString();
		System.out.println("-----<<<<< the userid  >>>> = "+userid);
		System.out.println("-----<<<<the passWoHash>>>> = "+passWord);
		Map<String,String> map = new HashMap<String,String>();
		map.put("userName", userid);
		map.put("passWord", passWord);
		
	}

}