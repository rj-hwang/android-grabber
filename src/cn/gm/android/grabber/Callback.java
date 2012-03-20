package cn.gm.android.grabber;

/**
 * 回调接口
 * @author dragon
 * 
 */
public interface Callback<T> {
	void call(T result);
}
