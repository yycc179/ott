package com.ott.webtv.liveplay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
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
import android.widget.TextClock;
import android.widget.TextView;

import com.ott.webtv.R;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.CoreHandler.Customer_CMD;
import com.ott.webtv.core.LiveDataManager;
import com.ott.webtv.core.LiveDataManager.EventInfo;
import com.ott.webtv.core.LiveDataManager.LiveBaseNode;

public class Epg extends PopupWindow{
	
	private View layoutView;
	private Context context;
	private ListView LvChnList;
	private ListView LvEventList;
	private TextClock mTextClock;
	
	private chnAdapter mChnNameAdapter;
	private chnAdapter mEventNameAdapter;
	public static final int WEEK_SIZE = 7;
	private int[] mWeekDaily = { R.string.IDS_String_Sun,
								 R.string.IDS_String_Mon,
								 R.string.IDS_String_Tue,
								 R.string.IDS_String_Wed,
								 R.string.IDS_String_Thu,
								 R.string.IDS_String_Fri,
								 R.string.IDS_String_Sat
								};
	
	private TextView[] mDaysOfWeek = new TextView[WEEK_SIZE];
	
	private SimpleDateFormat mCurrentDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
	
	private ArrayList<String> AlChnName = new ArrayList<String>();
	private ArrayList<String> eventName = new ArrayList<String>();
	private ArrayList<EventInfo> AlEventList = new ArrayList<EventInfo>();
	
	private int mChnListCurIndex = 0;
	private int mChnNameCurSelectIndex = 0;
	
	private ArrayList<LiveBaseNode> AlChnList ;
	
	private LiveDataManager liveDataMgr = LiveDataManager.getInstance();
	
	private Calendar calendar;
	private PopMsg popmsg ; 
	private Handler playerHandler;
	private int mDateOffset = 0;
	
	CoreHandler core = CoreHandler.getInstace();

