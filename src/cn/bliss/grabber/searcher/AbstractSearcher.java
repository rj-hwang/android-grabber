/**
 * 
 */
package cn.bliss.grabber.searcher;

import java.io.File;

import android.os.Environment;
import cn.bliss.grabber.Searcher;

/**
 * 抽象搜索器
 * 
 * @author dragon
 * 
 */
public abstract class AbstractSearcher implements Searcher {
	private String uid;
	private String name;
	private String path;
	protected File sdCardDir = Environment.getExternalStorageDirectory();// sd卡的目录路径
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
