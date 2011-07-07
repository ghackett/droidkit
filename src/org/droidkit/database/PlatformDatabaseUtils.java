package org.droidkit.database;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.text.TextUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PlatformDatabaseUtils {
    public static final int CONFLICT_NONE = SQLiteDatabase.CONFLICT_NONE;
    public static final int CONFLICT_ROLLBACK = SQLiteDatabase.CONFLICT_ROLLBACK;
    public static final int CONFLICT_ABORT = SQLiteDatabase.CONFLICT_ABORT;
    public static final int CONFLICT_FAIL = SQLiteDatabase.CONFLICT_FAIL;
    public static final int CONFLICT_IGNORE = SQLiteDatabase.CONFLICT_IGNORE;
    public static final int CONFLICT_REPLACE = SQLiteDatabase.CONFLICT_REPLACE;

    private static final String[] CONFLICT_VALUES = new String[] {
            "", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ", " OR REPLACE "
    };

    SQLiteOpenHelper mDatabase;

    public PlatformDatabaseUtils(SQLiteOpenHelper db) {
        mDatabase = db;
    }

    public int update(String table, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        return db.update(table, values, where, whereArgs);
    }

    public void execSQL(String query) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        db.execSQL(query);
    }

    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues values, int algorithm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return insert(table, nullColumnHack, values, algorithm);
        } else {
            return legacyInsert(table, nullColumnHack, values, algorithm);
        }
    }

    private long legacyInsert(String table, String nullColumnHack, ContentValues initialValues, int algorithm) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT");
        sql.append(CONFLICT_VALUES[algorithm]);
        sql.append(" INTO ");
        sql.append(table);

        StringBuilder values = new StringBuilder();
        Set<Map.Entry<String, Object>> entrySet = null;

        if (initialValues != null && initialValues.size() > 0) {
            entrySet = initialValues.valueSet();
            Iterator<Map.Entry<String, Object>> entriesIter = entrySet.iterator();
            boolean needsSeparator = false;

            sql.append("(");

            while (entriesIter.hasNext()) {
                if (needsSeparator) {
                    sql.append(", ");
                    values.append(", ");
                }

                needsSeparator = true;
                Map.Entry<String, Object> entry = entriesIter.next();
                sql.append(entry.getKey());
                values.append("?");
            }

            sql.append(")");
        } else {
            sql.append("(").append(nullColumnHack).append(") ");
            values.append("NULL");
        }

        sql.append(" VALUES (");
        sql.append(values);
        sql.append(");");

        SQLiteStatement statement = null;

        try {
            statement = db.compileStatement(sql.toString());

            if (entrySet != null) {
                int size = entrySet.size();
                Iterator<Map.Entry<String, Object>> entriesIter = entrySet.iterator();

                for (int i = 0; i < size; i++) {
                    Map.Entry<String, Object> entry = entriesIter.next();
                    DatabaseUtils.bindObjectToProgram(statement, i + 1, entry.getValue());
                }
            }

            return statement.executeInsert();
        } catch (SQLiteDatabaseCorruptException e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    private long insert(String table, String nullColumnHack, ContentValues values, int algorithm) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        return db.insertWithOnConflict(table, nullColumnHack, values, algorithm);
    }

    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int algorithm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return update(table, values, whereClause, whereArgs, algorithm);
        } else {
            return legacyUpdate(table, values, whereClause, whereArgs, algorithm);
        }
    }

    private int legacyUpdate(String table, ContentValues values, String whereClause, String[] whereArgs, int algorithm) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(CONFLICT_VALUES[algorithm]);
        sql.append(table);
        sql.append(" SET ");

        Set<Map.Entry<String, Object>> entrySet = values.valueSet();
        Iterator<Map.Entry<String, Object>> entriesIter = entrySet.iterator();

        while (entriesIter.hasNext()) {
            Map.Entry<String, Object> entry = entriesIter.next();
            sql.append(entry.getKey());
            sql.append("=?");

            if (entriesIter.hasNext()) {
                sql.append(",");
            }
        }

        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ");
            sql.append(whereClause);
        }

        SQLiteStatement statement = null;

        try {
            statement = db.compileStatement(sql.toString());

            int bindArg = 1;
            int size = entrySet.size();
            entriesIter = entrySet.iterator();

            for (int i = 0; i < size; i++) {
                Map.Entry<String, Object> entry = entriesIter.next();
                DatabaseUtils.bindObjectToProgram(statement, bindArg, entry.getValue());
                bindArg++;
            }

            if (whereArgs != null) {
                size = whereArgs.length;

                for (int i = 0; i < size; i++) {
                    statement.bindString(bindArg, whereArgs[i]);
                    bindArg++;
                }
            }

            return statement.executeUpdateDelete();
        } catch (SQLiteDatabaseCorruptException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    private int update(String table, ContentValues values, String whereClause, String[] whereArgs, int algorithm) {
        SQLiteDatabase db = mDatabase.getWritableDatabase();
        return db.updateWithOnConflict(table, values, whereClause, whereArgs, algorithm);
    }
}
