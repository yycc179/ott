package com.ott.webtv.liveplay;

import com.ott.webtv.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class PopMsg {

	private PopupWindow mWindow;
	private View mViewConfirm;
	
	private Button mButtonOk;
	private TextView mText;

	private View mViewPassword;
	private ImageButton mPassword[]= new ImageButton[4];
	private int mInputPassword[] = new int[4];
	
	private AlertDialog mDialog;
	private ProgressDialog myDialog;
	
	private Handler handler;

	PopMsg(Context context) {
		
		mViewConfirm = LayoutInflater.from(context).inflate(R.layout.popmsg, null,
				false);
		mViewPassword = LayoutInflater.from(context).inflate(R.layout.live_password, null,
				false);
		
		
		
		mDialog = new AlertDialog.Builder(context).create();
		myDialog = new ProgressDialog(context);

		mButtonOk = (Button) mViewConfirm.findViewById(R.id.pop_ok);
		mText = (TextView) mViewConfirm.findViewById(R.id.pop_content);
		mViewConfirm.setFocusable(true);
		mViewConfirm.setFocusableInTouchMode(true);	
		
		mViewPassword.setFocusable(true);
		mViewPassword.setBackgroundDrawable(new BitmapDrawable());
		
		mPassword[0] = (ImageButton) mViewPassword.findViewById(R.id.IDC_ImageButton_Password1);
		mPassword[0].setOnKeyListener(new UnitOnKeyListenrer(0));
		
		mPassword[1] = (ImageButton) mViewPassword.findViewById(R.id.IDC_ImageButton_Password2);
		mPassword[1].setOnKeyListener(new UnitOnKeyListenrer(1));

		mPassword[2] = (ImageButton) mViewPassword.findViewById(R.id.IDC_ImageButton_Password3);
		mPassword[2].setOnKeyListener(new UnitOnKeyListenrer(2));

		mPassword[3] = (ImageButton) mViewPassword.findViewById(R.id.IDC_ImageButton_Password4);
		mPassword[3].setOnKeyListener(new UnitOnKeyListenrer(3));
	}	
	
	
	public void setHandler(Handler handler){
		this.handler = handler;
	}
	public void setListener(OnClickListener clickListener, DialogInterface.OnKeyListener cancelListener){
		mButtonOk.setOnClickListener(clickListener);
		mDialog.setOnKeyListener(cancelListener);
	}
	
	public void showMessage(String err){
		mText.setText(err);
		if(mWindow == null){
			mWindow = new PopupWindow(mViewConfirm, 420, 216, true);
		}
		mWindow.showAtLocation(mViewConfirm, Gravity.NO_GRAVITY, 422, 211);
		mButtonOk.requestFocus();
	}
	
	public void showPwd(){
		if(mWindow == null){
			mWindow = new PopupWindow(mViewPassword, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		}
		mWindow.showAtLocation(mViewPassword, Gravity.CENTER, 0, 0);
		mPassword[0].requestFocus();
	}
	
	public void closePopWindow() {
		mWindow.dismiss();
	}
	
	public void showLoading(){
		myDialog.setMessage("Loading");
		myDialog.show();
	}
	
	public void closeLoading() {
		mDialog.dismiss();
		myDialog.dismiss();
	}
	
	
	class UnitOnKeyListenrer implements OnKeyListener {
		int index;
		boolean mPassFlag;
		
		UnitOnKeyListenrer(){
			
		}
		
		public UnitOnKeyListenrer(int index) {
			this.index = index;
			mInputPassword[index] = -1;
		}

		@Override
		public boolean onKey(View arg0, int keycode, KeyEvent keyEvent) {
			if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keycode) {
				case KeyEvent.KEYCODE_0:
				case KeyEvent.KEYCODE_1:
				case KeyEvent.KEYCODE_2:
				case KeyEvent.KEYCODE_3:
				case KeyEvent.KEYCODE_4:
				case KeyEvent.KEYCODE_5:
				case KeyEvent.KEYCODE_6:
				case KeyEvent.KEYCODE_7:
				case KeyEvent.KEYCODE_8:
				case KeyEvent.KEYCODE_9:
					mInputPassword[index] = keycode - KeyEvent.KEYCODE_0;
					mPassword[index].setSelected(true);
					inputProcess();
					return true;
				case KeyEvent.KEYCODE_BACK:
					mWindow.dismiss();
					return true;
				}
			}
			return false;
		}

		public void inputProcess() {
			switch (index) {
			case 0:
			case 1:
			case 2:

				mPassword[index + 1].requestFocus();
				break;
			case 3:
				String inputCode = "";
				for (int i = 0; i < 4; i++) {
						// judge the password
					if(mInputPassword[i] == -1){
						return;
					}
					inputCode += Integer.toString(mInputPassword[i]);
					System.out.println("-------- the new inputCode is "+inputCode);
				}
				// send the password to net to judge
				Message msg = handler.obtainMessage();
				msg.what = MainActivity.GET_PROTECTED_URL;
				msg.obj = inputCode;
				msg.sendToTarget();
				closePopWindow();
				break;
			}
		}

	};
	

}