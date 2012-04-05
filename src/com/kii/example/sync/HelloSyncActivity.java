//
//
//  Copyright 2012 Kii Corporation
//  http://kii.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//

package com.kii.example.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import com.kii.cloud.storage.exception.CloudExecutionException;
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
import com.kii.cloud.storage.*;

public class HelloSyncActivity extends Activity {
    KiiClient kiiClient;
    Handler myHandler;
    String userName = "testUser";
    static final String PASSWORD = "1234";
    static final String ACCOUNT_TYPE_KII = "KII_ID";
    static final String ACCOUNT_TYPE_EMAIL = "EMAIL";
    String appId;
    String appKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        kiiClient = KiiClient.getInstance(this.getApplicationContext());
        kiiClient.setServerBaseUrl("http://api.kii.com");

        myHandler = new Handler();
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    public void run() {
                        createUser();
                        uploadFile();
                        updateMain();
                    }
                }).start();
            }
        });
    }

    private void createUser() {
        // Each time create new user.
        userName = userName + Long.toString(System.currentTimeMillis());
        KiiUMInfo umInfo = new KiiUMInfo(this, userName, PASSWORD, ACCOUNT_TYPE_KII, userName);

        // Cache is for accessing WebUI later.
        appId = umInfo.getAppId();
        appKey = umInfo.getAppKey();

        com.kii.cloud.storage.KiiClient.initialize(appId, appKey,
                "http://api.kii.com/app/api");

        try {
            new KiiUser().register(userName, PASSWORD);
            KiiUser.logIn(userName, PASSWORD);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Auth failed.");
        } catch (CloudExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Auth failed.");
        }
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
    }

    private File createFile() {
        File f = new File(Environment.getExternalStorageDirectory(),
                "hello.txt");
        try {
            if (f.exists())
                f.delete();
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
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView linkURL = (TextView) HelloSyncActivity.this.findViewById(R.id.url_link);
                MovementMethod method = LinkMovementMethod.getInstance();
                linkURL.setMovementMethod(method);

                linkURL.setText(getLink());
                linkURL.setVisibility(View.VISIBLE);

                EditText userNameET = (EditText)HelloSyncActivity.this.findViewById(R.id.username_disp);
                userNameET.setText(userName);
                userNameET.setVisibility(View.VISIBLE);
            }
        });
    }

    private CharSequence getLink() {
        Uri.Builder b = new Uri.Builder();
        Log.v("HelloSync", Base64.encodeToString((appId+":"+appKey).getBytes(), Base64.DEFAULT));
        String lUrl = b.scheme("http")
                .authority("api.kii.com")
                .appendQueryParameter(
                        "app",
                        Base64.encodeToString(
                                (appId + ":" + appKey).getBytes(),
                                Base64.DEFAULT)).build().toString();
        String htmlLink = "<a href=\""
                + lUrl
                + "\">Sync Success! on Web UI you can see the data uploaded!\nPlease copy user name bellow before click.</a>";
        CharSequence seq = Html.fromHtml(htmlLink);
        return seq;
    }
}