package cn.bliss.grabber.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cn.bliss.grabber.R;

/**
 * 关于界面
 * 
 * @author dragon
 * 
 */
public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		// 点击图标就返回
		((ImageView) this.findViewById(R.id.about_logo))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						AboutActivity.this.finish();
					}
				});
	}
}
