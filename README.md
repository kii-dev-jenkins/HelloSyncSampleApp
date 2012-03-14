# Quick Start Guide

## Hello Sync Project
* Before this tutorial, please Register an Application ID and Application Key.

### Anatomy of the project

* These are required settings to use Sync SDK. please check followings when you create your own project.
    * Sync SDK libraries in following places:  
{project root}/libs/KiiSyncManager-{version}.jar  
{project root}/libs/armeabi/libpfsengine.so
    * UserManager SDK libraries in following places:  
{project root}/libs/KiiCloudStorageSDK-{version}.jar
    * Build path to KiiSyncManager-{vesion}.jar and KiiCloudStorageSDK-{version}.jar is added
    * 2 XML resources in following places:  
{project root}/res/xml/pfsyncservice_logconfig.xml  
{project root}/res/xml/pfsyncservice_preferences.xml
    * AndroidManifest.xml

permission under `<manifest>` element:

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>

sync service under `<application>` element:  

    <application>
    .....
            <service android:name="com.synclore.android.pfscsdk.PFSyncService" >
                <intent-filter >
                    <action android:name="com.synclore.android.pfscsdk.ISessionManager" />
                    <action android:name="com.synclore.android.pfscsdk.IChangeLogger" />
                </intent-filter>
                <meta-data
                    android:name="com.synclore.android.pfscsdk.PFSyncService.preferences"
                    android:resource="@xml/pfsyncservice_preferences" />
                <meta-data
                    android:name="com.synclore.android.pfscsdk.PFSyncService.logconfig"
                    android:resource="@xml/pfsyncservice_logconfig" />
            </service>
    .....
    </application>

Sync content provider under `<application>` element:  
NOTE: where {APPLICATION_ID} and {APPLICATION_KEY} must be replaced with your Application Key and Application ID.

    <application>
    .....
            <provider
                android:authorities="com.kii.sync.generic.c42a57d0"
                android:label="generic content provider"
                android:name="com.kii.sync.provider.GenericSyncProvider" >
                <meta-data
                    android:name="app-key"
                    android:value="224b6595df9387530feb6f696a51c658" />
            </provider>
    .....
    </application>

### Create an application user

Look into HelloSyncActivity.java

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

### Upload file by Sync SDK 

Look into HelloSyncActivity.java

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

### Confirm data that has uploaded

* View the file on Web: Click the link on Sample app. It will appear after the upload succeeded.
* View the file on PC Client: To be added.

## Next step.
* To see full functionality of SyncSDK: <http://static.kii.com/devportal/docs/sync/>
* To install PC Client (SyncBox) and see the upload data on PC:
    * Customize AppId/AppKey of SyncBox: <https://github.com/satoshikumano/SyncBoxDoc/blob/master/Products-SyncBox(PCsyncapplication)Custominstaller-13Mar12-1155AM-10.pdf>
    * Installation guide of SyncBox: <https://github.com/satoshikumano/SyncBoxDoc/blob/master/Products-SyncBox(PCsyncapplication)Overview-14Mar12-0638AM-12.pdf>
    * If you want to try without customize SyncBox, using following appId, appKey on HelloSyncSampleApp.  
    appId: 6b587cee  
    appKey: 82946bf6961f4e34d3a53bbc941ac115  
