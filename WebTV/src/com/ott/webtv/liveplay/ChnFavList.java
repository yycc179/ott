package com.ott.webtv.liveplay;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ott.webtv.R;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.CoreHandler.Customer_CMD;
import com.ott.webtv.core.LiveDataManager;
import com.ott.webtv.core.LiveDataManager.LiveBaseNode;

public class ChnFavList extends PopupWindow{
	
	private Activity activity;
	private TextView titleName;
	private ListView listview;
	private View favInfoBar;
	public View chnList;
	private View layoutView;
	
	private final String CHNLIST = "Channel List";
	private final String FAVLIST = "Favorite List";
	
	private List<LiveBaseNode> AlChnlist;
	private List<LiveBaseNode> AlFavlist;
	
	private Handler playerhandler;
	
	private LiveDataManager liveDataMgr = LiveDataManager.getInstance();
	private ChangePinCode changepincode;
	
	private boolean mIsFav = false;
	private int mCurSelIndexForFav = 0;
	private int mCurPlayIndex = 0;
	private listAdapter adapter;
	
	private PopMsg popmsg;
	
	private boolean getFavListOk = false; // for get Favlist before add FavChannel; 
	private boolean getFavFromRedKey = false;
	
	private String TAG = "---ChanList---";
	
	CoreHandler core = CoreHandler.getInstace();
	
	ChnFavList(Activity activity,Handler handler){
		this.activity = activity;
		this.playerhandler = handler;
		
		initChanPopWindow();
		
		AlChnlist = liveDataMgr.getChnList();
		
		titleName.setText(CHNLIST);
		
		initOthers();
	}
	
