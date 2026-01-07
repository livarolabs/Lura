package com.lura.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lura.data.db.entity.BookEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class BookDao_Impl implements BookDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BookEntity> __insertionAdapterOfBookEntity;

  private final EntityDeletionOrUpdateAdapter<BookEntity> __deletionAdapterOfBookEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateProgress;

  public BookDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBookEntity = new EntityInsertionAdapter<BookEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `books` (`id`,`title`,`author`,`filePath`,`coverImagePath`,`publisher`,`publicationDate`,`isbn`,`description`,`fileSize`,`currentPage`,`totalPages`,`currentChapter`,`progressPercentage`,`lastReadTimestamp`,`readingTimeMinutes`,`folderId`,`tags`,`isFavorite`,`readingStatus`,`coverUrl`,`progress`,`totalWords`,`importDate`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BookEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getAuthor());
        statement.bindString(4, entity.getFilePath());
        if (entity.getCoverImagePath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCoverImagePath());
        }
        if (entity.getPublisher() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPublisher());
        }
        if (entity.getPublicationDate() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPublicationDate());
        }
        if (entity.getIsbn() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getIsbn());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getDescription());
        }
        statement.bindLong(10, entity.getFileSize());
        statement.bindLong(11, entity.getCurrentPage());
        statement.bindLong(12, entity.getTotalPages());
        statement.bindLong(13, entity.getCurrentChapter());
        statement.bindDouble(14, entity.getProgressPercentage());
        statement.bindLong(15, entity.getLastReadTimestamp());
        statement.bindLong(16, entity.getReadingTimeMinutes());
        if (entity.getFolderId() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getFolderId());
        }
        statement.bindString(18, entity.getTags());
        final int _tmp = entity.isFavorite() ? 1 : 0;
        statement.bindLong(19, _tmp);
        statement.bindString(20, entity.getReadingStatus());
        if (entity.getCoverUrl() == null) {
          statement.bindNull(21);
        } else {
          statement.bindString(21, entity.getCoverUrl());
        }
        statement.bindDouble(22, entity.getProgress());
        statement.bindLong(23, entity.getTotalWords());
        statement.bindLong(24, entity.getImportDate());
      }
    };
    this.__deletionAdapterOfBookEntity = new EntityDeletionOrUpdateAdapter<BookEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `books` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BookEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateProgress = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE books SET progress = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertBook(final BookEntity book, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBookEntity.insert(book);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBook(final BookEntity book, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBookEntity.handle(book);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProgress(final String id, final float progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateProgress.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, progress);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfUpdateProgress.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BookEntity>> getAllBooks() {
    final String _sql = "SELECT * FROM books ORDER BY importDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImagePath");
          final int _cursorIndexOfPublisher = CursorUtil.getColumnIndexOrThrow(_cursor, "publisher");
          final int _cursorIndexOfPublicationDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publicationDate");
          final int _cursorIndexOfIsbn = CursorUtil.getColumnIndexOrThrow(_cursor, "isbn");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfCurrentPage = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPage");
          final int _cursorIndexOfTotalPages = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPages");
          final int _cursorIndexOfCurrentChapter = CursorUtil.getColumnIndexOrThrow(_cursor, "currentChapter");
          final int _cursorIndexOfProgressPercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "progressPercentage");
          final int _cursorIndexOfLastReadTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReadTimestamp");
          final int _cursorIndexOfReadingTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "readingTimeMinutes");
          final int _cursorIndexOfFolderId = CursorUtil.getColumnIndexOrThrow(_cursor, "folderId");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReadingStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "readingStatus");
          final int _cursorIndexOfCoverUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverUrl");
          final int _cursorIndexOfProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "progress");
          final int _cursorIndexOfTotalWords = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWords");
          final int _cursorIndexOfImportDate = CursorUtil.getColumnIndexOrThrow(_cursor, "importDate");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpCoverImagePath;
            if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
              _tmpCoverImagePath = null;
            } else {
              _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
            }
            final String _tmpPublisher;
            if (_cursor.isNull(_cursorIndexOfPublisher)) {
              _tmpPublisher = null;
            } else {
              _tmpPublisher = _cursor.getString(_cursorIndexOfPublisher);
            }
            final String _tmpPublicationDate;
            if (_cursor.isNull(_cursorIndexOfPublicationDate)) {
              _tmpPublicationDate = null;
            } else {
              _tmpPublicationDate = _cursor.getString(_cursorIndexOfPublicationDate);
            }
            final String _tmpIsbn;
            if (_cursor.isNull(_cursorIndexOfIsbn)) {
              _tmpIsbn = null;
            } else {
              _tmpIsbn = _cursor.getString(_cursorIndexOfIsbn);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final int _tmpCurrentPage;
            _tmpCurrentPage = _cursor.getInt(_cursorIndexOfCurrentPage);
            final int _tmpTotalPages;
            _tmpTotalPages = _cursor.getInt(_cursorIndexOfTotalPages);
            final int _tmpCurrentChapter;
            _tmpCurrentChapter = _cursor.getInt(_cursorIndexOfCurrentChapter);
            final float _tmpProgressPercentage;
            _tmpProgressPercentage = _cursor.getFloat(_cursorIndexOfProgressPercentage);
            final long _tmpLastReadTimestamp;
            _tmpLastReadTimestamp = _cursor.getLong(_cursorIndexOfLastReadTimestamp);
            final int _tmpReadingTimeMinutes;
            _tmpReadingTimeMinutes = _cursor.getInt(_cursorIndexOfReadingTimeMinutes);
            final String _tmpFolderId;
            if (_cursor.isNull(_cursorIndexOfFolderId)) {
              _tmpFolderId = null;
            } else {
              _tmpFolderId = _cursor.getString(_cursorIndexOfFolderId);
            }
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final String _tmpReadingStatus;
            _tmpReadingStatus = _cursor.getString(_cursorIndexOfReadingStatus);
            final String _tmpCoverUrl;
            if (_cursor.isNull(_cursorIndexOfCoverUrl)) {
              _tmpCoverUrl = null;
            } else {
              _tmpCoverUrl = _cursor.getString(_cursorIndexOfCoverUrl);
            }
            final float _tmpProgress;
            _tmpProgress = _cursor.getFloat(_cursorIndexOfProgress);
            final int _tmpTotalWords;
            _tmpTotalWords = _cursor.getInt(_cursorIndexOfTotalWords);
            final long _tmpImportDate;
            _tmpImportDate = _cursor.getLong(_cursorIndexOfImportDate);
            _item = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpFilePath,_tmpCoverImagePath,_tmpPublisher,_tmpPublicationDate,_tmpIsbn,_tmpDescription,_tmpFileSize,_tmpCurrentPage,_tmpTotalPages,_tmpCurrentChapter,_tmpProgressPercentage,_tmpLastReadTimestamp,_tmpReadingTimeMinutes,_tmpFolderId,_tmpTags,_tmpIsFavorite,_tmpReadingStatus,_tmpCoverUrl,_tmpProgress,_tmpTotalWords,_tmpImportDate);
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

  @Override
  public Object getBookById(final String id, final Continuation<? super BookEntity> $completion) {
    final String _sql = "SELECT * FROM books WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<BookEntity>() {
      @Override
      @Nullable
      public BookEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
          final int _cursorIndexOfCoverImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "coverImagePath");
          final int _cursorIndexOfPublisher = CursorUtil.getColumnIndexOrThrow(_cursor, "publisher");
          final int _cursorIndexOfPublicationDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publicationDate");
          final int _cursorIndexOfIsbn = CursorUtil.getColumnIndexOrThrow(_cursor, "isbn");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfCurrentPage = CursorUtil.getColumnIndexOrThrow(_cursor, "currentPage");
          final int _cursorIndexOfTotalPages = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPages");
          final int _cursorIndexOfCurrentChapter = CursorUtil.getColumnIndexOrThrow(_cursor, "currentChapter");
          final int _cursorIndexOfProgressPercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "progressPercentage");
          final int _cursorIndexOfLastReadTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReadTimestamp");
          final int _cursorIndexOfReadingTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "readingTimeMinutes");
          final int _cursorIndexOfFolderId = CursorUtil.getColumnIndexOrThrow(_cursor, "folderId");
          final int _cursorIndexOfTags = CursorUtil.getColumnIndexOrThrow(_cursor, "tags");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfReadingStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "readingStatus");
          final int _cursorIndexOfCoverUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverUrl");
          final int _cursorIndexOfProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "progress");
          final int _cursorIndexOfTotalWords = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWords");
          final int _cursorIndexOfImportDate = CursorUtil.getColumnIndexOrThrow(_cursor, "importDate");
          final BookEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpFilePath;
            _tmpFilePath = _cursor.getString(_cursorIndexOfFilePath);
            final String _tmpCoverImagePath;
            if (_cursor.isNull(_cursorIndexOfCoverImagePath)) {
              _tmpCoverImagePath = null;
            } else {
              _tmpCoverImagePath = _cursor.getString(_cursorIndexOfCoverImagePath);
            }
            final String _tmpPublisher;
            if (_cursor.isNull(_cursorIndexOfPublisher)) {
              _tmpPublisher = null;
            } else {
              _tmpPublisher = _cursor.getString(_cursorIndexOfPublisher);
            }
            final String _tmpPublicationDate;
            if (_cursor.isNull(_cursorIndexOfPublicationDate)) {
              _tmpPublicationDate = null;
            } else {
              _tmpPublicationDate = _cursor.getString(_cursorIndexOfPublicationDate);
            }
            final String _tmpIsbn;
            if (_cursor.isNull(_cursorIndexOfIsbn)) {
              _tmpIsbn = null;
            } else {
              _tmpIsbn = _cursor.getString(_cursorIndexOfIsbn);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final int _tmpCurrentPage;
            _tmpCurrentPage = _cursor.getInt(_cursorIndexOfCurrentPage);
            final int _tmpTotalPages;
            _tmpTotalPages = _cursor.getInt(_cursorIndexOfTotalPages);
            final int _tmpCurrentChapter;
            _tmpCurrentChapter = _cursor.getInt(_cursorIndexOfCurrentChapter);
            final float _tmpProgressPercentage;
            _tmpProgressPercentage = _cursor.getFloat(_cursorIndexOfProgressPercentage);
            final long _tmpLastReadTimestamp;
            _tmpLastReadTimestamp = _cursor.getLong(_cursorIndexOfLastReadTimestamp);
            final int _tmpReadingTimeMinutes;
            _tmpReadingTimeMinutes = _cursor.getInt(_cursorIndexOfReadingTimeMinutes);
            final String _tmpFolderId;
            if (_cursor.isNull(_cursorIndexOfFolderId)) {
              _tmpFolderId = null;
            } else {
              _tmpFolderId = _cursor.getString(_cursorIndexOfFolderId);
            }
            final String _tmpTags;
            _tmpTags = _cursor.getString(_cursorIndexOfTags);
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final String _tmpReadingStatus;
            _tmpReadingStatus = _cursor.getString(_cursorIndexOfReadingStatus);
            final String _tmpCoverUrl;
            if (_cursor.isNull(_cursorIndexOfCoverUrl)) {
              _tmpCoverUrl = null;
            } else {
              _tmpCoverUrl = _cursor.getString(_cursorIndexOfCoverUrl);
            }
            final float _tmpProgress;
            _tmpProgress = _cursor.getFloat(_cursorIndexOfProgress);
            final int _tmpTotalWords;
            _tmpTotalWords = _cursor.getInt(_cursorIndexOfTotalWords);
            final long _tmpImportDate;
            _tmpImportDate = _cursor.getLong(_cursorIndexOfImportDate);
            _result = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpFilePath,_tmpCoverImagePath,_tmpPublisher,_tmpPublicationDate,_tmpIsbn,_tmpDescription,_tmpFileSize,_tmpCurrentPage,_tmpTotalPages,_tmpCurrentChapter,_tmpProgressPercentage,_tmpLastReadTimestamp,_tmpReadingTimeMinutes,_tmpFolderId,_tmpTags,_tmpIsFavorite,_tmpReadingStatus,_tmpCoverUrl,_tmpProgress,_tmpTotalWords,_tmpImportDate);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
