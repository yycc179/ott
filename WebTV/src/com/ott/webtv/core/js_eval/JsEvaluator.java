package com.ott.webtv.core.js_eval;

import java.util.ArrayList;

import com.ott.webtv.core.CoreHandler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class JsEvaluator {
	public final static String JS_NAMESPACE = "ottJsEvaluator";
	private WebViewWrapper mWebViewWrapper;
	private JSInterface mJsInterface;
	public static JsEvaluator mEvaluator;

	private JsEvaluator(Context context) {
		// TODO Auto-generated constructor stub
		mJsInterface = new JSInterface();
		mWebViewWrapper = new WebViewWrapper(context, mJsInterface);
	}

	public static JsEvaluator getEvaluator() {
		if (mEvaluator == null) {
			return new JsEvaluator(CoreHandler.getInstace().getContext());
		}
		return mEvaluator;
	}

	public void evaluate(String jsCode) {
		evaluate(jsCode, null);
	}

	private String getJsForEval(String jsCode, int callbackIndex) {
		return String.format("%s.returnResultToJava(eval('%s'), %s);",
				JS_NAMESPACE, JSFormatter.escapeCharSequence(jsCode),
				callbackIndex);
	}

	public void evaluate(String jsCode, JsCallback resultCallback) {
		String js = getJsForEval(jsCode, resultCallback == null ? -1
				: mJsInterface.getResultCallbacks().size());

		js = String.format("javascript: %s", js);

		if (resultCallback != null) {
			mJsInterface.getResultCallbacks().add(resultCallback);
		}

		mWebViewWrapper.loadUrl(js);
	}

	public void callFunction(JsCallback resultCallback, String name,
			Object... args) {
		evaluate(JSFormatter.format(name, args), resultCallback);
	}

}

@SuppressLint("SetJavaScriptEnabled")
class WebViewWrapper {
	protected WebView mWebView;

	public WebViewWrapper(Context context, JSInterface jsi) {
		mWebView = new WebView(context);
		mWebView.setWillNotDraw(true);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(jsi, JsEvaluator.JS_NAMESPACE);
	}

	public void loadUrl(String url) {
		mWebView.loadUrl(url);
	}
}

final class JSInterface {
	private final ArrayList<JsCallback> mResultCallbacks = new ArrayList<JsCallback>();

	public void addResultCallbacks(JsCallback cbk) {
		mResultCallbacks.add(cbk);
	}

	public ArrayList<JsCallback> getResultCallbacks() {
		return mResultCallbacks;
	}

	@JavascriptInterface
	public void returnResultToJava(String value, int cbkIndex) {
		if (cbkIndex != -1) {
			mResultCallbacks.get(cbkIndex).onResult(value);
		}
	}
}

class JSFormatter {

	public static String escapeCharSequence(String str) {
		return str.replace("\\", "\\\\").replace("'", "\\'")
				.replace("\"", "\\\"").replaceAll("\\s", " ");
	}

	public static String paramToString(Object param) {
		String str = "";
		if (param instanceof String) {
			str = (String) param;
			str = String.format("\"%s\"", escapeCharSequence(str));
		} else {
			try {
				@SuppressWarnings("unused")
				final double d = Double.parseDouble(param.toString());
				str = param.toString();
			} catch (final NumberFormatException nfe) {
			}
		}

		return str;
	}

	public static String format(String functionName, Object... args) {
		final StringBuilder paramsStr = new StringBuilder();

		for (final Object param : args) {
			if (paramsStr.length() > 0) {
				paramsStr.append(", ");
			}
			paramsStr.append(paramToString(param));
		}

		return String.format("%s(%s)", functionName, paramsStr);
	}
}