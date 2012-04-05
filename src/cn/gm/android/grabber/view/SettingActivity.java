package cn.gm.android.grabber.view;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import cn.gm.android.grabber.R;
import cn.gm.android.grabber.cfg.Config;
import cn.gm.android.grabber.cfg.Item;

/**
 * 设置界面
 * 
 * @author dragon
 * 
 */
public class SettingActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {
	private static final String tag = SettingActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.setting);

		// 从配置文件加载配置
		PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("setting");
		CheckBoxPreference p;
		for (Item item : Config.getInstance().getItems()) {
			if (item.isNested())
				continue;
			p = new CheckBoxPreference(this);
			p.setChecked(item.isSelected());
			p.setKey(item.getId());
			p.setTitle(item.getName());
			p.setSummary("/grabber/" + item.getDir());
			preferenceScreen.addPreference(p);

			// 为Preference注册监听接口
			p.setOnPreferenceChangeListener(this);
		}
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d(tag, "key=" + preference.getKey() + ",newValue=" + newValue);
		// 修改相应配置的值
		Item item = Config.getInstance().getItem(preference.getKey());
		if (item != null) {
			Log.d(tag, "item=" + item.getId());
			item.setSelected((Boolean) newValue);
		}

		// 返回true表示允许改变
		return true;
	}
}
