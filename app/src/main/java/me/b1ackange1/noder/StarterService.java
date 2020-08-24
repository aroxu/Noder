package me.b1ackange1.noder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;

import androidx.annotation.Nullable;

import java.io.File;

import static me.b1ackange1.noder.Utils.copyAssetFolder;
import static me.b1ackange1.noder.Utils.deleteFolderRecursively;
import static me.b1ackange1.noder.Utils.saveLastUpdateTime;
import static me.b1ackange1.noder.Utils.wasAPKUpdated;

public class StarterService extends Service {

    public static final String BROADCAST_STARTED = "node.broadcast.started";
    public static final String BROADCAST_FINISHED = "node.broadcast.finished";
    public static final int PORT = 3000;
    public static final int NOTIFICATION_ID = 2;
    public static final String NOTIFICATION_CHANNEL_ID = "Noder notification";

    @Override
    public void onCreate() {
        super.onCreate();

        startForeground(NOTIFICATION_ID, createNotification());

        new Thread(new Runnable() {
            @Override
            public void run() {
                //The path where we expect the node project to be at runtime.
                String nodeDir=getApplicationContext().getFilesDir().getAbsolutePath()+"/nodejs-on-android";
                if (wasAPKUpdated(getApplicationContext())) {
                    //Recursively delete any existing nodejs-project.
                    File nodeDirReference=new File(nodeDir);
                    if (nodeDirReference.exists()) {
                        deleteFolderRecursively(new File(nodeDir));
                    }
                    //Copy the node project from assets into the application's data path.
                    copyAssetFolder(getApplicationContext().getAssets(), "nodejs-on-android", nodeDir);

                    saveLastUpdateTime(getApplicationContext());
                }startNode("node", nodeDir+"/app.js", nodeDir);
            }
        }).start();

        sendBroadcast(new Intent(BROADCAST_STARTED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent(BROADCAST_FINISHED));

        // This ugly hack is for now necessary to kill node's process
        Process.killProcess(Utils.getPid(this, getString(R.string.node_process_name)));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Noder", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(channel);
            return new Notification.Builder(this,NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(this.getText(R.string.app_name))
                    .setContentText(this.getText(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setTicker(this.getText(R.string.app_name))
                    .build();
        }
        else {
            return new Notification.Builder(this)
                    .setContentTitle(this.getText(R.string.app_name))
                    .setContentText(this.getText(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setTicker(this.getText(R.string.app_name))
                    .build();
        }
    }

    static {
        System.loadLibrary("node");
        System.loadLibrary("native-lib");
    }

    private native void startNode(String... argv);
}