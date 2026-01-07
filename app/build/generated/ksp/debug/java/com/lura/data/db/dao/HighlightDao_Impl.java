package com.lura.data.db.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lura.data.db.entity.Highlight;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HighlightDao_Impl implements HighlightDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Highlight> __insertionAdapterOfHighlight;

  private final SharedSQLiteStatement __preparedStmtOfDeleteHighlight;

  public HighlightDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHighlight = new EntityInsertionAdapter<Highlight>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `highlights` (`id`,`bookId`,`chapterIndex`,`elementIndex`,`startIndex`,`endIndex`,`color`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Highlight entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getBookId());
        statement.bindLong(3, entity.getChapterIndex());
        statement.bindLong(4, entity.getElementIndex());
        statement.bindLong(5, entity.getStartIndex());
        statement.bindLong(6, entity.getEndIndex());
        statement.bindLong(7, entity.getColor());
        statement.bindLong(8, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfDeleteHighlight = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM highlights WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertHighlight(final Highlight highlight,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfHighlight.insertAndReturnId(highlight);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteHighlight(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteHighlight.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteHighlight.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Highlight>> getHighlights(final String bookId, final int chapterIndex) {
    final String _sql = "SELECT * FROM highlights WHERE bookId = ? AND chapterIndex = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, bookId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, chapterIndex);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"highlights"}, new Callable<List<Highlight>>() {
      @Override
      @NonNull
      public List<Highlight> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBookId = CursorUtil.getColumnIndexOrThrow(_cursor, "bookId");
          final int _cursorIndexOfChapterIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "chapterIndex");
          final int _cursorIndexOfElementIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "elementIndex");
          final int _cursorIndexOfStartIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "startIndex");
          final int _cursorIndexOfEndIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "endIndex");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Highlight> _result = new ArrayList<Highlight>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Highlight _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpBookId;
            _tmpBookId = _cursor.getString(_cursorIndexOfBookId);
            final int _tmpChapterIndex;
            _tmpChapterIndex = _cursor.getInt(_cursorIndexOfChapterIndex);
            final int _tmpElementIndex;
            _tmpElementIndex = _cursor.getInt(_cursorIndexOfElementIndex);
            final int _tmpStartIndex;
            _tmpStartIndex = _cursor.getInt(_cursorIndexOfStartIndex);
            final int _tmpEndIndex;
            _tmpEndIndex = _cursor.getInt(_cursorIndexOfEndIndex);
            final int _tmpColor;
            _tmpColor = _cursor.getInt(_cursorIndexOfColor);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Highlight(_tmpId,_tmpBookId,_tmpChapterIndex,_tmpElementIndex,_tmpStartIndex,_tmpEndIndex,_tmpColor,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
