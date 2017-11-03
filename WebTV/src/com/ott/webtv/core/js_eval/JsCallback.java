package com.ott.webtv.core.js_eval;

/**
 * Interface for passing code that will be executed after the JS has finished
 */

public interface JsCallback {
	public abstract void onResult(String value);
}
