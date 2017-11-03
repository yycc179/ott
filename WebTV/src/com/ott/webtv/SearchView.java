package com.ott.webtv;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.ott.webtv.core.DataNode.DATA_TYPE;
import com.ott.webtv.R;


public class SearchView{
		
		private PopupWindow mPopWindow_Search;
		private View SearchPopView;
		private Button SearchButton[]=new Button[3];
		private EditText SearchText;
		private Context mcontext;
		private List<String> searchList;
		private List<DATA_TYPE> searchType;
		int searchLength;
		enum SearchType{
			SEARCH_VIDEO,
			SEARCH_PERSON,
			SEARCH_SPECIAL
		}
		public SearchView(Context context){
			this.mcontext = context;
		}
		
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
			
			// for display the softKey when the EditText is focused
			new Handler().postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                    InputMethodManager imm = (InputMethodManager) mcontext.getSystemService(Context.INPUT_METHOD_SERVICE);
	                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	            }
			}, 70);
		}


		private void init_SearchPopWindow() {
			// TODO Auto-generated method stub
			SearchPopView = LayoutInflater.from(mcontext).inflate(R.layout.video_search, null, false);
			mPopWindow_Search = new PopupWindow(SearchPopView, 1280, 720, true);
			
			SearchButton[0] = (Button) SearchPopView.findViewById(R.id.OTT_Video_Search_1);
			SearchButton[1] = (Button) SearchPopView.findViewById(R.id.OTT_Video_Search_2);
			SearchButton[2] = (Button) SearchPopView.findViewById(R.id.OTT_Video_Search_3);
			
			SearchText = (EditText) SearchPopView.findViewById(R.id.OTT_Video_Edit_Search);
			
			mPopWindow_Search.setFocusable(true);
			SearchPopView.setFocusable(true);
			SearchPopView.setFocusableInTouchMode(true);
			SearchText.setOnKeyListener(keyListener);
			SearchPopView.setOnKeyListener(keyListener);
			
			
			for(int i=0;i<searchLength;i++){
				SearchButton[i].setVisibility(View.VISIBLE);
				SearchButton[i].setText(searchList.get(i));
				SearchButton[i].setOnKeyListener(keyListener);
				SearchButton[i].setOnClickListener(buttonOnClick);
			}
			
		}
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
					}else if(keyCode == KeyEvent.KEYCODE_ENTER){
						SearchButton[0].requestFocus();
					}
				}
				return false;
			}
		};
		
		OnClickListener buttonOnClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mPopWindow_Search != null){
					mPopWindow_Search.dismiss();
					mPopWindow_Search = null;
				}
				String searchText = SearchText.getText().toString();
				for(int i=0;i<searchLength;i++){
					if(v == SearchButton[i]){
						((VideoBrowser)mcontext).getSearchVideoData(searchType.get(i),searchText);
						break;
					}
				}
				
			}
		};
	}