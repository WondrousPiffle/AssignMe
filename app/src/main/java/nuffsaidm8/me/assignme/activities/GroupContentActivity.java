package nuffsaidm8.me.assignme.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import nuffsaidm8.me.assignme.Constants;
import nuffsaidm8.me.assignme.R;
import nuffsaidm8.me.assignme.frags.GroupChatFragment;
import nuffsaidm8.me.assignme.frags.GroupTasksFragment;

public class GroupContentActivity extends AppCompatActivity {

    private GroupChatFragment chatFrag;
    private GroupTasksFragment taskFrag;
    private FragmentTabHost tabHost;
    private PubNub connection;
    private String groupName;
    private String nickName;
    private boolean chatFragInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_content);
        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        tabHost.addTab(tabHost.newTabSpec("tasks").setIndicator("Tasks"),
                GroupTasksFragment.class, null);

        tabHost.addTab(tabHost.newTabSpec("chat")
                .setIndicator("Chat"), GroupChatFragment.class, null);

        groupName = getIntent().getStringExtra("groupName");
        nickName = getIntent().getStringExtra("nickName");
        PNConfiguration config = new PNConfiguration();
        config.setPublishKey(Constants.publishKey);
        config.setSubscribeKey(Constants.subscribeKey);
        connection = new PubNub(config);



        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (!chatFragInitialized && tabId.equals("chat")) {
                    chatFragInitialized = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chatFrag = (GroupChatFragment) getSupportFragmentManager().findFragmentByTag("chat");
                            connection.history()
                                    .channel(groupName)
                                    .count(50)
                                    .async(new PNCallback<PNHistoryResult>() {
                                        @Override
                                        public void onResponse(PNHistoryResult result, PNStatus status) {
                                            for (PNHistoryItemResult item : result.getMessages()) {
                                                final String[] sForm = item.getEntry().getAsString().split(">>>>");
                                                String m = "";
                                                if (sForm.length > 2) {
                                                    for (int x = 1; x < sForm.length; x++) {
                                                        m += sForm[x];
                                                    }
                                                } else {
                                                    m = sForm[1];
                                                }

                                                final String mCopy = m;

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        switch (sForm[0]) {
                                                            case "groupCreated":
                                                                chatFrag.adapter.clear();
                                                                break;
                                                            case "chat":
                                                                chatFrag.adapter.add(mCopy);
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    });
                        }
                    }, 50);
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                taskFrag = (GroupTasksFragment) getSupportFragmentManager().findFragmentByTag("tasks");

                connection.history()
                        .channel(groupName)
                        .count(50)
                        .async(new PNCallback<PNHistoryResult>() {
                            @Override
                            public void onResponse(PNHistoryResult result, PNStatus status) {
                                for (PNHistoryItemResult item : result.getMessages()) {
                                    final String[] sForm = item.getEntry().getAsString().split(">>>>");
                                    String m = "";
                                    if (sForm.length > 2) {
                                        for (int x = 1; x < sForm.length; x++) {
                                            m += sForm[x];
                                        }
                                    } else {
                                        m = sForm[1];
                                    }

                                    final String mCopy = m;

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (sForm[0]) {
                                                case "addTask":
                                                    if (taskFrag.adapter.getPosition(mCopy) < 0) {
                                                        taskFrag.adapter.add(mCopy);
                                                    }
                                                    break;
                                                case "deleteTask":
                                                    if (taskFrag.adapter.getPosition(mCopy) >= 0) {
                                                        taskFrag.adapter.remove(mCopy);
                                                    }
                                                    break;
                                                case "groupCreated":
                                                    taskFrag.adapter.clear();
                                                    break;
                                            }
                                        }
                                    });

                                }
                            }
                        });

                connection.addListener(new SubscribeCallback() {
                    @Override
                    public void status(PubNub pubnub, PNStatus status) {
                        if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                            Toast.makeText(getApplicationContext(), "You were disconnected!", Toast.LENGTH_SHORT).show();
                        } else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                            if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                                pubnub.publish().channel(groupName).message("chat>>>><ADMIN> User '" + nickName + "' Connected.").async(new PNCallback<PNPublishResult>() {
                                    @Override
                                    public void onResponse(PNPublishResult result, PNStatus status) {
                                    }
                                });
                            }
                        } else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                            Toast.makeText(getApplicationContext(), "You were reconnected!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void message(PubNub pubnub, PNMessageResult message) {
                        final String[] sForm = message.getMessage().getAsString().split(">>>>");
                        String m = "";
                        if (sForm.length > 2) {
                            for (int x = 1; x < sForm.length; x++) {
                                m += sForm[x];
                            }
                        } else {
                            m = sForm[1];
                        }

                        final String mCopy = m;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (sForm[0]) {
                                    case "chat":
                                        if (chatFragInitialized) {
                                            chatFrag.adapterContent.add(mCopy);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    chatFrag.chatListView.setSelection(chatFrag.adapter.getCount() - 1);
                                                }
                                            });
                                        }
                                        break;
                                    case "addTask":
                                        taskFrag.adapterContent.add(mCopy);
                                        connection.publish().channel(groupName).message("chat>>>><ADMIN> Task '" + mCopy + "' Added.").async(new PNCallback<PNPublishResult>() {
                                            @Override
                                            public void onResponse(PNPublishResult pnPublishResult, PNStatus pnStatus) {
                                            }
                                        });
                                        break;
                                    case "deleteTask":
                                        taskFrag.adapterContent.remove(mCopy);
                                        connection.publish().channel(groupName).message("chat>>>><ADMIN> Task '" + mCopy + "' Deleted.").async(new PNCallback<PNPublishResult>() {
                                            @Override
                                            public void onResponse(PNPublishResult pnPublishResult, PNStatus pnStatus) {
                                            }
                                        });
                                        break;
                                }
                            }
                        });
                    }

                    @Override
                    public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                    }
                });
                connection.subscribe().channels(java.util.Collections.singletonList(groupName)).execute();
            }
        }, 100);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        connection.publish().channel(groupName).message("chat>>>><ADMIN> User '" + nickName + "' Logged Out.").async(new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult pnPublishResult, PNStatus pnStatus) {
            }
        });
        connection.disconnect();
        Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
    }

    public void goHome(View v) {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    public void sendMessage(View v) {
        String message = ((EditText) findViewById(R.id.messageToSend)).getText().toString();
        if(message.length() == 0){
            Toast.makeText(getApplicationContext(), "Sending no content is not allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        connection.publish().channel(groupName).message("chat>>>><" + nickName + "> " + message).async(new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult pnPublishResult, PNStatus pnStatus) {
            }
        });
        ((EditText) findViewById(R.id.messageToSend)).setText("");
    }

    public void deleteTask(View v) {
        final EditText input = new EditText(getApplicationContext());
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("What task would you like to delete?")
                .setView(input)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().length() == 0) {
                            Toast.makeText(getApplicationContext(), "Blank is not a valid input", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        } else if (taskFrag.adapter.getPosition(input.getText().toString()) >= 0) {
                            connection.publish().channel(groupName).message("deleteTask>>>>" + input.getText().toString()).async(new PNCallback<PNPublishResult>() {
                                @Override
                                public void onResponse(PNPublishResult pnPublishResult, PNStatus pnStatus) {
                                }
                            });
                            dialog.cancel();
                        } else {
                            Toast.makeText(getApplicationContext(), "This task doesn't exist", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    }
                })
                .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    public void addTask(View v) {
        final EditText input = new EditText(getApplicationContext());
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        new AlertDialog.Builder(this)
                .setTitle("Create New Task")
                .setView(input)
                .setMessage("What task would you like to create?")
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(input.getText().length() == 0){
                            Toast.makeText(getApplicationContext(), "Blank is not a valid input", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        } else if (taskFrag.adapter.getPosition(input.getText().toString()) < 0) {
                            connection.publish().channel(groupName).message("addTask>>>>" + input.getText().toString()).async(new PNCallback<PNPublishResult>() {
                                @Override
                                public void onResponse(PNPublishResult pnPublishResult, PNStatus pnStatus) {
                                }
                            });
                            dialog.cancel();
                        } else {
                            Toast.makeText(getApplicationContext(), "This task already exists", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    }
                })
                .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}