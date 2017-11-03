package com.ott.webtv;

import java.util.List;

import stb.input.keyboard_dialog.KeyboardDialog;
import stb.input.keyboard_dialog.KeyboardDialogStatusListener;
import stb.input.keyboard_dialog.KeyboardDialogUtil;
import stb.input.keyboard_dialog.TextSettingParams;
import android.content.Context;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ott.webtv.core.DataNode.DATA_TYPE;


public class SearchView{
		
		private PopupWindow mPopWindow_Search;
		private View SearchPopView;
		private KeyboardDialog mKeyboardDialog = null;
		
		private Button SearchButton[]=new Button[3];
		private TextView SearchText;	
		private Context mcontext;
		private List<String> searchList;
		private List<DATA_TYPE> searchType;
		private View textBg;
		int searchLength;
		
		
		enum SearchType{
			SEARCH_VIDEO,
			SEARCH_PERSON,
			SEARCH_SPECIAL
		}
		
		public SearchView(Context context){
			this.mcontext = context;
		}
		
		Handler handler = new Handler();
		
		public void showSearchPopWindow(List<String> searchList,List<DATA_TYPE> searchType) {
			// TODO Auto-generated method stub
			this.searchList = searchList;
			this.searchType = searchType;
			this.searchLength = searchList.size();
			if (mPopWindow_Search == null) {
				init_SearchPopWindow();
			}
			mPopWindow_Search.showAtLocation(SearchPopView, Gravity.NO_GRAVITY,0, 0);
			SearchText.requestFocus();
			
		}


		private void init_SearchPopWindow() {
			// TODO Auto-generated method stub
			SearchPopView = LayoutInflater.from(mcontext).inflate(R.layout.video_search, null, false);
			mPopWindow_Search = new PopupWindow(SearchPopView, 1280, 720, true);
			
			SearchButton[0] = (Button) SearchPopView.findViewById(R.id.OTT_Video_Search_1);
			SearchButton[1] = (Button) SearchPopView.findViewById(R.id.OTT_Video_Search_2);
			SearchButton[2] = (Button) SearchPopView.findViewById(R.id.OTT_Video_Search_3);
			textBg =  SearchPopView.findViewById(R.id.search_text_bg);
			
			SearchText = (TextView) SearchPopView.findViewById(R.id.OTT_Video_Edit_Search);
			SearchText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			SearchPopView.setOnKeyListener(keyListener);

			mPopWindow_Search.setFocusable(true);
			SearchPopView.setFocusable(true);
			SearchPopView.setFocusableInTouchMode(true);
			SearchText.setOnKeyListener(keyListener);
//			SearchText.setOnEditorActionListener(editorActionListener);
			SearchPopView.setOnKeyListener(keyListener);
			SearchPopView.setOnClickListener(backListener);
			
			SearchText.setOnHoverListener(new OnHoverListener() {
				
				@Override
				public boolean onHover(View arg0, MotionEvent arg1) {
					// TODO Auto-generated method stub
					switch(arg1.getAction()){
						case MotionEvent.ACTION_HOVER_ENTER:
							textBg.setVisibility(View.GONE);
							SearchText.setBackground(mcontext.getResources().getDrawable(R.drawable.edittext_corner_focus_bg));
							setKeyboardParam();
							break;
						case MotionEvent.ACTION_HOVER_EXIT:
							SearchText.setBackground(mcontext.getResources().getDrawable(R.color.transparent_background));
							textBg.setVisibility(View.VISIBLE);
							break;
						
					}
					return false;
				}
			});
			SearchText.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if(hasFocus){
						textBg.setVisibility(View.GONE);
						SearchText.setBackground(mcontext.getResources().getDrawable(R.drawable.edittext_corner_focus_bg));
						setKeyboardParam();
					}else{
						SearchText.setBackground(mcontext.getResources().getDrawable(R.color.transparent_background));
						textBg.setVisibility(View.VISIBLE);
					}
				}
			});
			
			
			for(int i=0;i<searchLength;i++){
				SearchButton[i].setVisibility(View.VISIBLE);
				SearchButton[i].setText(searchList.get(i));
				SearchButton[i].setOnKeyListener(keyListener);
				SearchButton[i].setOnClickListener(buttonOnClick);
			}
		}
	
		private void setKeyboardParam(){
			System.out.println();
			mKeyboardDialog = KeyboardDialogUtil.obtainKeyboardDialog(mcontext);
			TextSettingParams textSettingParams = new TextSettingParams();
	    	textSettingParams.mMaxLength = 20;
	    	textSettingParams.mInputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
	    	mKeyboardDialog.showEditDialog(SearchText.getText().toString(), mKeyboardDialogStatusListener, textSettingParams);
		}
		
		private KeyboardDialogStatusListener mKeyboardDialogStatusListener = new KeyboardDialogStatusListener(){

			@Override
			public void onDialogDone(String ret) {
				SearchText.setText(ret);
				SearchButton[0].requestFocus();
			}

			@Override
			public void onDialogForceClose() {
				if(!SearchButton[0].isFocused()){
					SearchButton[0].requestFocus();
				}
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
		
		OnKeyListener keyListener = new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == KeyEvent.ACTION_DOWN){
					if(keyCode == KeyEvent.KEYCODE_BACK ){
						if (mPopWindow_Search != null) 
							mPopWindow_Search.dismiss();
							mPopWindow_Search = null;
							((VideoBrowser)mcontext).doForPopWindowBack();
					}
				}
				return false;
			}
		};
		
		OnClickListener buttonOnClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				String searchText = SearchText.getText().toString();				
				if(searchText.length() == 0){
					PopDialog pop = new PopDialog(mcontext, null);
					pop.showToast(R.string.input_searchContent);
					return;
				}
				
				if (mPopWindow_Search != null){
					mPopWindow_Search.dismiss();
					mPopWindow_Search = null;
				}
				
				for(int i=0;i<searchLength;i++){
					if(v == SearchButton[i]){
						((VideoBrowser)mcontext).getSearchVideoData(searchType.get(i),searchText);
						break;
					}
				}
				
			}
		};
		
		OnClickListener backListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mPopWindow_Search != null) 
					mPopWindow_Search.dismiss();
					mPopWindow_Search = null;
					((VideoBrowser)mcontext).doForPopWindowBack();
			}
		};
	}