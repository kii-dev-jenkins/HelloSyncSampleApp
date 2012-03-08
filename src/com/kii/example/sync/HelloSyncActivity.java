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
import com.kii.cloud.storage.response.UserResult;
import com.kii.mobilesdk.bridge.KiiUMInfo;
import com.kii.sync.KiiClient;
import com.kii.sync.KiiFile;
import com.kii.sync.KiiFileUtil;
import com.kii.sync.SyncMsg;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

public class HelloSyncActivity extends Activity {
    KiiClient kiiClient;
    AuthManager authMan;
    Handler myHandler;

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
                createUser();
            }
        });
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                uploadFile();
            }
        });
    }

    private void createUser() {
        // Set Application ID and Application Key.
        EasyClient.start(this, "d250d1c1", "a7d82e1181318f1f037fd4fde1af5669");
        EasyClient.getInstance().setBaseURL("http://dev-usergrid.kii.com");
        authMan = EasyClient.getUserManager();
        KiiUser kUser = new KiiUser();
        UserResult res = null;
        // Create User by communicating KiiCloud with User Manager SDK.
        try {
            kUser.setUsername("testUser");
            kUser.setEmail("testUser@testkii.com");
            kUser.put("country", "JP");
            res = authMan.createUser(kUser, "1234");
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
        kUser = res.getKiiUser();
        KiiUMInfo umInfo = new KiiUMInfo(this, kUser.getUsername(), "1234", kUser.getType(), null);
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
            throw new RuntimeException("Sync Failed!");
        }
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

}