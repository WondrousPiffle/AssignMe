package nuffsaidm8.me.assignme.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

import java.util.Map;

import nuffsaidm8.me.assignme.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toNewGroup(View v) {
        Intent i = new Intent(getApplicationContext(), NewGroupActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
    }

    @SuppressLint("InflateParams")
    public void toGroup(View w) {
        Intent i = new Intent(getApplicationContext(), GroupLogin.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
    }
}
