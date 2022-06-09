package net.sourceforge.simcpux.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.byronwechat.ByronWXEntryActivity;

public class WXEntryActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, ByronWXEntryActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
        finish();
    }
}
