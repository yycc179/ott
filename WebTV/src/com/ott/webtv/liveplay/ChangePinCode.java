package com.ott.webtv.liveplay;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;

import com.ott.webtv.R;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.CoreHandler.Customer_CMD;
import com.ott.webtv.core.LiveDataManager.CodeManager;



public class ChangePinCode extends PopupWindow{
	public static final int PASSWORDLENGTH = 8;
	private Context mContext;  
	private String mOldPassword="1234";
	private int[] mInputPassword;
	private String mNewPassword;
	private ImageButton[] mPasswordWindow;
	private CodeManager CodeMgr = new CodeManager();
	private PopMsg popmsg;
	
	CoreHandler core = CoreHandler.getInstace();
	
	public ChangePinCode(Context context){
		super(context);  
		mContext = context;
		mInputPassword = new int[PASSWORDLENGTH];
		showWindow();
		popmsg = new PopMsg(mContext);
//		mOldPassword = CommonSettingManager.getInstance(context.getApplicationContext()).getSystemPassword();
	}

	class UnitOnKeyListenrer implements OnKeyListener {
		int index;

		public UnitOnKeyListenrer(int index) {
			this.index = index;
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
					mPasswordWindow[index].setSelected(true);
					inputProcess();
					return true;
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					return false;
				case KeyEvent.KEYCODE_BACK:
					dismiss();
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
			case 3:
			case 4:
			case 5:
			case 6:
				mPasswordWindow[index + 1].requestFocus();
				break;
			case 7:
				mNewPassword = "";
				mOldPassword = "";
				for (int i = 4; i < 8; i++) {
					mNewPassword += Integer.toString(mInputPassword[i]);
				}
				System.out.println("-------- the new password is "+mNewPassword);
				for (int i = 0; i < 4; i++) {
					mOldPassword += Integer.toString(mInputPassword[i]);
				}
				for(int i = 0; i < 8;i++){
					if(mInputPassword[i] == -1){
						return;
					}
				}
				System.out.println("-------- the new mOldPassword is "+mOldPassword);
				ProcessPassword(mOldPassword,mNewPassword);
				
				break;
			}
		}

	};

	public void handlerMessageFromChnList(int what,int arg1,int arg2,Object obj){
		if(what == CoreHandler.Callback.CBK_GET_CUSTOMER_DONE.ordinal()){
			dismiss();
			popmsg.showMessage("Code Change Succ");
		}else if(what == CoreHandler.Callback.CBK_GET_CUSTOMER_FAIL.ordinal()){
			dismiss();
			popmsg.showMessage("Code Change Failed");
		}
		popmsg.setListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				popmsg.closePopWindow();
			}
		}, null);
	}

	private void ProcessPassword(String oldCode,String newCode){
		CodeMgr.setParentCode(oldCode);
		CodeMgr.setNewCode(newCode);
//		LiveDataUtils.getInstace().sendCmdToSetPinCode(CodeMgr);
		core.loadCustomerData(Customer_CMD.LIVE_POP_CHANGE_PIN_CODE.ordinal(), 0, CodeMgr);
	}
	
	private void showWindow() {
		View password = LayoutInflater.from(mContext).inflate(R.layout.live_changepincode, null,false);
		password.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_background));
		mPasswordWindow = new ImageButton[PASSWORDLENGTH];
		mPasswordWindow[0] = (ImageButton)password.findViewById(R.id.IDC_ImageButton_OldPassword1);
		mPasswordWindow[1] = (ImageButton)password.findViewById(R.id.IDC_ImageButton_OldPassword2);
		mPasswordWindow[2] = (ImageButton)password.findViewById(R.id.IDC_ImageButton_OldPassword3);
		mPasswordWindow[3] = (ImageButton)password.findViewById(R.id.IDC_ImageButton_OldPassword4);
		
		mPasswordWindow[4] = (ImageButton)password.findViewById(R.id.IDC_ImageButton_NewPassword1);
		mPasswordWindow[5] = (ImageButton)password.findViewById(R.id.IDC_ImageButton_NewPassword2);
		mPasswordWindow[6] = (ImageButton)password.findViewById(R.id.IDC_ImageButton_NewPassword3);
		mPasswordWindow[7] = (ImageButton)password.findViewById(R.id.IDC_ImageButton_NewPassword4);
		
		
		setContentView(password);		
		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.MATCH_PARENT);
		setFocusable(true);
		
    	ColorDrawable dw = new ColorDrawable(0x00);
		setBackgroundDrawable(dw);
	}
	
	public void show() {
		
		for(int i = 0; i < mPasswordWindow.length; i++){
			mPasswordWindow[i].setOnKeyListener(new UnitOnKeyListenrer(i));
			mPasswordWindow[i].setSelected(false);
			mInputPassword[i] = -1;
		}
		showAtLocation(getContentView(), Gravity.CENTER, 0, 0);
	}


	public void changeSucessProcess() {
		// Save system password
		dismiss();
	}
	
}
