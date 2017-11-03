package com.ott.webtv.liveplay;


import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ott.webtv.R;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.CoreHandler.Customer_CMD;
import com.ott.webtv.core.LiveDataManager;
import com.ott.webtv.core.LiveDataManager.CodeManager;
import com.ott.webtv.core.LiveDataManager.EventInfo;
import com.ott.webtv.core.LiveDataManager.LiveBaseNode;

@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {
	
	private ChanInfo chanInfo = null;
	
	private PopMsg popmsg = null;
	private int playIndex = 5;
	
	public final static int PLAY_SELECTED_CHANNEL = 100;
	public final static int SET_PLAYER_HANDLER = 101;
	public final static int GET_PROTECTED_URL = 102;
	
	private LiveDataManager liveDataMgr = LiveDataManager.getInstance();
	
	private List<LiveBaseNode> chnDatalist = new ArrayList<LiveBaseNode>();
	private CodeManager CodeMgr = new CodeManager();
	
	private String playUrl = "";
	private String TAG = "----LivePlayer----";
	
	private Epg epg;
	private ChnFavList chnFavList;
	private int tryTimes = 0;
	
	CoreHandler core = CoreHandler.getInstace();
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
		   if(msg.what == PLAY_SELECTED_CHANNEL){
				
				playChanIndex(msg.arg1);
			}else if(msg.what == GET_PROTECTED_URL){
				
				CodeMgr.setCid(chnDatalist.get(playIndex).getCid());
				String pinCode = (String) msg.obj;
				CodeMgr.setParentCode(pinCode);
				
//				liveData.sendCmdToGetChnPlayUrl(CodeMgr);
				
				core.loadCustomerData(Customer_CMD.LIVE_PLAYER_GET_REAL_URL.ordinal(), 0, CodeMgr);
			}
			   
			System.out.println(TAG+"---------- cmd = "+msg.arg1);
			Customer_CMD cmd = Customer_CMD.values()[msg.arg1];
			if(msg.what == CoreHandler.Callback.CBK_GET_CUSTOMER_DONE.ordinal()){
				if(cmd == Customer_CMD.LIVE_PLAYER_GET_CHAN_LIST){
					
					chnDatalist = liveDataMgr.getChnList();
					initData();
					playChanIndex(playIndex);
				}else if(cmd == Customer_CMD.LIVE_PLAYER_GET_REAL_URL){
					
					popmsg.closeLoading();
					playUrl = CodeMgr.getPlayUrl();
					Log.d(TAG,"----- play url = "+playUrl);
					startToPlay(playUrl);
				}else if(cmd == Customer_CMD.LIVE_PLAYER_GET_CHAN_INFO){
					System.out.println("------get chan info in mainactivity ------");
					
					chanInfo.getInfoDataFromEvent(liveDataMgr.getCurEventList());
				}
			}else if(msg.what == CoreHandler.Callback.CBK_GET_CUSTOMER_FAIL.ordinal()){
				popmsg.closeLoading();
				if(cmd == Customer_CMD.LIVE_PLAYER_GET_CHAN_LIST){
					
					if(tryTimes < 3){
//						liveData.sendCmdToGetChnListData();
						core.loadCustomerData(Customer_CMD.LIVE_PLAYER_GET_CHAN_LIST.ordinal(), 0, null);
						
						tryTimes++;
						Log.d(TAG,"----get Chnlist fail  times = "+tryTimes);
					}
				}else if(cmd == Customer_CMD.LIVE_PLAYER_GET_REAL_URL){
					
					popmsg.showMessage("Get Url Fail");
				}else if(cmd == Customer_CMD.LIVE_PLAYER_GET_CHAN_INFO){
					chanInfo.getInfoDataFromChnlist(chnDatalist.get(playIndex));
				}
			}
					
			 if(cmd == Customer_CMD.LIVE_EPG_GET_DAILY_EPG){
				
				epg.handlerMessageFromPlayer(msg.what, msg.arg1, msg.arg2, msg.obj);
				
			}else if((cmd == Customer_CMD.LIVE_CHNFAV_ADD_FAV) || (cmd == Customer_CMD.LIVE_CHNFAV_DEL_FAV) || (cmd == Customer_CMD.LIVE_POP_CHANGE_PIN_CODE)||
					(cmd == Customer_CMD.LIVE_CHNFAV_GET_FAV_LIST)){
				
				chnFavList.handlerMessageFromPlayer(msg.what, msg.arg1, msg.arg2, msg.obj);
			}
				 
		};
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.liveplayer);
		
		popmsg = new PopMsg(this);
		
		core.initializeAll(null, null, handler, 0, null, null);
