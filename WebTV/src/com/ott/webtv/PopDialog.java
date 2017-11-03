package com.ott.webtv;

import com.ott.webtv.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class PopDialog {
	// private PopupWindow mWindow;
	// private View view;
	// private Button mButtonOk;
	// private TextView tv;
	//
	// private String infoText;
	// private Context context;

	// private PopupWindow mWindow;
	// private View mView;
	// private Button mButtonOk;
	// private TextView mText;

	private View mView;
	private AlertDialog mDialog;
	// private AlertDialog.Builder mBuilder;
	private Context context;

	// PopDialog(Context context, String warnInfo) {
	// this.context = context;
	// this.infoText = warnInfo;
	// }

	PopDialog(Context context, DialogInterface.OnKeyListener cancelListener) {
		// mView = LayoutInflater.from(context).inflate(R.layout.popmsg, null,
		// false);
		this.context = context;
		mView = LayoutInflater.from(context).inflate(R.layout.alertxml, null,
				false);
		mDialog = new AlertDialog.Builder(context).create();
		mDialog.setOnKeyListener(cancelListener);
		// mWindow = new PopupWindow(mView, 420, 216, true);
		// mButtonOk = (Button) mView.findViewById(R.id.pop_ok);
		// mText = (TextView) mView.findViewById(R.id.pop_content);
		// mView.setFocusable(true);
		// mView.setFocusableInTouchMode(true);
	}

	public void showAnimation() {
		mDialog.show();
		mDialog.getWindow().setContentView(mView);
	}

	public void closeAnimation() {
		mDialog.dismiss();
	}

	public void showWarning(String msg, DialogInterface.OnClickListener ok) {
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);

		mBuilder.setTitle(R.string.warning);
		mBuilder.setMessage(msg).setPositiveButton(R.string.ok, ok).create()
				.show();
	}

	public void showConfirm(int msg, DialogInterface.OnClickListener yes,
			DialogInterface.OnClickListener no) {
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);

		mBuilder.setTitle(R.string.message);
		mBuilder.setMessage(msg).setCancelable(false)
				.setPositiveButton(R.string.yes, yes)
				.setNegativeButton(R.string.no, no).create().show();

	}

	public void showToast(int msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
}