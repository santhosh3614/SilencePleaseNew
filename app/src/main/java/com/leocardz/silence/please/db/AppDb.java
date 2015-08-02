package com.leocardz.silence.please.db;

import android.annotation.TargetApi;
import android.os.Build;

import com.appsforbb.common.sqlitelib.SQLiteStore;

/**
 * Created by santhosh on 3/8/15.
 */
public class AppDb {

    private static AppDb appDb;
    private static final String DATABASE_NAME = "applocklib.db";
    private static final int DATABASE_VERSION = 1;
    private final SQLiteStore store;

    public SoundLevelTable getSoundLevelTable() {
        return soundLevelTable;
    }

    private SoundLevelTable soundLevelTable=new SoundLevelTable();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private AppDb(){
        store = SQLiteStore.getInstance(DATABASE_NAME, DATABASE_VERSION,soundLevelTable);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            store.getDatabase().setForeignKeyConstraintsEnabled(true);
        }
    }

    public static AppDb getInstance() {
        synchronized (AppDb.class) {
            if (appDb == null)
                appDb = new AppDb();
        }
        return appDb;
    }

}