	public Epg(Context context,Handler handler){
		this.context = context;
		
		this.playerHandler = handler;
		
		popmsg = new PopMsg(context);
		popmsg.setListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				popmsg.closePopWindow();
			}
		}, null);
		
		initPopup();
		initChannelList();
		
	}
	
	public void show(int curPlayIndex){
		this.mChnListCurIndex = curPlayIndex;
		
		LvChnList.setSelection(mChnListCurIndex);
		popmsg.showLoading();
		
//		liveData.sendCmdToGetTotalEpgData((calendar.getTime().getTime())/1000);
		
		core.loadCustomerData(Customer_CMD.LIVE_EPG_GET_DAILY_EPG.ordinal(), 0, (calendar.getTime().getTime())/1000);
		
		showAtLocation(layoutView, Gravity.NO_GRAVITY, 190, 290);
		
	}
	
	private void initPopup(){
		layoutView = LayoutInflater.from(context).inflate(R.layout.live_epg, null, false);
		LvChnList = (ListView) layoutView.findViewById(R.id.IDC_ListView_epg_DailyMode_chnList);
		LvEventList = (ListView) layoutView.findViewById(R.id.IDC_ListView_epg_DailyMode_eventList);
		mTextClock = (TextClock) layoutView.findViewById(R.id.IDC_TextClock_epg_daily_dateTime);
		mDaysOfWeek[0] = (TextView) layoutView.findViewById(R.id.IDC_TextView_epg_DailyMode_DAY0);
		mDaysOfWeek[1] = (TextView) layoutView.findViewById(R.id.IDC_TextView_epg_DailyMode_DAY1);
		mDaysOfWeek[2] = (TextView) layoutView.findViewById(R.id.IDC_TextView_epg_DailyMode_DAY2);
		mDaysOfWeek[3] = (TextView) layoutView.findViewById(R.id.IDC_TextView_epg_DailyMode_DAY3);
		mDaysOfWeek[4] = (TextView) layoutView.findViewById(R.id.IDC_TextView_epg_DailyMode_DAY4);
		mDaysOfWeek[5] = (TextView) layoutView.findViewById(R.id.IDC_TextView_epg_DailyMode_DAY5);
		mDaysOfWeek[6] = (TextView) layoutView.findViewById(R.id.IDC_TextView_epg_DailyMode_DAY6);
		
		setContentView(layoutView);
		setHeight(414);
		setWidth(900);
		setFocusable(true);
		
		calendar = Calendar.getInstance();
		Date data = new Date(System.currentTimeMillis());
		calendar.setTime(data);
		System.out.println("------ the data.getTime = "+calendar.getTime().getTime());
		int mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		
		int weekOffset = getCurWeekOffset(mDayOfWeek);
		
		for(int i =0;i<WEEK_SIZE;i++){
			mDaysOfWeek[i].setText(mWeekDaily[(weekOffset+i)%WEEK_SIZE]);
		}
		
		layoutView.setOnKeyListener(keyListener);
		LvChnList.setOnKeyListener(keyListener);
		LvChnList.setOnItemClickListener(playListener);
		LvEventList.setOnKeyListener(keyListener);

	}
	private void initChannelList(){
		 AlChnList = liveDataMgr.getChnList();
		 AlChnName.clear();
		for(int i=0;i < 30;i++){
			AlChnName.add(AlChnList.get(i).getChnName());
		}
		
		mChnNameAdapter = new chnAdapter();
		mChnNameAdapter.setData(AlChnName);
		LvChnList.setAdapter(mChnNameAdapter);
	}
	
	
	public void cleanData(){
		AlEventList.clear();
		AlChnList.clear();
	};
	
	private int getCurWeekOffset(int mDayOfWeek){
		int offX = 0;
		
		switch(mDayOfWeek){
		case Calendar.SUNDAY:
			offX = 0;
			break;
		case Calendar.MONDAY:
			offX = 1;
			break;
		case Calendar.TUESDAY:
			offX = 2;
			break;
		case Calendar.WEDNESDAY:
			offX = 3;
			break;
		case Calendar.THURSDAY:
			offX = 4;
			break;
		case Calendar.FRIDAY:
			offX = 5;
			break;
		case Calendar.SATURDAY:
			offX = 6;
			break;
		default:
			System.out.println("---  getWeekDaily error = "+mDayOfWeek);
		}
		return offX;
	}
	
	private void updateEventInfo(boolean gotoNext){
		
		if(gotoNext){
			 if((mDateOffset + 1) >= WEEK_SIZE){
					return;
				}
			 mDateOffset++;
			 calendar.add(Calendar.DATE, +1);
			
		}else{
			 if((mDateOffset - 1) < 0){
					return;
				}
			 mDateOffset--;
			 calendar.add(Calendar.DATE, -1);
		}
		for(int i =0;i<WEEK_SIZE;i++){
			mDaysOfWeek[i].setEnabled(false);
		}
		mDaysOfWeek[(mDateOffset+WEEK_SIZE)%WEEK_SIZE].setEnabled(true);
		
		String dateFormat = mCurrentDateFormat.format(calendar.getTime());
		mTextClock.setText(dateFormat);
		
		popmsg.showLoading();
//		liveData.sendCmdToGetTotalEpgData((calendar.getTime().getTime())/1000);
		
		core.loadCustomerData(Customer_CMD.LIVE_EPG_GET_DAILY_EPG.ordinal(), 0, (calendar.getTime().getTime())/1000);
	}
	
	private void updateEventNameFromChannel(){
		
		mDateOffset = 0;
		
		Date data = new Date(System.currentTimeMillis());
		calendar.setTime(data);
		
		String dateFormat = mCurrentDateFormat.format(calendar.getTime());
		mTextClock.setText(dateFormat);
		
		for(int i =0;i<WEEK_SIZE;i++){
			mDaysOfWeek[i].setEnabled(false);
		}
		mDaysOfWeek[0].setEnabled(true);
		
		popmsg.showLoading();
//		liveData.sendCmdToGetTotalEpgData((calendar.getTime().getTime())/1000);
		
		core.loadCustomerData(Customer_CMD.LIVE_EPG_GET_DAILY_EPG.ordinal(), 0, (calendar.getTime().getTime())/1000);
	}
	
	OnItemClickListener playListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			playSelectChanl(mChnListCurIndex);
		}
		
	};
	
	
	int KEYCODE_YELLOW = 0;
	int KEYCODE_BLUE = 1;
	
	
	int position = 0;	
	OnKeyListener keyListener = new OnKeyListener() {
		
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			
			if(arg2.getAction() == KeyEvent.ACTION_DOWN){
				
				if(arg1 == KeyEvent.KEYCODE_BACK){
					dismiss();
				}else if(arg1 == KEYCODE_YELLOW){
//				}else if(arg1 == KeyEvent.KEYCODE_YELLOW){
					updateEventInfo(false);
				}else if(arg1 == KEYCODE_BLUE){
//				}else if(arg1 == KeyEvent.KEYCODE_BLUE){
					updateEventInfo(true);
				}
				
				if(arg0 == LvChnList){
					if(arg1 == KeyEvent.KEYCODE_DPAD_DOWN){
						if(mChnListCurIndex < AlChnName.size()){
							mChnListCurIndex++;
							updateEventNameFromChannel();
						}
					}else if(arg1 == KeyEvent.KEYCODE_DPAD_UP){
						if(mChnListCurIndex > 0){
							mChnListCurIndex--;
							updateEventNameFromChannel();
						}
					}else if(arg1 == KeyEvent.KEYCODE_DPAD_RIGHT){
						mChnNameCurSelectIndex = LvChnList.getSelectedItemPosition();
						position = mChnNameCurSelectIndex - LvChnList.getFirstVisiblePosition();
						LvChnList.getChildAt(position).setActivated(true);
						LvEventList.setAdapter(mEventNameAdapter);
						LvEventList.setSelection(0);
					}
				}else if(arg0 == LvEventList){
					if(arg1 == KeyEvent.KEYCODE_DPAD_LEFT){
							LvChnList.getChildAt(position).setActivated(false);
							LvChnList.setAdapter(mChnNameAdapter);
							LvChnList.setSelectionFromTop(mChnNameCurSelectIndex,48 * (mChnNameCurSelectIndex%7));
						}
					}
			}
			return false;
		}
	};
	
	private void playSelectChanl(int mChnListCurIndex){
		Message msg = playerHandler.obtainMessage();
		msg.what = MainActivity.PLAY_SELECTED_CHANNEL;
		msg.arg1 = mChnListCurIndex;
		playerHandler.sendMessage(msg);
		
	}
	
	public void handlerMessageFromPlayer(int what,int arg1,int arg2,Object obj){
		if(what == CoreHandler.Callback.CBK_GET_CUSTOMER_DONE.ordinal()){
			if(arg1 == Customer_CMD.LIVE_EPG_GET_DAILY_EPG.ordinal()){
				popmsg.closeLoading();
				
				AlEventList = liveDataMgr.getEpgList();
				processEventList(AlEventList);
				
			}
		}else if(what == CoreHandler.Callback.CBK_GET_CUSTOMER_FAIL.ordinal()){
			if(arg1 == Customer_CMD.LIVE_EPG_GET_DAILY_EPG.ordinal()){
				popmsg.closeLoading();
				popmsg.showMessage("Get Epg Data Fail");
			}
		}
	}
	
	private void processEventList(ArrayList<EventInfo> list){
		int listCount = list.size();
		int position = 0;
		eventName.clear();
		for(int i=0;i < listCount;i++){
			
			long startTime = list.get(i).getStartTime();
			long endTime = list.get(i).getEndTime();
			long systemTime = System.currentTimeMillis();
			if(systemTime >= startTime && systemTime <= endTime){
				position = i;
			}
			String StartTime = TimeStamp2Date(startTime);
			String EndTime = TimeStamp2Date(endTime);
			
			String EventInfo = StartTime+" ~ "+EndTime+"\t\t"+list.get(i).getCurProName();
			eventName.add(EventInfo);
		}
		
		mEventNameAdapter = new chnAdapter();
		mEventNameAdapter.setData(eventName);
		LvEventList.setAdapter(mEventNameAdapter);
		LvEventList.setSelection(position);
		
	}
	
	public String TimeStamp2Date(long timestamp){  
		  String date = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(timestamp*1000)); 
		  return date;  
		} 
	
	class chnAdapter extends BaseAdapter{

		ArrayList<String> viewData;
		public void setData(ArrayList<String> listData){
			this.viewData = listData;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return viewData.size();
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

		TextView chnTextView;
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			if(arg1 == null){
				arg1 = LayoutInflater.from(context).inflate(R.layout.live_epg_item, null, false);
				chnTextView = (TextView) arg1.findViewById(R.id.epg_chnName);
				arg1.setTag(chnTextView);
			}else{
				chnTextView = (TextView)arg1.getTag();
			}

			chnTextView.setText(viewData.get(arg0));
			
			return arg1;
		}
		
	}
}