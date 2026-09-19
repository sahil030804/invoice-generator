package com.kjbilling.app.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.kjbilling.app.data.db.entity.BusinessProfileEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BusinessProfileDao_Impl implements BusinessProfileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BusinessProfileEntity> __insertionAdapterOfBusinessProfileEntity;

  public BusinessProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBusinessProfileEntity = new EntityInsertionAdapter<BusinessProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `business_profiles` (`id`,`businessName`,`ownerName`,`mobile`,`address`,`state`,`gstin`,`email`,`city`,`pincode`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BusinessProfileEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getBusinessName());
        statement.bindString(3, entity.getOwnerName());
        statement.bindString(4, entity.getMobile());
        statement.bindString(5, entity.getAddress());
        statement.bindString(6, entity.getState());
        if (entity.getGstin() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getGstin());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getEmail());
        }
        if (entity.getCity() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCity());
        }
        if (entity.getPincode() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getPincode());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getUpdatedAt());
      }
    };
  }

  @Override
  public Object upsert(final BusinessProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBusinessProfileEntity.insert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<BusinessProfileEntity> getProfile() {
    final String _sql = "SELECT * FROM business_profiles WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"business_profiles"}, new Callable<BusinessProfileEntity>() {
      @Override
      @Nullable
      public BusinessProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBusinessName = CursorUtil.getColumnIndexOrThrow(_cursor, "businessName");
          final int _cursorIndexOfOwnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerName");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "gstin");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfCity = CursorUtil.getColumnIndexOrThrow(_cursor, "city");
          final int _cursorIndexOfPincode = CursorUtil.getColumnIndexOrThrow(_cursor, "pincode");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final BusinessProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpBusinessName;
            _tmpBusinessName = _cursor.getString(_cursorIndexOfBusinessName);
            final String _tmpOwnerName;
            _tmpOwnerName = _cursor.getString(_cursorIndexOfOwnerName);
            final String _tmpMobile;
            _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final String _tmpGstin;
            if (_cursor.isNull(_cursorIndexOfGstin)) {
              _tmpGstin = null;
            } else {
              _tmpGstin = _cursor.getString(_cursorIndexOfGstin);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpCity;
            if (_cursor.isNull(_cursorIndexOfCity)) {
              _tmpCity = null;
            } else {
              _tmpCity = _cursor.getString(_cursorIndexOfCity);
            }
            final String _tmpPincode;
            if (_cursor.isNull(_cursorIndexOfPincode)) {
              _tmpPincode = null;
            } else {
              _tmpPincode = _cursor.getString(_cursorIndexOfPincode);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new BusinessProfileEntity(_tmpId,_tmpBusinessName,_tmpOwnerName,_tmpMobile,_tmpAddress,_tmpState,_tmpGstin,_tmpEmail,_tmpCity,_tmpPincode,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
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
  public Object getProfileOnce(final Continuation<? super BusinessProfileEntity> $completion) {
    final String _sql = "SELECT * FROM business_profiles WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<BusinessProfileEntity>() {
      @Override
      @Nullable
      public BusinessProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBusinessName = CursorUtil.getColumnIndexOrThrow(_cursor, "businessName");
          final int _cursorIndexOfOwnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerName");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "gstin");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfCity = CursorUtil.getColumnIndexOrThrow(_cursor, "city");
          final int _cursorIndexOfPincode = CursorUtil.getColumnIndexOrThrow(_cursor, "pincode");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final BusinessProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpBusinessName;
            _tmpBusinessName = _cursor.getString(_cursorIndexOfBusinessName);
            final String _tmpOwnerName;
            _tmpOwnerName = _cursor.getString(_cursorIndexOfOwnerName);
            final String _tmpMobile;
            _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            final String _tmpAddress;
            _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            final String _tmpState;
            _tmpState = _cursor.getString(_cursorIndexOfState);
            final String _tmpGstin;
            if (_cursor.isNull(_cursorIndexOfGstin)) {
              _tmpGstin = null;
            } else {
              _tmpGstin = _cursor.getString(_cursorIndexOfGstin);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpCity;
            if (_cursor.isNull(_cursorIndexOfCity)) {
              _tmpCity = null;
            } else {
              _tmpCity = _cursor.getString(_cursorIndexOfCity);
            }
            final String _tmpPincode;
            if (_cursor.isNull(_cursorIndexOfPincode)) {
              _tmpPincode = null;
            } else {
              _tmpPincode = _cursor.getString(_cursorIndexOfPincode);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new BusinessProfileEntity(_tmpId,_tmpBusinessName,_tmpOwnerName,_tmpMobile,_tmpAddress,_tmpState,_tmpGstin,_tmpEmail,_tmpCity,_tmpPincode,_tmpCreatedAt,_tmpUpdatedAt);
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
