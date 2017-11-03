package com.ott.webtv.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ott.webtv.core.DataNode.Content;

public class StoreManager<T extends Serializable> {
	public static final int MAX_COUNT = 100;
	private static StoreManager<Content> favManager;
	private static StoreManager<Content> historyManager;
	private static int pageSize;

	private final String mBuffPath;
	private List<T> list;

	private String[] titles;
	private String[] picUrls;

	private StoreManager(String name) {
		mBuffPath = name;
		list = new ArrayList<T>();
		read();
		updateData();
	}

	static void initialize(String path, int size) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		
		pageSize = size;
		
		if (favManager == null) {
			favManager = new StoreManager<Content>(path + "/data.fav");
		}

		if (historyManager == null) {
			historyManager = new StoreManager<Content>(path + "/data.his");
		}
	}

	public static StoreManager<Content> getFavManager() {
		return favManager;
	}

	public static StoreManager<Content> getHisManager() {
		return historyManager;
	}

	private void updateData() {
		int size = list.size();

		if (size == 0) {
			titles = null;
			picUrls = null;
			return;
		}

		titles = new String[size];
		picUrls = new String[size];
		int i = 0;

		for (T t : list) {
			Content node = (Content) t;
			titles[i] = node.getTitle();
			picUrls[i] = node.getPicURL();
			i++;
		}
	}

	public Boolean isExist(T data){
		return list.contains(data);
	}
	
	public synchronized void add(T data) {
		if (data == null)
			return;
		
//		if (list.contains(data)) {
//			if (!bFavFlag) {
//				list.remove(data);
//			} else {
//				throw new Exception();
//			}
//		}

		list.add(0, data);
		for (int i = list.size() - 1; i >= MAX_COUNT; i--) {
			list.remove(i);
		}

		updateData();
	}

	public T get(int index) {
		return list.get(index);
	}

	public T getInCache(T data) {
		T ret = data;

		do {
			if (data == null) {
				break;
			}

			for (T node : list) {
				if (node.hashCode() == data.hashCode()) {
					ret = node;
					break;
				}
			}
		} while (false);

		return ret;
	}

	public String[] getTitles() {
		return titles;
	}

	public String[] getPicURLs() {
		return picUrls;
	}
	
	public int getTotalPage(){
		int size = list.size();
		int page = 0;
		
		if (size > 0) {
			int a = size % pageSize;
			page = size / pageSize;
			page =  a > 0 ? page + 1 : page;			
		}
		
		return page;
	}
	
	public synchronized void remove(T data) {
		if (list.size() == 0) {
			return;
		}

		list.remove(data);
		updateData();
	}

	public synchronized void clear() {
		if (list.size() == 0) {
			return;
		}
		list.clear();
		updateData();
	}

	@SuppressWarnings("unchecked")
	private void read() {
		try {
			File file = new File(mBuffPath);
			if (!file.exists()) {
				return;
			}

			FileInputStream fis = new FileInputStream(mBuffPath);

			ObjectInputStream ois = new ObjectInputStream(fis);
			list = (List<T>) ois.readObject();

			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void flush() {
		if (list.size() == 0) {
			return;
		}

		try {
			FileOutputStream fos = new FileOutputStream(mBuffPath);

			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(list);

			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void release() {
		if(favManager != null){
			favManager.flush();	
			favManager = null;
		}
		if(historyManager != null){
			historyManager.flush();
			historyManager = null;
		}
	}

}
