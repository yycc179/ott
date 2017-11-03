package com.ott.webtv;

import com.ott.webtv.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PopDialog {
	private View mView;
	private AlertDialog mDialog;
	private Context context;

	private AlertDialog editVideoDialog;

	PopDialog(Context context, DialogInterface.OnKeyListener cancelListener) {
		this.context = context;
		mView = LayoutInflater.from(context).inflate(R.layout.alertxml, null,
				false);
		mDialog = new AlertDialog.Builder(context).create();
		mDialog.setOnKeyListener(cancelListener);
	}

	public void showAnimation() {
		mDialog.show();
		mDialog.getWindow().setContentView(mView);
	}

	public void closeAnimation() {
		mDialog.dismiss();
	}

	public void showWarning(int msg, DialogInterface.OnClickListener ok) {
		new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.warning).setMessage(msg)
				.setCancelable(false).setPositiveButton(R.string.ok, ok).show();
	}

	public void showConfirm(int msg, DialogInterface.OnClickListener ok,
			final DialogInterface.OnClickListener cancel) {
		new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.message).setMessage(msg)
				.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {
						// TODO Auto-generated method stub
						if (cancel != null) {
							cancel.onClick(arg0, 0);
						}
					}

				}).setPositiveButton(R.string.ok, ok)
				.setNegativeButton(R.string.cancel, cancel).show();

	}

	public void showEditVideoMenu(int titleTxt, boolean isMouse,
			OnClickListener firstBtn, OnClickListener secondBtn) {

		View editVideoView = LayoutInflater.from(context).inflate(
				R.layout.edit_video_dialog, null, false);
		editVideoDialog = new AlertDialog.Builder(context,
				R.style.EditVideoTheme).create();
		TextView mTitle = (TextView) editVideoView
				.findViewById(R.id.edit_video_title);
		Button firstButton = (Button) editVideoView
				.findViewById(R.id.edit_video_first_button);
		Button secondButton = (Button) editVideoView
				.findViewById(R.id.edit_video_second_button);

		mTitle.setText(titleTxt);
		firstButton.setText(R.string.editVideoBtn1);
		firstButton.setOnClickListener(firstBtn);

		if (isMouse) {
			firstButton.setVisibility(View.GONE);
		}

		secondButton.setText(R.string.editVideoBtn2);
		secondButton.setOnClickListener(secondBtn);

		editVideoDialog.show();
		editVideoDialog.getWindow().setContentView(editVideoView);

	}

	public void closeEditVideoMenu() {
		editVideoDialog.dismiss();
	}

	public void showToast(int msg) {
		Toast toast = new Toast(context);
		TextView view = new TextView(context);
		view.setText(msg);
		view.setBackground(context.getResources().getDrawable(
				R.drawable.toast_frame));
		view.setPadding(20, 10, 20, 10);
		view.setTextSize(26);
		view.setTextColor(Color.WHITE);
		toast.setView(view);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();
	}
}