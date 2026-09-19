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
import com.kjbilling.app.data.db.converter.Converters;
import com.kjbilling.app.data.db.entity.ProductEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.math.BigDecimal;
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
public final class ProductDao_Impl implements ProductDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProductEntity> __insertionAdapterOfProductEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ProductEntity> __deletionAdapterOfProductEntity;

  private final EntityDeletionOrUpdateAdapter<ProductEntity> __updateAdapterOfProductEntity;

  private final SharedSQLiteStatement __preparedStmtOfIncrementUseCount;

  public ProductDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProductEntity = new EntityInsertionAdapter<ProductEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `products` (`id`,`name`,`sellingPrice`,`hsnCode`,`unit`,`gstRate`,`description`,`sku`,`useCount`,`lastUsedAt`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProductEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        final String _tmp = __converters.fromBigDecimal(entity.getSellingPrice());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp);
        }
        if (entity.getHsnCode() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getHsnCode());
        }
        statement.bindString(5, entity.getUnit());
        final String _tmp_1 = __converters.fromBigDecimal(entity.getGstRate());
        if (_tmp_1 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_1);
        }
        if (entity.getDescription() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDescription());
        }
        if (entity.getSku() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSku());
        }
        statement.bindLong(9, entity.getUseCount());
        if (entity.getLastUsedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getLastUsedAt());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfProductEntity = new EntityDeletionOrUpdateAdapter<ProductEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `products` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProductEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfProductEntity = new EntityDeletionOrUpdateAdapter<ProductEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `products` SET `id` = ?,`name` = ?,`sellingPrice` = ?,`hsnCode` = ?,`unit` = ?,`gstRate` = ?,`description` = ?,`sku` = ?,`useCount` = ?,`lastUsedAt` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProductEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        final String _tmp = __converters.fromBigDecimal(entity.getSellingPrice());
        if (_tmp == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp);
        }
        if (entity.getHsnCode() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getHsnCode());
        }
        statement.bindString(5, entity.getUnit());
        final String _tmp_1 = __converters.fromBigDecimal(entity.getGstRate());
        if (_tmp_1 == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp_1);
        }
        if (entity.getDescription() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getDescription());
        }
        if (entity.getSku() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSku());
        }
        statement.bindLong(9, entity.getUseCount());
        if (entity.getLastUsedAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getLastUsedAt());
        }
        statement.bindLong(11, entity.getCreatedAt());
        statement.bindLong(12, entity.getUpdatedAt());
        statement.bindLong(13, entity.getId());
      }
    };
    this.__preparedStmtOfIncrementUseCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE products SET useCount = useCount + 1, lastUsedAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ProductEntity product, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfProductEntity.insertAndReturnId(product);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ProductEntity product, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfProductEntity.handle(product);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ProductEntity product, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProductEntity.handle(product);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementUseCount(final long id, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementUseCount.acquire();
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
          __preparedStmtOfIncrementUseCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ProductEntity>> getAll() {
    final String _sql = "SELECT * FROM products ORDER BY useCount DESC, lastUsedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"products"}, new Callable<List<ProductEntity>>() {
      @Override
      @NonNull
      public List<ProductEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfHsnCode = CursorUtil.getColumnIndexOrThrow(_cursor, "hsnCode");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfGstRate = CursorUtil.getColumnIndexOrThrow(_cursor, "gstRate");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfUseCount = CursorUtil.getColumnIndexOrThrow(_cursor, "useCount");
          final int _cursorIndexOfLastUsedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductEntity> _result = new ArrayList<ProductEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final BigDecimal _tmpSellingPrice;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSellingPrice)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSellingPrice);
            }
            final BigDecimal _tmp_1 = __converters.toBigDecimal(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpSellingPrice = _tmp_1;
            }
            final String _tmpHsnCode;
            if (_cursor.isNull(_cursorIndexOfHsnCode)) {
              _tmpHsnCode = null;
            } else {
              _tmpHsnCode = _cursor.getString(_cursorIndexOfHsnCode);
            }
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final BigDecimal _tmpGstRate;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfGstRate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfGstRate);
            }
            _tmpGstRate = __converters.toBigDecimal(_tmp_2);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpSku;
            if (_cursor.isNull(_cursorIndexOfSku)) {
              _tmpSku = null;
            } else {
              _tmpSku = _cursor.getString(_cursorIndexOfSku);
            }
            final int _tmpUseCount;
            _tmpUseCount = _cursor.getInt(_cursorIndexOfUseCount);
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
            _item = new ProductEntity(_tmpId,_tmpName,_tmpSellingPrice,_tmpHsnCode,_tmpUnit,_tmpGstRate,_tmpDescription,_tmpSku,_tmpUseCount,_tmpLastUsedAt,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Flow<List<ProductEntity>> search(final String query) {
    final String _sql = "SELECT * FROM products WHERE name LIKE '%' || ? || '%' OR sku LIKE '%' || ? || '%'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"products"}, new Callable<List<ProductEntity>>() {
      @Override
      @NonNull
      public List<ProductEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfHsnCode = CursorUtil.getColumnIndexOrThrow(_cursor, "hsnCode");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfGstRate = CursorUtil.getColumnIndexOrThrow(_cursor, "gstRate");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfUseCount = CursorUtil.getColumnIndexOrThrow(_cursor, "useCount");
          final int _cursorIndexOfLastUsedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<ProductEntity> _result = new ArrayList<ProductEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final BigDecimal _tmpSellingPrice;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSellingPrice)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSellingPrice);
            }
            final BigDecimal _tmp_1 = __converters.toBigDecimal(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpSellingPrice = _tmp_1;
            }
            final String _tmpHsnCode;
            if (_cursor.isNull(_cursorIndexOfHsnCode)) {
              _tmpHsnCode = null;
            } else {
              _tmpHsnCode = _cursor.getString(_cursorIndexOfHsnCode);
            }
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final BigDecimal _tmpGstRate;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfGstRate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfGstRate);
            }
            _tmpGstRate = __converters.toBigDecimal(_tmp_2);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpSku;
            if (_cursor.isNull(_cursorIndexOfSku)) {
              _tmpSku = null;
            } else {
              _tmpSku = _cursor.getString(_cursorIndexOfSku);
            }
            final int _tmpUseCount;
            _tmpUseCount = _cursor.getInt(_cursorIndexOfUseCount);
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
            _item = new ProductEntity(_tmpId,_tmpName,_tmpSellingPrice,_tmpHsnCode,_tmpUnit,_tmpGstRate,_tmpDescription,_tmpSku,_tmpUseCount,_tmpLastUsedAt,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getById(final long id, final Continuation<? super ProductEntity> $completion) {
    final String _sql = "SELECT * FROM products WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProductEntity>() {
      @Override
      @Nullable
      public ProductEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSellingPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "sellingPrice");
          final int _cursorIndexOfHsnCode = CursorUtil.getColumnIndexOrThrow(_cursor, "hsnCode");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfGstRate = CursorUtil.getColumnIndexOrThrow(_cursor, "gstRate");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfSku = CursorUtil.getColumnIndexOrThrow(_cursor, "sku");
          final int _cursorIndexOfUseCount = CursorUtil.getColumnIndexOrThrow(_cursor, "useCount");
          final int _cursorIndexOfLastUsedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUsedAt");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final ProductEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final BigDecimal _tmpSellingPrice;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSellingPrice)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSellingPrice);
            }
            final BigDecimal _tmp_1 = __converters.toBigDecimal(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpSellingPrice = _tmp_1;
            }
            final String _tmpHsnCode;
            if (_cursor.isNull(_cursorIndexOfHsnCode)) {
              _tmpHsnCode = null;
            } else {
              _tmpHsnCode = _cursor.getString(_cursorIndexOfHsnCode);
            }
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final BigDecimal _tmpGstRate;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfGstRate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfGstRate);
            }
            _tmpGstRate = __converters.toBigDecimal(_tmp_2);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpSku;
            if (_cursor.isNull(_cursorIndexOfSku)) {
              _tmpSku = null;
            } else {
              _tmpSku = _cursor.getString(_cursorIndexOfSku);
            }
            final int _tmpUseCount;
            _tmpUseCount = _cursor.getInt(_cursorIndexOfUseCount);
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
            _result = new ProductEntity(_tmpId,_tmpName,_tmpSellingPrice,_tmpHsnCode,_tmpUnit,_tmpGstRate,_tmpDescription,_tmpSku,_tmpUseCount,_tmpLastUsedAt,_tmpCreatedAt,_tmpUpdatedAt);
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
