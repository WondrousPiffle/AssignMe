package nuffsaidm8.me.assignme.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;

import java.util.ArrayList;

import nuffsaidm8.me.assignme.Constants;
import nuffsaidm8.me.assignme.R;

public class GroupLogin extends AppCompatActivity {

    private AutoCompleteTextView groupNameInput;
    private EditText yourNameInput;
    private SharedPreferences sp;
    private PubNub connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_login);
        groupNameInput = (AutoCompleteTextView) findViewById(R.id.groupNameInput);
        yourNameInput = (EditText) findViewById(R.id.yourNameInput);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.customadapterview, new ArrayList<>(sp.getAll().keySet()));
        groupNameInput.setAdapter(adapter);

        PNConfiguration config = new PNConfiguration();
        config.setPublishKey(Constants.publishKey);
        config.setSubscribeKey(Constants.subscribeKey);
        connection = new PubNub(config);
    }

    public void tryLogIn(View v) {
        final String name = groupNameInput.getText().toString();
        final String yourName = yourNameInput.getText().toString();

        if(yourName.length() == 0){
            Toast.makeText(getApplicationContext(), "Blank names are not allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        connection.history()
                .channel("allGroups")
                .count(500)
                .async(new PNCallback<PNHistoryResult>() {
                    @Override
                    public void onResponse(PNHistoryResult result, PNStatus status) {
                        boolean go = false;
                        for (PNHistoryItemResult item : result.getMessages()) {
                            if (item.getEntry().getAsString().equals(name)) {
                                go = true;
                            }
                        }
                        if (go) {
                            goOn(name, yourName);
                        } else {
                            if (sp.contains(name)) {
                                //Saved, but doesn't exist
                                Toast.makeText(getApplicationContext(), "This group no longer exists!", Toast.LENGTH_SHORT).show();
                                sp.edit().remove(name).apply();
                                return;
                            }
                            //Not saved, still doesn't exist
                            Toast.makeText(getApplicationContext(), "This group doesn't exist!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        connection.disconnect();
    }

    public void goOn(String name, String yourName){
        sp.edit().putString(name, "trivial").apply();
        Intent i = new Intent(getApplicationContext(), GroupContentActivity.class);
        i.putExtra("groupName", name);
        i.putExtra("nickName", yourName);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Toast.makeText(getApplicationContext(), "Log in successful", Toast.LENGTH_SHORT).show();
        getApplicationContext().startActivity(i);
    }

    public void goHome(View v){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
    }
}