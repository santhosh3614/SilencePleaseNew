package com.leocardz.silence.please.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.appsforbb.common.sqlitelib.Column;
import com.appsforbb.common.sqlitelib.ColumnTypes;
import com.appsforbb.common.sqlitelib.ObjectTable;

/**
 * Created by santhosh on 3/8/15.
 */
public class SoundLevelTable extends ObjectTable<SoundLevel> {

    private static final String TABLE_NAME = SoundLevelTable.class.getSimpleName();
    private ColumnTypes.LongColumn QUIET_TIME = new ColumnTypes.LongColumn("quiet_time");
    private ColumnTypes.LongColumn GROUP_TIME = new ColumnTypes.LongColumn("group_time");
    private ColumnTypes.LongColumn NOISE_TIME = new ColumnTypes.LongColumn("noise_time");
    private ColumnTypes.LongColumn DATE = new ColumnTypes.LongColumn("date");

    protected SoundLevelTable() {
        super(TABLE_NAME);
        addColumns(QUIET_TIME, GROUP_TIME, NOISE_TIME, DATE);
    }

    @Override
    protected SoundLevel readRecordRow(long row, Cursor cursor) {
        return new SoundLevel(row,
                QUIET_TIME.getValue(cursor),
                GROUP_TIME.getValue(cursor),
                NOISE_TIME.getValue(cursor),
                DATE.getValue(cursor));
    }

    @Override
    protected void fillRecordRow(SQLiteRow sqLiteRow, SoundLevel soundLevel) {
        sqLiteRow.setColumnValue(QUIET_TIME,soundLevel.getQuietTime());
        sqLiteRow.setColumnValue(GROUP_TIME,soundLevel.getGroupTime());
        sqLiteRow.setColumnValue(NOISE_TIME,soundLevel.getNoiseTime());
        sqLiteRow.setColumnValue(DATE,soundLevel.getDate());
    }


    public SoundLevel getSoundLevel(long date){
        return getSingleRecord(WHERE(DATE+"= ?",date));
    }

    public SoundLevel insetSound(long quietTime,long groupTime,long noiseTime,long date){
        SoundLevel soundLevel = new SoundLevel(-1, quietTime, groupTime, noiseTime, date);
        if(insertNewRecord(soundLevel)>0){
            return soundLevel;
        }
        return null;
    }


    public void updateSound(SoundLevel soundLevel){
        ContentValues contentValues=new ContentValues();
        contentValues.put(QUIET_TIME.toString(),soundLevel.getQuietTime());
        contentValues.put(GROUP_TIME.toString(), soundLevel.getGroupTime());
        contentValues.put(NOISE_TIME.toString(),soundLevel.getNoiseTime());
        updateByRowId(contentValues, soundLevel.getRowId());
    }


}
