package com.ott.webtv.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;

import com.ott.webtv.core.DataNode.Content;

public class StoreManager<T extends Serializable> {
	public static final int MAX_COUNT = 100;
	private static StoreManager<Content> favManager;
	private static StoreManager<Content> historyManager;
	private static SimpleDateFormat dateFormat;
	private static int pageSize;
	private static String cacheDir;

	private final String mBuffPath;
	private List<T> list;

	private StoreManager(String name) {
		mBuffPath = name;
		read();
	}

	static void initialize(Context context, String name, int size) {
		String path = context.getApplicationInfo().dataDir + "/" + name;

		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}

		pageSize = size;
		cacheDir = context.getCacheDir().getAbsolutePath();

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

	public Boolean isExist(T data) {
		return list.contains(data);
	}

	public synchronized void add(T data) {
		if (data == null)
			return;

		list.add(0, data);
		for (int i = list.size() - 1; i >= MAX_COUNT; i--) {
			list.remove(i);
		}

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

	@SuppressWarnings("unchecked")
	public List<Content> getList() {
		return (List<Content>) list;
	}

	public int getSize() {
		return list.size();
	}

	public int getTotalPage() {
		int size = list.size();
		int page = 0;

		if (size > 0) {
			int a = size % pageSize;
			page = size / pageSize;
			page = a > 0 ? page + 1 : page;
		}

		return page;
	}

	public synchronized void remove(T data) {
		if (list.size() > 0) {
			list.remove(data);
		}
	}

	public synchronized void remove(int index) {
		if (list.size() > 0) {
			list.remove(index);
		}
	}

	public synchronized void clear() {
		if (list.size() > 0) {
			list.clear();
		}
	}

	@SuppressWarnings("unchecked")
	private void read() {
		File file = new File(mBuffPath);

		list = (List<T>) readObject(file);

		if (list == null) {
			list = new ArrayList<T>();
		}
	}

	private void flush() {
		writeObject(new File(mBuffPath), list);
	}

	static void release() {
		if (favManager != null) {
			favManager.flush();
			favManager = null;
		}
		if (historyManager != null) {
			historyManager.flush();
			historyManager = null;
		}
	}

	public static String getCacheDir() {
		return cacheDir;
	}

	public static Object readObject(File file) {
		Object obj = null;

		if (!file.exists()) {
			return obj;
		}

		try {
			FileInputStream fis = new FileInputStream(file);

			ObjectInputStream ois = new ObjectInputStream(fis);
			obj = ois.readObject();

			fis.close();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return obj;
	}

	public static void writeObject(File file, Object obj) {
		if (obj == null) {
			return;
		}

		try {
			FileOutputStream fos = new FileOutputStream(file);

			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);

			fos.close();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] readFile(InputStream is) {
		byte[] buf = null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			BufferedInputStream bis = new BufferedInputStream(is);

			int i = -1;
			while ((i = bis.read()) != -1) {
				baos.write(i);
			}
			buf = baos.toByteArray();
			bis.close();
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return buf;
	}

	public static byte[] readFile(File file) {
		byte[] buf = null;

		if (!file.exists()) {
			return buf;
		}

		try {
			buf = readFile(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return buf;
	}

	public static void writeFile(File file, byte[] buf) {

		if (buf == null) {
			return;
		}

		try {
			FileOutputStream fis = new FileOutputStream(file);

			fis.write(buf);
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getLastModified(File file) {
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'",
					Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		}
		return file.exists() ? dateFormat.format(file.lastModified()) : null;
	}
}
