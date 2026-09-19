package com.kjbilling.app.data.db.dao;

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
import com.kjbilling.app.data.db.entity.CustomerEntity;
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
public final class CustomerDao_Impl implements CustomerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CustomerEntity> __insertionAdapterOfCustomerEntity;

  private final EntityDeletionOrUpdateAdapter<CustomerEntity> __deletionAdapterOfCustomerEntity;

  private final EntityDeletionOrUpdateAdapter<CustomerEntity> __updateAdapterOfCustomerEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateLastUsedAt;

  public CustomerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCustomerEntity = new EntityInsertionAdapter<CustomerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `customers` (`id`,`name`,`mobile`,`email`,`billingAddress`,`state`,`pincode`,`gstin`,`businessName`,`notes`,`isWalkIn`,`lastUsedAt`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CustomerEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getMobile() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMobile());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEmail());
        }
        if (entity.getBillingAddress() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBillingAddress());
        }
        if (entity.getState() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getState());
        }
        if (entity.getPincode() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPincode());
        }
        if (entity.getGstin() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getGstin());
        }
        if (entity.getBusinessName() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getBusinessName());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNotes());
        }
        final int _tmp = entity.isWalkIn() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getLastUsedAt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getLastUsedAt());
        }
        statement.bindLong(13, entity.getCreatedAt());
        statement.bindLong(14, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfCustomerEntity = new EntityDeletionOrUpdateAdapter<CustomerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `customers` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CustomerEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfCustomerEntity = new EntityDeletionOrUpdateAdapter<CustomerEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `customers` SET `id` = ?,`name` = ?,`mobile` = ?,`email` = ?,`billingAddress` = ?,`state` = ?,`pincode` = ?,`gstin` = ?,`businessName` = ?,`notes` = ?,`isWalkIn` = ?,`lastUsedAt` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CustomerEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getMobile() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMobile());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getEmail());
        }
        if (entity.getBillingAddress() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBillingAddress());
        }
        if (entity.getState() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getState());
        }
        if (entity.getPincode() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPincode());
        }
        if (entity.getGstin() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getGstin());
        }
        if (entity.getBusinessName() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getBusinessName());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getNotes());
        }
        final int _tmp = entity.isWalkIn() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getLastUsedAt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getLastUsedAt());
        }
        statement.bindLong(13, entity.getCreatedAt());
        statement.bindLong(14, entity.getUpdatedAt());
        statement.bindLong(15, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateLastUsedAt = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE customers SET lastUsedAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final CustomerEntity customer,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCustomerEntity.insertAndReturnId(customer);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final CustomerEntity customer,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfCustomerEntity.handle(customer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final CustomerEntity customer,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfCustomerEntity.handle(customer);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateLastUsedAt(final long id, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateLastUsedAt.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
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
          __preparedStmtOfUpdateLastUsedAt.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CustomerEntity>> getAll() {
    final String _sql = "SELECT * FROM customers ORDER BY lastUsedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"customers"}, new Callable<List<CustomerEntity>>() {
      @Override
      @NonNull
      public List<CustomerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfBillingAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "billingAddress");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfPincode = CursorUtil.getColumnIndexOrThrow(_cursor, "pincode");
          final int _cursorIndexOfGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "gstin");
          final int _cursorIndexOfBusinessName = CursorUtil.getColumnIndexOrThrow(_cursor, "businessName");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsWalkIn = CursorUtil.getColumnIndexOrThrow(_cursor, "isWalkIn");
          final int _cursorIndexOfLastUsedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<CustomerEntity> _result = new ArrayList<CustomerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomerEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpBillingAddress;
            if (_cursor.isNull(_cursorIndexOfBillingAddress)) {
              _tmpBillingAddress = null;
            } else {
              _tmpBillingAddress = _cursor.getString(_cursorIndexOfBillingAddress);
            }
            final String _tmpState;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmpState = null;
            } else {
              _tmpState = _cursor.getString(_cursorIndexOfState);
            }
            final String _tmpPincode;
            if (_cursor.isNull(_cursorIndexOfPincode)) {
              _tmpPincode = null;
            } else {
              _tmpPincode = _cursor.getString(_cursorIndexOfPincode);
            }
            final String _tmpGstin;
            if (_cursor.isNull(_cursorIndexOfGstin)) {
              _tmpGstin = null;
            } else {
              _tmpGstin = _cursor.getString(_cursorIndexOfGstin);
            }
            final String _tmpBusinessName;
            if (_cursor.isNull(_cursorIndexOfBusinessName)) {
              _tmpBusinessName = null;
            } else {
              _tmpBusinessName = _cursor.getString(_cursorIndexOfBusinessName);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final boolean _tmpIsWalkIn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsWalkIn);
            _tmpIsWalkIn = _tmp != 0;
            final Long _tmpLastUsedAt;
            if (_cursor.isNull(_cursorIndexOfLastUsedAt)) {
              _tmpLastUsedAt = null;
            } else {
              _tmpLastUsedAt = _cursor.getLong(_cursorIndexOfLastUsedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new CustomerEntity(_tmpId,_tmpName,_tmpMobile,_tmpEmail,_tmpBillingAddress,_tmpState,_tmpPincode,_tmpGstin,_tmpBusinessName,_tmpNotes,_tmpIsWalkIn,_tmpLastUsedAt,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<CustomerEntity>> search(final String query) {
    final String _sql = "SELECT * FROM customers WHERE name LIKE '%' || ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"customers"}, new Callable<List<CustomerEntity>>() {
      @Override
      @NonNull
      public List<CustomerEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfBillingAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "billingAddress");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfPincode = CursorUtil.getColumnIndexOrThrow(_cursor, "pincode");
          final int _cursorIndexOfGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "gstin");
          final int _cursorIndexOfBusinessName = CursorUtil.getColumnIndexOrThrow(_cursor, "businessName");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsWalkIn = CursorUtil.getColumnIndexOrThrow(_cursor, "isWalkIn");
          final int _cursorIndexOfLastUsedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<CustomerEntity> _result = new ArrayList<CustomerEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomerEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpBillingAddress;
            if (_cursor.isNull(_cursorIndexOfBillingAddress)) {
              _tmpBillingAddress = null;
            } else {
              _tmpBillingAddress = _cursor.getString(_cursorIndexOfBillingAddress);
            }
            final String _tmpState;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmpState = null;
            } else {
              _tmpState = _cursor.getString(_cursorIndexOfState);
            }
            final String _tmpPincode;
            if (_cursor.isNull(_cursorIndexOfPincode)) {
              _tmpPincode = null;
            } else {
              _tmpPincode = _cursor.getString(_cursorIndexOfPincode);
            }
            final String _tmpGstin;
            if (_cursor.isNull(_cursorIndexOfGstin)) {
              _tmpGstin = null;
            } else {
              _tmpGstin = _cursor.getString(_cursorIndexOfGstin);
            }
            final String _tmpBusinessName;
            if (_cursor.isNull(_cursorIndexOfBusinessName)) {
              _tmpBusinessName = null;
            } else {
              _tmpBusinessName = _cursor.getString(_cursorIndexOfBusinessName);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final boolean _tmpIsWalkIn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsWalkIn);
            _tmpIsWalkIn = _tmp != 0;
            final Long _tmpLastUsedAt;
            if (_cursor.isNull(_cursorIndexOfLastUsedAt)) {
              _tmpLastUsedAt = null;
            } else {
              _tmpLastUsedAt = _cursor.getLong(_cursorIndexOfLastUsedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new CustomerEntity(_tmpId,_tmpName,_tmpMobile,_tmpEmail,_tmpBillingAddress,_tmpState,_tmpPincode,_tmpGstin,_tmpBusinessName,_tmpNotes,_tmpIsWalkIn,_tmpLastUsedAt,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getById(final long id, final Continuation<? super CustomerEntity> $completion) {
    final String _sql = "SELECT * FROM customers WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CustomerEntity>() {
      @Override
      @Nullable
      public CustomerEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfMobile = CursorUtil.getColumnIndexOrThrow(_cursor, "mobile");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfBillingAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "billingAddress");
          final int _cursorIndexOfState = CursorUtil.getColumnIndexOrThrow(_cursor, "state");
          final int _cursorIndexOfPincode = CursorUtil.getColumnIndexOrThrow(_cursor, "pincode");
          final int _cursorIndexOfGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "gstin");
          final int _cursorIndexOfBusinessName = CursorUtil.getColumnIndexOrThrow(_cursor, "businessName");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfIsWalkIn = CursorUtil.getColumnIndexOrThrow(_cursor, "isWalkIn");
          final int _cursorIndexOfLastUsedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final CustomerEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpMobile;
            if (_cursor.isNull(_cursorIndexOfMobile)) {
              _tmpMobile = null;
            } else {
              _tmpMobile = _cursor.getString(_cursorIndexOfMobile);
            }
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            final String _tmpBillingAddress;
            if (_cursor.isNull(_cursorIndexOfBillingAddress)) {
              _tmpBillingAddress = null;
            } else {
              _tmpBillingAddress = _cursor.getString(_cursorIndexOfBillingAddress);
            }
            final String _tmpState;
            if (_cursor.isNull(_cursorIndexOfState)) {
              _tmpState = null;
            } else {
              _tmpState = _cursor.getString(_cursorIndexOfState);
            }
            final String _tmpPincode;
            if (_cursor.isNull(_cursorIndexOfPincode)) {
              _tmpPincode = null;
            } else {
              _tmpPincode = _cursor.getString(_cursorIndexOfPincode);
            }
            final String _tmpGstin;
            if (_cursor.isNull(_cursorIndexOfGstin)) {
              _tmpGstin = null;
            } else {
              _tmpGstin = _cursor.getString(_cursorIndexOfGstin);
            }
            final String _tmpBusinessName;
            if (_cursor.isNull(_cursorIndexOfBusinessName)) {
              _tmpBusinessName = null;
            } else {
              _tmpBusinessName = _cursor.getString(_cursorIndexOfBusinessName);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final boolean _tmpIsWalkIn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsWalkIn);
            _tmpIsWalkIn = _tmp != 0;
            final Long _tmpLastUsedAt;
            if (_cursor.isNull(_cursorIndexOfLastUsedAt)) {
              _tmpLastUsedAt = null;
            } else {
              _tmpLastUsedAt = _cursor.getLong(_cursorIndexOfLastUsedAt);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new CustomerEntity(_tmpId,_tmpName,_tmpMobile,_tmpEmail,_tmpBillingAddress,_tmpState,_tmpPincode,_tmpGstin,_tmpBusinessName,_tmpNotes,_tmpIsWalkIn,_tmpLastUsedAt,_tmpCreatedAt,_tmpUpdatedAt);
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
