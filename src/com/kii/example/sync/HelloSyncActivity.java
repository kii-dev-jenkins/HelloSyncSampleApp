package com.kii.example.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONException;

import com.kii.cloud.storage.EasyClient;
import com.kii.cloud.storage.dataType.KiiUser;
import com.kii.cloud.storage.manager.AuthManager;
import com.kii.cloud.storage.response.CloudExecutionException;
import com.kii.mobilesdk.bridge.KiiUMInfo;
import com.kii.sync.KiiClient;
import com.kii.sync.KiiFile;
import com.kii.sync.KiiFileUtil;
import com.kii.sync.SyncMsg;
import com.kii.sync.utils.Base64;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class HelloSyncActivity extends Activity {
    KiiClient kiiClient;
    AuthManager authMan;
    Handler myHandler;
    String userName = "testUser";
    public static final String appId = "c42a57d0";
    public static final String appKey = "224b6595df9387530feb6f696a51c658";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        kiiClient = KiiClient.getInstance(this.getApplicationContext());
        kiiClient.setServerBaseUrl("http://dev-usergrid.kii.com");

        myHandler = new Handler();
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    public void run() {
                        createUser();
                        myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                new Thread(new Runnable() {
                                    public void run() {
                                        uploadFile();
                                    }
                                }).start();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void createUser() {
        // Set Application ID and Application Key.
        EasyClient.start(this, appId, appKey);
        EasyClient.getInstance().setBaseURL("http://dev-usergrid.kii.com:12110");
        authMan = EasyClient.getUserManager();
        KiiUser kUser = new KiiUser();
        // Create User by communicating KiiCloud with User Manager SDK.
        try {
            userName = userName + Long.toString(System.currentTimeMillis());
            kUser.setUsername(userName);
            authMan.createUser(kUser, "1234");
            authMan.login(userName, "1234");
        } catch (CloudExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Create user Failed!");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Create user Failed!");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Create user Failed!");
        }
        KiiUMInfo umInfo = new KiiUMInfo(this, userName, "1234", "KII_ID", userName);
        // Pass the User Information to Sync SDK.
        kiiClient.setKiiUMInfo(umInfo);
    }

    private void uploadFile() {
        try {
            kiiClient.initSync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Sync Failed!");
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException("Sync Failed!");
        }
        File txtFile = createFile();
        KiiFile kFile = KiiFileUtil.createKiiFileFromFile(txtFile.getAbsolutePath());
        int ret = kiiClient.upload(kFile);
        if (ret != SyncMsg.OK) {
            throw new RuntimeException("Sync Failed! code: " + ret);
        }
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                updateMain();

            }
        });
    }

    private File createFile() {
        File f = new File(Environment.getExternalStorageDirectory(),
                "hello.txt");
        try {
            f.createNewFile();
            PrintWriter p = new PrintWriter(f);
            p.write("Hello, Sync");
            p.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private void updateMain() {
        TextView linkURL = (TextView) HelloSyncActivity.this.findViewById(R.id.url_link);
        MovementMethod method = LinkMovementMethod.getInstance();
        linkURL.setMovementMethod(method);

        linkURL.setText(getLink());
        linkURL.setVisibility(View.VISIBLE);

        EditText userNameET = (EditText)HelloSyncActivity.this.findViewById(R.id.username_disp);
        userNameET.setText(userName);
        userNameET.setVisibility(View.VISIBLE);
    }

    private CharSequence getLink() {
        Uri.Builder b = new Uri.Builder();
        Log.v("HelloSync", Base64.encodeToString((appId+":"+appKey).getBytes(), Base64.DEFAULT));
        String lUrl = b.scheme("http")
                .authority("dev-usergrid.kii.com")
                .appendQueryParameter(
                        "app",
                        Base64.encodeToString(
                                (appId + ":" + appKey).getBytes(),
                                Base64.DEFAULT)).build().toString();
        String htmlLink = "<a href=\"" + lUrl + "\">Sync Success! on Web UI you can see the data uploaded!\nPlease copy user name bellow before click.</a>";
        CharSequence seq = Html.fromHtml(htmlLink);
        return seq;
    }
}