//		liveData.sendCmdToGetChnListData();
		core.loadCustomerData(Customer_CMD.LIVE_PLAYER_GET_CHAN_LIST.ordinal(), 0, null);
		
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(chnFavList != null){
			chnFavList.cleanData();
		}
		if(epg != null){
			epg.cleanData();
		}
		liveDataMgr.clearData();
		chnDatalist.clear();
	}
	private void initData(){
		
		chanInfo = new ChanInfo(this);
		chnFavList = new ChnFavList(this,handler);
		epg = new Epg(this,handler);
	}
	
	private void openChanInfoBar(){
		
		chanInfo.showChanInfo();
	}
	
	private void openChanList(){
		
		chnFavList.show(playIndex);
	}
	
	private void openEgpView(){
		
		epg.show(playIndex);
	}
	
	@SuppressLint("SimpleDateFormat")
	public String TimeStamp2Date(String timestampString){  
		  Long timestamp = Long.parseLong(timestampString)*1000;  
		  String date = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(timestamp)); 
		  System.out.println("-----date = "+date);
		  return date;  
		} 
	
	
	int  KEYCODE_EPG = 0; // 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		System.out.println("--------- the keyCode = "+keyCode);
		if(keyCode == KeyEvent.KEYCODE_INFO){
			
			System.out.println("----- open the chaninfo ------");
			// request the current channel info
			openChanInfoBar();
		}else if(keyCode == KeyEvent.KEYCODE_ENTER){
			// request the chanList or FavList
			if(chanInfo != null && chanInfo.isOpened()){
				System.out.println("----- close the chaninfo ------");
				chanInfo.closeInfoBar();
			}  
			openChanList();
			
		}else if(keyCode == KeyEvent.KEYCODE_BACK){
			if(chanInfo != null && chanInfo.isOpened()){
				System.out.println("----- close the chaninfo ------");
				
				chanInfo.closeInfoBar();
				return true;
			}
//		}else if(keyCode == KeyEvent.KEYCODE_EPG){
		}else if(keyCode == KEYCODE_EPG){
			
			if(chanInfo != null && chanInfo.isOpened()){
				System.out.println("----- close the chaninfo ------");
				chanInfo.closeInfoBar();
			}
			openEgpView();
			
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			System.out.println("----- playIndex -------"+playIndex);
			
			if(playIndex > 0){
				playIndex--;
				playChanIndex(playIndex);
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
			System.out.println("----- playIndex -------"+playIndex);
			if(playIndex < 30){
				playIndex++;
				playChanIndex(playIndex);
			}
			
		}else if(keyCode == KeyEvent.KEYCODE_1){
			System.out.println("----- key = 1 -------");
			showPopMsg("test for popmsg");
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
//	Handler handlerToPop = new Handler(){
//		public void handleMessage(android.os.Message msg) {
//			else if(msg.what == PLAY_SELECTED_CHANNEL){
//				playChanIndex(msg.arg1);
//			}else if(msg.what == SET_PLAYER_HANDLER){
//				liveData.setCallBackHandler(handler);
//			}
//			
//		};
//	};
	
	private void playChanIndex(int curChanIndex){
		
		handler.removeCallbacks(chanInfo.shwoInfoThread);
		
		playIndex = curChanIndex;
		if((chnFavList != null && !chnFavList.isShowing()) && (epg != null && !epg.isShowing())){
				openChanInfoBar();
		}
		
		boolean isProtect =  (Boolean) chnDatalist.get(playIndex).isProtected();
		int mChanCid = (Integer) chnDatalist.get(playIndex).getCid();
		
		System.out.println("------ the protest = "+isProtect);
		if(isProtect){
			// protected
			PopMsg pwd = new PopMsg(this);
			pwd.setHandler(handler);
			pwd.showPwd();
		}else{
			requestRealUrl(mChanCid);
		}
		
	}
	
	
	public void requestRealUrl(int mChanCid){
		
		//get the play Url and call initPlay to Play;
		
		CodeMgr.setCid(mChanCid);
		CodeMgr.setParentCode(null);
//		liveData.sendCmdToGetChnPlayUrl(CodeMgr);
		core.loadCustomerData(Customer_CMD.LIVE_PLAYER_GET_REAL_URL.ordinal(), 0, CodeMgr);
		
	}
	
	private void startToPlay(String url){
		// call mcplayer to play
		 Toast.makeText(this, "Url:"+url, Toast.LENGTH_LONG).show();
	}
	
	
	public void showPopMsg(String info){
		final PopMsg popMes = new PopMsg(this);
		popMes.showMessage(info);
		popMes.setListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				popMes.closePopWindow();
			}
		}, null);
	
	}
	
	public class ChanInfo {
		
		private Activity mActivity;
		private TextView mChanNum;
		private TextView mChnNo,mChnName;
		private TextView mCurEventTime,mNextEventTime;
		public View mChnInfoLayoutView ;
		private List<EventInfo> chanInfoData = new ArrayList<EventInfo>();
		
		
		ChanInfo(Activity activity){
			this.mActivity = activity;
			findViewById();
		}
		
		private void findViewById(){
			mChnInfoLayoutView = mActivity.findViewById(R.id.chanInfo_bar);
			mChanNum = (TextView) mActivity.findViewById(R.id.tv_num);
			mChnNo = (TextView) mActivity.findViewById(R.id.IDC_TextView_chninfobar_chnno);
			mChnName = (TextView) mActivity.findViewById(R.id.IDC_TextView_chninfobar_chnname);
			mCurEventTime = (TextView) mActivity.findViewById(R.id.IDC_TextView_chninfobar_noweventtime);
			mNextEventTime = (TextView) mActivity.findViewById(R.id.IDC_TextView_chninfobar_nexteventtime);
		}
		
		public void getInfoDataFromEvent(List<EventInfo> infoList){
			this.chanInfoData = infoList;
			updateChanInfoFromEvent();
			mChnInfoLayoutView.setVisibility(View.VISIBLE);
			System.out.println(" start time = "+System.currentTimeMillis());
			handler.postDelayed(shwoInfoThread,10000);
		
		}
		
		public void getInfoDataFromChnlist(LiveBaseNode ChanInfo){
			int cid = ChanInfo.getCid();
			mChanNum.setText(String.valueOf(cid));
			String channum = "0000".substring(String.valueOf(cid).length())+cid;
			mChnNo.setText(channum);
			mChnName.setText(ChanInfo.getChnName());
			mCurEventTime.setText("");
			mNextEventTime.setText("");
			
			mChnInfoLayoutView.setVisibility(View.VISIBLE);
			handler.postDelayed(shwoInfoThread,10000);
			
		}
		
		Runnable shwoInfoThread = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				closeInfoBar();
				System.out.println(" end time = "+System.currentTimeMillis());
			}
		};
		
		public void showChanInfo(){
			handler.postDelayed(shwoInfoThread,10000);
			System.out.println("------ play name = "+chnDatalist.get(playIndex).getChnName());
//			liveData.sendCmdToGetCurEpgData(chnDatalist.get(playIndex));
			core.loadCustomerData(Customer_CMD.LIVE_PLAYER_GET_CHAN_INFO.ordinal(), 0, chnDatalist.get(playIndex));
		}
		
		public boolean isOpened(){
			return mChnInfoLayoutView.getVisibility() == View.VISIBLE ? true:false;
		}
		
		public void closeInfoBar(){
			mChnInfoLayoutView.setVisibility(View.GONE);
		}
		
		public String TimeStamp2Date(long timestamp){  
			  String date = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(timestamp*1000)); 
			  return date;  
			} 
		
		public void updateChanInfoFromEvent(){
			if(chanInfoData == null){
				return;
			}
			// if request fail, show the No and name
			mChanNum.setText(String.valueOf(chanInfoData.get(0).getCid()));
			String channum = "0000".substring(String.valueOf(chanInfoData.get(0).getCid()).length())+chanInfoData.get(0).getCid();
			mChnNo.setText(channum);
			mChnName.setText(chanInfoData.get(0).getChnName());
			String firstStartTime = TimeStamp2Date(chanInfoData.get(0).getStartTime());
			String firstEndTime = TimeStamp2Date(chanInfoData.get(0).getEndTime());
			
			String firstEvent = firstStartTime+" -- "+firstEndTime+"\t\t"+chanInfoData.get(0).getCurProName();
			
			String secStartTime = TimeStamp2Date(chanInfoData.get(1).getStartTime());
			String secEndTime = TimeStamp2Date(chanInfoData.get(1).getEndTime());
			
			String secEvent = secStartTime+" -- "+secEndTime+"\t\t"+chanInfoData.get(1).getCurProName();
			
			
			mCurEventTime.setText(firstEvent);
			mNextEventTime.setText(secEvent);
		}
		
	}
	
}