	public void initOthers(){
		changepincode = new ChangePinCode(activity);
		popmsg = new PopMsg(activity);
		popmsg.setListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				popmsg.closePopWindow();
			}
		},null );
	}
	
	public void show(int playIndex){
		mCurPlayIndex = playIndex;
		updateListView(AlChnlist);
		showAtLocation(layoutView, Gravity.NO_GRAVITY, 24, 221);
	}
	
	public void cleanData(){
		AlChnlist.clear();
		AlFavlist.clear();
	};
	
	private void initChanPopWindow(){
		
		layoutView = LayoutInflater.from(activity).inflate(R.layout.live_chnlist, null, false);
		favInfoBar = layoutView.findViewById(R.id.chnlistInfoBar_fav);
		titleName = (TextView) layoutView.findViewById(R.id.chnListName);
		listview = (ListView) layoutView.findViewById(R.id.listview_channellist);
		
		setContentView(layoutView);
		setWidth(350);
		setHeight(439);
		setFocusable(true);
		
		layoutView.setFocusable(true);
		layoutView.setOnKeyListener(keyPressedListener);
		listview.setOnKeyListener(keyPressedListener);
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				mCurPlayIndex = arg2;
				Message msg = playerhandler.obtainMessage(MainActivity.PLAY_SELECTED_CHANNEL,arg2,0);
				msg.sendToTarget();
			}
		});
	}

	public void updateListView(List<LiveBaseNode> list){
		
		adapter = new listAdapter();
		adapter.setListData(list);
		listview.setAdapter(adapter);
		if(list == AlChnlist){
			mIsFav = false;
			titleName.setText(CHNLIST);
			favInfoBar.setVisibility(View.GONE);
			listview.setSelection(mCurPlayIndex);
		}else if(list == AlFavlist){
			mIsFav = true;
			titleName.setText(FAVLIST);
			favInfoBar.setVisibility(View.VISIBLE);
		}
		listview.requestFocus();
	}
	
	
	
	int KEYCODE_RED = 0;
	int KEYCODE_BLUE = 1;
	int KEYCODE_GREEN = 2;///////
	
	OnKeyListener keyPressedListener = new OnKeyListener() {
		
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			if(arg2.getAction() == KeyEvent.ACTION_DOWN){
				
				if(arg1 == KeyEvent.KEYCODE_BACK){
					changepincode = null;
					dismiss();
				}
				
				if(!mIsFav){
					if(arg1 == KEYCODE_RED){
//						if(arg1 == KeyEvent.KEYCODE_RED){
						
						getFavFromRedKey = true;
						
						if(getFavListOk){
							System.out.println("----get fav  second ----");
							AlFavlist =  liveDataMgr.getFavList();
							updateListView(AlFavlist);
						}else{
							System.out.println("----get fav  first ----");
//							liveData.sendCmdToGetFavListData();
							
							core.loadCustomerData(Customer_CMD.LIVE_CHNFAV_GET_FAV_LIST.ordinal(), 0, null);
						}
						
					}else if(arg1 == KEYCODE_GREEN){
//					}else if(arg1 == KeyEvent.KEYCODE_GREEN){
						
						getFavFromRedKey = false;
						favlistAddChannel();
						
					}else if(arg1 == KEYCODE_BLUE){
//					}else if(arg1 == KeyEvent.KEYCODE_BLUE){
						
						if(changepincode == null){
							changepincode = new ChangePinCode(activity);
						}
						changepincode.show();
					}
				}else{
//					if(arg1 == KeyEvent.KEYCODE_BLUE){
					 if(arg1 == KEYCODE_BLUE){
						
						updateListView(AlChnlist);
						
//					}else if(arg1 == KeyEvent.KEYCODE_RED){
					}else if(arg1 == KEYCODE_RED){
						
						favlistDelChannel();
					}
				}
			}
			return false;
		}
	};
	
	private void favlistDelChannel(){
		mCurSelIndexForFav = listview.getSelectedItemPosition();
		
		if(AlFavlist.size() > 0){
//			liveData.sendCmdToDelFavChannel(AlFavlist.get(mCurSelIndexForFav));
			
			core.loadCustomerData(Customer_CMD.LIVE_CHNFAV_DEL_FAV.ordinal(), 0, AlFavlist.get(mCurSelIndexForFav));
		}
	}
	
	private void favlistAddChannel(){
		System.out.println("--------get Ffava list  = "+getFavListOk);
		mCurSelIndexForFav = listview.getSelectedItemPosition();
		if(!getFavListOk){
//			liveData.sendCmdToGetFavListData();
			
			core.loadCustomerData(Customer_CMD.LIVE_CHNFAV_GET_FAV_LIST.ordinal(), 0, null);
		}else{
//			liveData.sendCmdToAddFavChannel(AlChnlist.get(mCurSelIndexForFav));
			
			core.loadCustomerData(Customer_CMD.LIVE_CHNFAV_ADD_FAV.ordinal(), 0, AlChnlist.get(mCurSelIndexForFav));
		}
	}
	
	public void handlerMessageFromPlayer(int what,int arg1,int arg2,Object obj){
		if(what == CoreHandler.Callback.CBK_GET_CUSTOMER_DONE.ordinal()){
			System.out.println(TAG+"CallBack = "+arg1);
			if(arg1 == Customer_CMD.LIVE_CHNFAV_GET_FAV_LIST.ordinal()){
				getFavListOk = true;
				if(getFavFromRedKey){
					AlFavlist = liveDataMgr.getFavList();
					updateListView(AlFavlist);
				}else{
//					liveData.sendCmdToAddFavChannel(AlChnlist.get(mCurSelIndexForFav));
					
					core.loadCustomerData(Customer_CMD.LIVE_CHNFAV_ADD_FAV.ordinal(), 0, AlChnlist.get(mCurSelIndexForFav));
				}
			}else if(arg1 == Customer_CMD.LIVE_CHNFAV_ADD_FAV.ordinal()){
				
					AlFavlist = liveDataMgr.getFavList();
					popmsg.showMessage("Add Fav Succ");
					
			}else if(arg1 == Customer_CMD.LIVE_CHNFAV_DEL_FAV.ordinal()){
				
					AlFavlist = liveDataMgr.getFavList();
					if(AlFavlist.size() >= 0){
						adapter.setListData(AlFavlist);
						listview.setAdapter(adapter);
						if(mCurSelIndexForFav == AlFavlist.size()){
							listview.setSelectionFromTop(mCurSelIndexForFav-1,45 * ((mCurSelIndexForFav-1)%8));
						}else{
							listview.setSelectionFromTop(mCurSelIndexForFav,45 * (mCurSelIndexForFav%8));
						}
					}
					popmsg.showMessage("Del Fav Succ");
			}else if(arg1 == Customer_CMD.LIVE_POP_CHANGE_PIN_CODE.ordinal()){
				
				changepincode.handlerMessageFromChnList(what, arg1, arg2, obj);
			}
			
		}else if(what == CoreHandler.Callback.CBK_GET_CUSTOMER_FAIL.ordinal()){
			if(arg1 == Customer_CMD.LIVE_CHNFAV_GET_FAV_LIST.ordinal()){
				getFavListOk = false;
				
				popmsg.showMessage("Get FavList Fail");
			}else if(arg1 == Customer_CMD.LIVE_CHNFAV_ADD_FAV.ordinal()){
				
				popmsg.showMessage("Add Fav Fail");
			}else if(arg1 == Customer_CMD.LIVE_CHNFAV_DEL_FAV.ordinal()){
				
				popmsg.showMessage("Del Fav Fail");
			}else if(arg1 == Customer_CMD.LIVE_POP_CHANGE_PIN_CODE.ordinal()){
				
				changepincode.handlerMessageFromChnList(what, arg1, arg2, obj);
			}
		}
	}
	

	private class listAdapter extends BaseAdapter{
		private List<LiveBaseNode> list;
		public void setListData(List<LiveBaseNode> list){
			this.list = list;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			HoldView holdview=null;
			if(arg1 == null){
				holdview = new HoldView();
				arg1 = LayoutInflater.from(activity).inflate(R.layout.live_chanlist_item,null, false);
				holdview.ChanNo = (TextView) arg1.findViewById(R.id.chnNo);
				holdview.ChanName = (TextView) arg1.findViewById(R.id.chnName);
				arg1.setTag(holdview);
			}else{
				holdview = (HoldView)arg1.getTag();
			}
			
			if(arg0 % 2 == 0){
				arg1.setBackgroundColor(Color.parseColor("#2EAAAAAA"));
			}else{
				arg1.setBackgroundColor(Color.parseColor("#2E575757"));
			}
			int iChnNo = list.get(arg0).getCid();
			String sChnNo =  "0000".substring(String.valueOf(iChnNo).length())+iChnNo;
			holdview.ChanNo.setText(sChnNo);
			holdview.ChanName.setText((list.get(arg0).getChnName()));
			return arg1;
		}
		
		class HoldView{
			TextView ChanNo,ChanName;
		}
		
	}
}