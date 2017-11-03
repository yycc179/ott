package com.ott.webtv;

import java.util.ArrayList;
import java.util.List;

import stb.input.keyboard_dialog.KeyboardDialog;
import stb.input.keyboard_dialog.KeyboardDialogStatusListener;
import stb.input.keyboard_dialog.KeyboardDialogUtil;
import stb.input.keyboard_dialog.TextSettingParams;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.ott.webtv.core.ALParser;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.CoreHandler.Customer_CMD;

public class LoginView extends Activity {

	private Button mUserId, mPassWord;
	private Button mbuttonOk;
	private CheckBox cbSaveInfo;
	private SharedPreferences mSharedPreferences;
	private CoreHandler core;
	private PopDialog pop;
	private KeyboardDialog mKeyboardDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mUserId = (Button) findViewById(R.id.login_userid);
		mPassWord = (Button) findViewById(R.id.login_password);

		mbuttonOk = (Button) findViewById(R.id.login_ok);

//		cbShowPwd = (CheckBox) findViewById(R.id.login_showPwd);
		cbSaveInfo = (CheckBox) findViewById(R.id.login_saveInfo);

		
		mUserId.setOnClickListener(buttonClick);
		mPassWord.setOnClickListener(buttonClick);
		mbuttonOk.setOnClickListener(buttonClick);
		
		mUserId.setOnHoverListener(hoverListener);
		mPassWord.setOnHoverListener(hoverListener);
		mbuttonOk.setOnHoverListener(hoverListener);
		cbSaveInfo.setOnHoverListener(hoverListener);

//
//		cbShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView,
//					boolean isChecked) {
//				// TODO Auto-generated method stub
//				if (isChecked) {
//					// hide
//					mPassWord
//							.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//				} else {
//					// show
//					mPassWord.setInputType(InputType.TYPE_CLASS_TEXT
//							| InputType.TYPE_TEXT_VARIATION_PASSWORD);
//				}
//			}
//		});

		cbSaveInfo.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					Editor editor = mSharedPreferences.edit();
					editor.putString("userId", mUserId.getText().toString());
					editor.putString("passWord", mPassWord.getText().toString());
					editor.commit();
				} else {
					Editor editor = mSharedPreferences.edit();
					editor.clear();
					editor.commit();
				}
			}
		});

		
		mSharedPreferences = getPreferences(MODE_PRIVATE);
		String userId = mSharedPreferences.getString("userId", "");
		String passWord = mSharedPreferences.getString("passWord", "");
		mUserId.setText(userId);
		mPassWord.setText(passWord);

		cbSaveInfo.setChecked(true);

		initData();
		
		mUserId.requestFocus();
		
	}

	OnHoverListener hoverListener = new OnHoverListener() {
		
		@Override
		public boolean onHover(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			switch(arg1.getAction()){
			case MotionEvent.ACTION_HOVER_ENTER:
				arg0.requestFocusFromTouch();
				break;
			}
			return false;
		}
	};
	OnClickListener  buttonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v == mPassWord || v == mUserId){
				setKeyboardParam((Button) v);
			}else if(v == mbuttonOk){
				if (cbSaveInfo.isChecked()) {
					Editor editor = mSharedPreferences.edit();
					editor.putString("userId", mUserId.getText().toString());
					editor.putString("passWord", mPassWord.getText().toString());
					editor.commit();
				}
				checkPassWord();
			}
		}
	};
	
	private void initData() {
		pop = new PopDialog(this, null);
		core = CoreHandler.getInstace();
		core.setHandler(handler);
	}

	private void setKeyboardParam(Button tv){
		System.out.println();
		mKeyboardDialog = KeyboardDialogUtil.obtainKeyboardDialog(this);
		TextSettingParams textSettingParams = new TextSettingParams();
    	textSettingParams.mMaxLength = 20;
    	textSettingParams.mInputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
    	mKeyboardDialog.showEditDialog(tv.getText().toString(), mKeyboardDialogStatusListener, textSettingParams);
    	
	}
	
	private KeyboardDialogStatusListener mKeyboardDialogStatusListener = new KeyboardDialogStatusListener(){

		@Override
		public void onDialogDone(String ret) {
			if(mUserId.hasFocus()){
				mUserId.setText(ret);
			}else if(mPassWord.hasFocus()){
				mPassWord.setText(ret);
			}
		}

		@Override
		public void onDialogForceClose() {
		}


		@Override
		public void onTextChange(String arg0) {
		}

		@Override
		public void onDialogShow() {
			// TODO Auto-generated method stub
		}
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			return super.onKeyDown(keyCode, event);
		}

    };	
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.putExtra("LoginCancel", true);
			setResult(0, intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void checkPassWord() {
		String userid = mUserId.getText().toString();
		String passWord = mPassWord.getText().toString();
		System.out.println("-----<<<<< the userid  >>>> = " + userid);
		System.out.println("-----<<<<the passWord>>>> = " + passWord);
		List<String> list = new ArrayList<String>();
		list.add(userid);
		list.add(passWord);
		pop.showAnimation();
		core.loadCustomerData(Customer_CMD.CUSTOMER_LOGIN_SET.ordinal(), 0,
				list);
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (msg.what == CoreHandler.Callback.CBK_GET_CUSTOMER_DONE
					.ordinal()) {
				if (msg.arg1 == Customer_CMD.CUSTOMER_LOGIN_SET.ordinal()) {
					pop.closeAnimation();
					System.out.println("---- login succ ------");
					Intent intent = new Intent();
					intent.putExtra("LoginSuccess", true);
					setResult(0, intent);
					finish();
				}
			} else if (msg.what == CoreHandler.Callback.CBK_GET_CUSTOMER_FAIL
					.ordinal()) {
				System.out.println("----login fail ------");
				pop.closeAnimation();
				
				if(msg.arg2 == ALParser.PARSE_IOERROR){
					pop.showToast(R.string.err_io);
				}else {
					pop.showToast(R.string.site_login_fail);
				}
			}

		};
	};

}