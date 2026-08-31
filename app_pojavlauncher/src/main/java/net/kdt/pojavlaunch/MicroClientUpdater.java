package net.kdt.pojavlaunch;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MicroClientUpdater {
    public static void updateMixin(final Activity context, final Runnable onComplete) {
        if (!Tools.isOnline(context)) {
            Log.i("MicroClientUpdater", "No internet connection, skipping mixin update");
            if (onComplete != null) {
                Tools.runOnUiThread(onComplete);
            }
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("MicroClientUpdater", "Checking for mixin updates...");
                    String mixinUrl = "https://cdn-microclient.komas19.party/MicroClientMixin.jar?t=" + System.currentTimeMillis();
                    net.kdt.pojavlaunch.Logger.appendToLog("MicroClientUpdater: Checking for mixin updates at " + mixinUrl);
                    File targetDir = new File(Tools.DIR_HOME_LIBRARY, "party/komas19/microclient/stable");
                    if (!targetDir.exists()) {
                        targetDir.mkdirs();
                    }
                    File targetFile = new File(targetDir, "microclient-stable.jar");
                    
                    HttpURLConnection connection = (HttpURLConnection) new URL(mixinUrl).openConnection();
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Cache-Control", "no-cache");
                    connection.connect();
                    
                    int responseCode = connection.getResponseCode();
                    net.kdt.pojavlaunch.Logger.appendToLog("MicroClientUpdater: Server responded with " + responseCode);
                    
                    if (responseCode == 200) {
                        InputStream in = connection.getInputStream();
                        FileOutputStream out = new FileOutputStream(targetFile);
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        out.flush();
                        out.close();
                        in.close();
                        
                        Log.i("MicroClientUpdater", "Mixin updated successfully");
                        net.kdt.pojavlaunch.Logger.appendToLog("MicroClientUpdater: Mixin updated successfully, saved to " + targetFile.getAbsolutePath());
                    } else {
                        net.kdt.pojavlaunch.Logger.appendToLog("MicroClientUpdater: Failed, expected 200 but got " + responseCode);
                    }
                } catch (Exception e) {
                    Log.e("MicroClientUpdater", "Failed to update mixin", e);
                    net.kdt.pojavlaunch.Logger.appendToLog("MicroClientUpdater: Exception " + e.toString());
                }
                
                if (onComplete != null) {
                    Tools.runOnUiThread(onComplete);
                }
            }
        });
        executor.shutdown();
    }
}
