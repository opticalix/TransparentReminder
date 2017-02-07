package com.opticalix.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


import com.opticalix.storage.bean.Note;
import com.opticalix.storage.dao.DaoMaster;
import com.opticalix.storage.dao.NoteDao;

import java.util.List;

/**
 * Created by opticalix@gmail.com on 15/11/30.
 */
public class MyStorage {

    // 单例
    private static MyStorage mHelper;
    private final Context mContext;

    private MyStorage(Context context) {
        mContext = context;
        setupDb();
    }

    public static MyStorage getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new MyStorage(context);
        }
        return mHelper;
    }

    private DaoMaster.OpenHelper mOpenHelper;
    private SQLiteDatabase mDb;
    private DaoMaster mDaoMaster;


    public NoteDao getDao() {
        return mDaoMaster.newSession().getNoteDao();
    }

    private void setupDb() {
        mOpenHelper = new DaoMaster.DevOpenHelper(mContext, "db_opticalix", null);
        mDb = mOpenHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(mDb);
        mDaoMaster.newSession().clear();
    }

    public void insertNote(Note note){
        getDao().insert(note);
    }

    public List<Note> loadAllNotes(){
        return getDao().queryBuilder().orderDesc(NoteDao.Properties.Update_date).list();
    }

    public void removeNote(Note note){
        getDao().delete(note);
    }

    public void removeAllNote(){
        getDao().deleteAll();
    }

    public void updateNote(Note note){
        getDao().update(note);
    }
}
