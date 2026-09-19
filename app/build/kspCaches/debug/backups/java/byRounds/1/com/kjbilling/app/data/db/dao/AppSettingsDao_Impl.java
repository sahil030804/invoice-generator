package com.kjbilling.app.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.kjbilling.app.data.db.converter.Converters;
import com.kjbilling.app.data.db.entity.AppSettingsEntity;
import com.kjbilling.app.domain.model.PaymentStatus;
import com.kjbilling.app.domain.model.TaxType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppSettingsDao_Impl implements AppSettingsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AppSettingsEntity> __insertionAdapterOfAppSettingsEntity;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfUpdateOnboardingCompleted;

  private final SharedSQLiteStatement __preparedStmtOfUpdateGstSettings;

  private final SharedSQLiteStatement __preparedStmtOfUpdateInvoicePrefixAndNumber;

  public AppSettingsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppSettingsEntity = new EntityInsertionAdapter<AppSettingsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `app_settings` (`id`,`gstEnabled`,`defaultGstRate`,`defaultTaxType`,`invoicePrefix`,`nextInvoiceNumber`,`onboardingCompleted`,`defaultPaymentStatus`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppSettingsEntity entity) {
        statement.bindLong(1, entity.getId());
        final int _tmp = entity.getGstEnabled() ? 1 : 0;
        statement.bindLong(2, _tmp);
        final String _tmp_1 = __converters.fromBigDecimal(entity.getDefaultGstRate());
        if (_tmp_1 == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, _tmp_1);
        }
        final String _tmp_2 = __converters.fromTaxType(entity.getDefaultTaxType());
        if (_tmp_2 == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, _tmp_2);
        }
        statement.bindString(5, entity.getInvoicePrefix());
        statement.bindLong(6, entity.getNextInvoiceNumber());
        final int _tmp_3 = entity.getOnboardingCompleted() ? 1 : 0;
        statement.bindLong(7, _tmp_3);
        final String _tmp_4 = __converters.fromPaymentStatus(entity.getDefaultPaymentStatus());
        if (_tmp_4 == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, _tmp_4);
        }
      }
    };
    this.__preparedStmtOfUpdateOnboardingCompleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_settings SET onboardingCompleted = ? WHERE id = 1";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateGstSettings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_settings SET gstEnabled = ?, defaultGstRate = ?, defaultTaxType = ? WHERE id = 1";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateInvoicePrefixAndNumber = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE app_settings SET invoicePrefix = ?, nextInvoiceNumber = ? WHERE id = 1";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final AppSettingsEntity settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAppSettingsEntity.insert(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object generateNextInvoiceNumber(final String prefix,
      final Continuation<? super String> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> AppSettingsDao.DefaultImpls.generateNextInvoiceNumber(AppSettingsDao_Impl.this, prefix, __cont), $completion);
  }

  @Override
  public Object updateOnboardingCompleted(final boolean completed,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateOnboardingCompleted.acquire();
        int _argIndex = 1;
        final int _tmp = completed ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
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
          __preparedStmtOfUpdateOnboardingCompleted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateGstSettings(final boolean enabled, final String rate, final TaxType taxType,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateGstSettings.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, rate);
        _argIndex = 3;
        final String _tmp_1 = __converters.fromTaxType(taxType);
        if (_tmp_1 == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp_1);
        }
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
          __preparedStmtOfUpdateGstSettings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateInvoicePrefixAndNumber(final String prefix, final long nextNumber,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateInvoicePrefixAndNumber.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, prefix);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, nextNumber);
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
          __preparedStmtOfUpdateInvoicePrefixAndNumber.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<AppSettingsEntity> getSettings() {
    final String _sql = "SELECT * FROM app_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_settings"}, new Callable<AppSettingsEntity>() {
      @Override
      @Nullable
      public AppSettingsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGstEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "gstEnabled");
          final int _cursorIndexOfDefaultGstRate = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultGstRate");
          final int _cursorIndexOfDefaultTaxType = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultTaxType");
          final int _cursorIndexOfInvoicePrefix = CursorUtil.getColumnIndexOrThrow(_cursor, "invoicePrefix");
          final int _cursorIndexOfNextInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "nextInvoiceNumber");
          final int _cursorIndexOfOnboardingCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "onboardingCompleted");
          final int _cursorIndexOfDefaultPaymentStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultPaymentStatus");
          final AppSettingsEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final boolean _tmpGstEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfGstEnabled);
            _tmpGstEnabled = _tmp != 0;
            final BigDecimal _tmpDefaultGstRate;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDefaultGstRate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfDefaultGstRate);
            }
            final BigDecimal _tmp_2 = __converters.toBigDecimal(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpDefaultGstRate = _tmp_2;
            }
            final TaxType _tmpDefaultTaxType;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfDefaultTaxType)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfDefaultTaxType);
            }
            final TaxType _tmp_4 = __converters.toTaxType(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.TaxType', but it was NULL.");
            } else {
              _tmpDefaultTaxType = _tmp_4;
            }
            final String _tmpInvoicePrefix;
            _tmpInvoicePrefix = _cursor.getString(_cursorIndexOfInvoicePrefix);
            final long _tmpNextInvoiceNumber;
            _tmpNextInvoiceNumber = _cursor.getLong(_cursorIndexOfNextInvoiceNumber);
            final boolean _tmpOnboardingCompleted;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfOnboardingCompleted);
            _tmpOnboardingCompleted = _tmp_5 != 0;
            final PaymentStatus _tmpDefaultPaymentStatus;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfDefaultPaymentStatus)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfDefaultPaymentStatus);
            }
            final PaymentStatus _tmp_7 = __converters.toPaymentStatus(_tmp_6);
            if (_tmp_7 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.PaymentStatus', but it was NULL.");
            } else {
              _tmpDefaultPaymentStatus = _tmp_7;
            }
            _result = new AppSettingsEntity(_tmpId,_tmpGstEnabled,_tmpDefaultGstRate,_tmpDefaultTaxType,_tmpInvoicePrefix,_tmpNextInvoiceNumber,_tmpOnboardingCompleted,_tmpDefaultPaymentStatus);
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
  public Object getSettingsOnce(final Continuation<? super AppSettingsEntity> $completion) {
    final String _sql = "SELECT * FROM app_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppSettingsEntity>() {
      @Override
      @Nullable
      public AppSettingsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfGstEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "gstEnabled");
          final int _cursorIndexOfDefaultGstRate = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultGstRate");
          final int _cursorIndexOfDefaultTaxType = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultTaxType");
          final int _cursorIndexOfInvoicePrefix = CursorUtil.getColumnIndexOrThrow(_cursor, "invoicePrefix");
          final int _cursorIndexOfNextInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "nextInvoiceNumber");
          final int _cursorIndexOfOnboardingCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "onboardingCompleted");
          final int _cursorIndexOfDefaultPaymentStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultPaymentStatus");
          final AppSettingsEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final boolean _tmpGstEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfGstEnabled);
            _tmpGstEnabled = _tmp != 0;
            final BigDecimal _tmpDefaultGstRate;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDefaultGstRate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfDefaultGstRate);
            }
            final BigDecimal _tmp_2 = __converters.toBigDecimal(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpDefaultGstRate = _tmp_2;
            }
            final TaxType _tmpDefaultTaxType;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfDefaultTaxType)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfDefaultTaxType);
            }
            final TaxType _tmp_4 = __converters.toTaxType(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.TaxType', but it was NULL.");
            } else {
              _tmpDefaultTaxType = _tmp_4;
            }
            final String _tmpInvoicePrefix;
            _tmpInvoicePrefix = _cursor.getString(_cursorIndexOfInvoicePrefix);
            final long _tmpNextInvoiceNumber;
            _tmpNextInvoiceNumber = _cursor.getLong(_cursorIndexOfNextInvoiceNumber);
            final boolean _tmpOnboardingCompleted;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfOnboardingCompleted);
            _tmpOnboardingCompleted = _tmp_5 != 0;
            final PaymentStatus _tmpDefaultPaymentStatus;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfDefaultPaymentStatus)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfDefaultPaymentStatus);
            }
            final PaymentStatus _tmp_7 = __converters.toPaymentStatus(_tmp_6);
            if (_tmp_7 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.PaymentStatus', but it was NULL.");
            } else {
              _tmpDefaultPaymentStatus = _tmp_7;
            }
            _result = new AppSettingsEntity(_tmpId,_tmpGstEnabled,_tmpDefaultGstRate,_tmpDefaultTaxType,_tmpInvoicePrefix,_tmpNextInvoiceNumber,_tmpOnboardingCompleted,_tmpDefaultPaymentStatus);
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
