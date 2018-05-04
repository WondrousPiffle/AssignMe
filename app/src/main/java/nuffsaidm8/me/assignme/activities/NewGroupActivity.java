package nuffsaidm8.me.assignme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;

import nuffsaidm8.me.assignme.Constants;
import nuffsaidm8.me.assignme.R;

public class NewGroupActivity extends AppCompatActivity {

    private PubNub connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        PNConfiguration config = new PNConfiguration();
        config.setPublishKey(Constants.publishKey);
        config.setSubscribeKey(Constants.subscribeKey);
        connection = new PubNub(config);
    }

    public void goHome(View v) {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void createGroup(View v) throws PubNubException {
        final String name = ((EditText) findViewById(R.id.groupNameInput)).getText().toString();

        connection.history()
                .channel("allGroups")
                .count(50)
                .async(new PNCallback<PNHistoryResult>() {
                    @Override
                    public void onResponse(PNHistoryResult result, PNStatus status) {
                        boolean go = true;
                        for (PNHistoryItemResult message : result.getMessages()) {
                            if (message.getEntry().getAsString().equals(name)) {
                                go = false;
                            }
                        }
                        if(go){
                            goOn(name);
                        } else {
                            connection.disconnect();
                            Toast.makeText(getApplicationContext(), "That name is already in use!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void goOn(String name){
        connection.publish().channel("allGroups").message(name).async(new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult pnPublishResult, PNStatus pnStatus) {
            }
        });

        connection.publish().channel(name).message("groupCreated>>>>" + name).async(new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult pnPublishResult, PNStatus pnStatus) {
            }
        });

        Toast.makeText(getApplicationContext(), "Creation successful! Please log in", Toast.LENGTH_LONG).show();
        Intent i = new Intent(getApplicationContext(), GroupLogin.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        connection.disconnect();
    }
}
