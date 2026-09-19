package com.kjbilling.app.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.kjbilling.app.data.db.converter.Converters;
import com.kjbilling.app.data.db.entity.InvoiceEntity;
import com.kjbilling.app.data.db.entity.InvoiceItemEntity;
import com.kjbilling.app.domain.model.InvoiceStatus;
import com.kjbilling.app.domain.model.PaymentMethod;
import com.kjbilling.app.domain.model.PaymentStatus;
import com.kjbilling.app.domain.model.TaxType;
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
import kotlin.Pair;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class InvoiceDao_Impl implements InvoiceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<InvoiceEntity> __insertionAdapterOfInvoiceEntity;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<InvoiceItemEntity> __insertionAdapterOfInvoiceItemEntity;

  private final EntityDeletionOrUpdateAdapter<InvoiceEntity> __updateAdapterOfInvoiceEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteItemsByInvoiceId;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePaymentInfo;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDraft;

  public InvoiceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInvoiceEntity = new EntityInsertionAdapter<InvoiceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `invoices` (`id`,`invoiceNumber`,`invoiceDate`,`dueDate`,`customerId`,`customerName`,`customerGstin`,`customerState`,`customerAddress`,`subtotal`,`totalDiscount`,`totalTax`,`grandTotal`,`taxType`,`status`,`paymentStatus`,`paymentMethod`,`amountPaid`,`notes`,`createdAt`,`updatedAt`,`finalizedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InvoiceEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getInvoiceNumber());
        statement.bindLong(3, entity.getInvoiceDate());
        if (entity.getDueDate() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getDueDate());
        }
        if (entity.getCustomerId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getCustomerId());
        }
        statement.bindString(6, entity.getCustomerName());
        if (entity.getCustomerGstin() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCustomerGstin());
        }
        if (entity.getCustomerState() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCustomerState());
        }
        if (entity.getCustomerAddress() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCustomerAddress());
        }
        final String _tmp = __converters.fromBigDecimal(entity.getSubtotal());
        if (_tmp == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, _tmp);
        }
        final String _tmp_1 = __converters.fromBigDecimal(entity.getTotalDiscount());
        if (_tmp_1 == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, _tmp_1);
        }
        final String _tmp_2 = __converters.fromBigDecimal(entity.getTotalTax());
        if (_tmp_2 == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, _tmp_2);
        }
        final String _tmp_3 = __converters.fromBigDecimal(entity.getGrandTotal());
        if (_tmp_3 == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, _tmp_3);
        }
        final String _tmp_4 = __converters.fromTaxType(entity.getTaxType());
        if (_tmp_4 == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, _tmp_4);
        }
        final String _tmp_5 = __converters.fromInvoiceStatus(entity.getStatus());
        if (_tmp_5 == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, _tmp_5);
        }
        final String _tmp_6 = __converters.fromPaymentStatus(entity.getPaymentStatus());
        if (_tmp_6 == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, _tmp_6);
        }
        final String _tmp_7 = __converters.fromPaymentMethod(entity.getPaymentMethod());
        if (_tmp_7 == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, _tmp_7);
        }
        final String _tmp_8 = __converters.fromBigDecimal(entity.getAmountPaid());
        if (_tmp_8 == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, _tmp_8);
        }
        if (entity.getNotes() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getNotes());
        }
        statement.bindLong(20, entity.getCreatedAt());
        statement.bindLong(21, entity.getUpdatedAt());
        if (entity.getFinalizedAt() == null) {
          statement.bindNull(22);
        } else {
          statement.bindLong(22, entity.getFinalizedAt());
        }
      }
    };
    this.__insertionAdapterOfInvoiceItemEntity = new EntityInsertionAdapter<InvoiceItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `invoice_items` (`id`,`invoiceId`,`productId`,`itemName`,`description`,`hsnCode`,`quantity`,`unit`,`unitPrice`,`discountPercent`,`discountAmount`,`gstRate`,`taxableAmount`,`cgstAmount`,`sgstAmount`,`igstAmount`,`taxAmount`,`total`,`sortOrder`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InvoiceItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getInvoiceId());
        if (entity.getProductId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getProductId());
        }
        statement.bindString(4, entity.getItemName());
        if (entity.getDescription() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getDescription());
        }
        if (entity.getHsnCode() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getHsnCode());
        }
        final String _tmp = __converters.fromBigDecimal(entity.getQuantity());
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp);
        }
        if (entity.getUnit() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getUnit());
        }
        final String _tmp_1 = __converters.fromBigDecimal(entity.getUnitPrice());
        if (_tmp_1 == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp_1);
        }
        final String _tmp_2 = __converters.fromBigDecimal(entity.getDiscountPercent());
        if (_tmp_2 == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, _tmp_2);
        }
        final String _tmp_3 = __converters.fromBigDecimal(entity.getDiscountAmount());
        if (_tmp_3 == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, _tmp_3);
        }
        final String _tmp_4 = __converters.fromBigDecimal(entity.getGstRate());
        if (_tmp_4 == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, _tmp_4);
        }
        final String _tmp_5 = __converters.fromBigDecimal(entity.getTaxableAmount());
        if (_tmp_5 == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, _tmp_5);
        }
        final String _tmp_6 = __converters.fromBigDecimal(entity.getCgstAmount());
        if (_tmp_6 == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, _tmp_6);
        }
        final String _tmp_7 = __converters.fromBigDecimal(entity.getSgstAmount());
        if (_tmp_7 == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, _tmp_7);
        }
        final String _tmp_8 = __converters.fromBigDecimal(entity.getIgstAmount());
        if (_tmp_8 == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, _tmp_8);
        }
        final String _tmp_9 = __converters.fromBigDecimal(entity.getTaxAmount());
        if (_tmp_9 == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, _tmp_9);
        }
        final String _tmp_10 = __converters.fromBigDecimal(entity.getTotal());
        if (_tmp_10 == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, _tmp_10);
        }
        statement.bindLong(19, entity.getSortOrder());
      }
    };
    this.__updateAdapterOfInvoiceEntity = new EntityDeletionOrUpdateAdapter<InvoiceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `invoices` SET `id` = ?,`invoiceNumber` = ?,`invoiceDate` = ?,`dueDate` = ?,`customerId` = ?,`customerName` = ?,`customerGstin` = ?,`customerState` = ?,`customerAddress` = ?,`subtotal` = ?,`totalDiscount` = ?,`totalTax` = ?,`grandTotal` = ?,`taxType` = ?,`status` = ?,`paymentStatus` = ?,`paymentMethod` = ?,`amountPaid` = ?,`notes` = ?,`createdAt` = ?,`updatedAt` = ?,`finalizedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InvoiceEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getInvoiceNumber());
        statement.bindLong(3, entity.getInvoiceDate());
        if (entity.getDueDate() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getDueDate());
        }
        if (entity.getCustomerId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getCustomerId());
        }
        statement.bindString(6, entity.getCustomerName());
        if (entity.getCustomerGstin() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getCustomerGstin());
        }
        if (entity.getCustomerState() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCustomerState());
        }
        if (entity.getCustomerAddress() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCustomerAddress());
        }
        final String _tmp = __converters.fromBigDecimal(entity.getSubtotal());
        if (_tmp == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, _tmp);
        }
        final String _tmp_1 = __converters.fromBigDecimal(entity.getTotalDiscount());
        if (_tmp_1 == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, _tmp_1);
        }
        final String _tmp_2 = __converters.fromBigDecimal(entity.getTotalTax());
        if (_tmp_2 == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, _tmp_2);
        }
        final String _tmp_3 = __converters.fromBigDecimal(entity.getGrandTotal());
        if (_tmp_3 == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, _tmp_3);
        }
        final String _tmp_4 = __converters.fromTaxType(entity.getTaxType());
        if (_tmp_4 == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, _tmp_4);
        }
        final String _tmp_5 = __converters.fromInvoiceStatus(entity.getStatus());
        if (_tmp_5 == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, _tmp_5);
        }
        final String _tmp_6 = __converters.fromPaymentStatus(entity.getPaymentStatus());
        if (_tmp_6 == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, _tmp_6);
        }
        final String _tmp_7 = __converters.fromPaymentMethod(entity.getPaymentMethod());
        if (_tmp_7 == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, _tmp_7);
        }
        final String _tmp_8 = __converters.fromBigDecimal(entity.getAmountPaid());
        if (_tmp_8 == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, _tmp_8);
        }
        if (entity.getNotes() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getNotes());
        }
        statement.bindLong(20, entity.getCreatedAt());
        statement.bindLong(21, entity.getUpdatedAt());
        if (entity.getFinalizedAt() == null) {
          statement.bindNull(22);
        } else {
          statement.bindLong(22, entity.getFinalizedAt());
        }
        statement.bindLong(23, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteItemsByInvoiceId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM invoice_items WHERE invoiceId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE invoices SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePaymentInfo = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE invoices SET paymentStatus = ?, paymentMethod = ?, amountPaid = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDraft = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM invoices WHERE id = ? AND status = 'DRAFT'";
        return _query;
      }
    };
  }

  @Override
  public Object insertInvoice(final InvoiceEntity invoice,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfInvoiceEntity.insertAndReturnId(invoice);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertItems(final List<InvoiceItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfInvoiceItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateInvoice(final InvoiceEntity invoice,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfInvoiceEntity.handle(invoice);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertInvoiceWithItems(final InvoiceEntity invoice,
      final List<InvoiceItemEntity> items, final Continuation<? super Long> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> InvoiceDao.DefaultImpls.insertInvoiceWithItems(InvoiceDao_Impl.this, invoice, items, __cont), $completion);
  }

  @Override
  public Object getInvoiceWithItems(final long id,
      final Continuation<? super Pair<InvoiceEntity, ? extends List<InvoiceItemEntity>>> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> InvoiceDao.DefaultImpls.getInvoiceWithItems(InvoiceDao_Impl.this, id, __cont), $completion);
  }

  @Override
  public Object updateInvoiceWithItems(final InvoiceEntity invoice,
      final List<InvoiceItemEntity> items, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> InvoiceDao.DefaultImpls.updateInvoiceWithItems(InvoiceDao_Impl.this, invoice, items, __cont), $completion);
  }

  @Override
  public Object deleteItemsByInvoiceId(final long invoiceId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteItemsByInvoiceId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, invoiceId);
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
          __preparedStmtOfDeleteItemsByInvoiceId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatus(final long id, final InvoiceStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromInvoiceStatus(status);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp);
        }
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
          __preparedStmtOfUpdateStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePaymentInfo(final long id, final PaymentStatus paymentStatus,
      final PaymentMethod paymentMethod, final String amountPaid,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePaymentInfo.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromPaymentStatus(paymentStatus);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp);
        }
        _argIndex = 2;
        final String _tmp_1 = __converters.fromPaymentMethod(paymentMethod);
        if (_tmp_1 == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp_1);
        }
        _argIndex = 3;
        _stmt.bindString(_argIndex, amountPaid);
        _argIndex = 4;
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
          __preparedStmtOfUpdatePaymentInfo.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDraft(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDraft.acquire();
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
          __preparedStmtOfDeleteDraft.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<InvoiceEntity>> getAll() {
    final String _sql = "SELECT * FROM invoices ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"invoices"}, new Callable<List<InvoiceEntity>>() {
      @Override
      @NonNull
      public List<InvoiceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceNumber");
          final int _cursorIndexOfInvoiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceDate");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfCustomerName = CursorUtil.getColumnIndexOrThrow(_cursor, "customerName");
          final int _cursorIndexOfCustomerGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "customerGstin");
          final int _cursorIndexOfCustomerState = CursorUtil.getColumnIndexOrThrow(_cursor, "customerState");
          final int _cursorIndexOfCustomerAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "customerAddress");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTotalDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDiscount");
          final int _cursorIndexOfTotalTax = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTax");
          final int _cursorIndexOfGrandTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "grandTotal");
          final int _cursorIndexOfTaxType = CursorUtil.getColumnIndexOrThrow(_cursor, "taxType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPaymentStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStatus");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfAmountPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaid");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfFinalizedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "finalizedAt");
          final List<InvoiceEntity> _result = new ArrayList<InvoiceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InvoiceEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpInvoiceNumber;
            _tmpInvoiceNumber = _cursor.getString(_cursorIndexOfInvoiceNumber);
            final long _tmpInvoiceDate;
            _tmpInvoiceDate = _cursor.getLong(_cursorIndexOfInvoiceDate);
            final Long _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate);
            }
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final String _tmpCustomerName;
            _tmpCustomerName = _cursor.getString(_cursorIndexOfCustomerName);
            final String _tmpCustomerGstin;
            if (_cursor.isNull(_cursorIndexOfCustomerGstin)) {
              _tmpCustomerGstin = null;
            } else {
              _tmpCustomerGstin = _cursor.getString(_cursorIndexOfCustomerGstin);
            }
            final String _tmpCustomerState;
            if (_cursor.isNull(_cursorIndexOfCustomerState)) {
              _tmpCustomerState = null;
            } else {
              _tmpCustomerState = _cursor.getString(_cursorIndexOfCustomerState);
            }
            final String _tmpCustomerAddress;
            if (_cursor.isNull(_cursorIndexOfCustomerAddress)) {
              _tmpCustomerAddress = null;
            } else {
              _tmpCustomerAddress = _cursor.getString(_cursorIndexOfCustomerAddress);
            }
            final BigDecimal _tmpSubtotal;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSubtotal)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSubtotal);
            }
            final BigDecimal _tmp_1 = __converters.toBigDecimal(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpSubtotal = _tmp_1;
            }
            final BigDecimal _tmpTotalDiscount;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfTotalDiscount)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfTotalDiscount);
            }
            final BigDecimal _tmp_3 = __converters.toBigDecimal(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalDiscount = _tmp_3;
            }
            final BigDecimal _tmpTotalTax;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfTotalTax)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfTotalTax);
            }
            final BigDecimal _tmp_5 = __converters.toBigDecimal(_tmp_4);
            if (_tmp_5 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalTax = _tmp_5;
            }
            final BigDecimal _tmpGrandTotal;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfGrandTotal)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfGrandTotal);
            }
            final BigDecimal _tmp_7 = __converters.toBigDecimal(_tmp_6);
            if (_tmp_7 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpGrandTotal = _tmp_7;
            }
            final TaxType _tmpTaxType;
            final String _tmp_8;
            if (_cursor.isNull(_cursorIndexOfTaxType)) {
              _tmp_8 = null;
            } else {
              _tmp_8 = _cursor.getString(_cursorIndexOfTaxType);
            }
            final TaxType _tmp_9 = __converters.toTaxType(_tmp_8);
            if (_tmp_9 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.TaxType', but it was NULL.");
            } else {
              _tmpTaxType = _tmp_9;
            }
            final InvoiceStatus _tmpStatus;
            final String _tmp_10;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_10 = null;
            } else {
              _tmp_10 = _cursor.getString(_cursorIndexOfStatus);
            }
            final InvoiceStatus _tmp_11 = __converters.toInvoiceStatus(_tmp_10);
            if (_tmp_11 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.InvoiceStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_11;
            }
            final PaymentStatus _tmpPaymentStatus;
            final String _tmp_12;
            if (_cursor.isNull(_cursorIndexOfPaymentStatus)) {
              _tmp_12 = null;
            } else {
              _tmp_12 = _cursor.getString(_cursorIndexOfPaymentStatus);
            }
            final PaymentStatus _tmp_13 = __converters.toPaymentStatus(_tmp_12);
            if (_tmp_13 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.PaymentStatus', but it was NULL.");
            } else {
              _tmpPaymentStatus = _tmp_13;
            }
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp_14;
            if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
              _tmp_14 = null;
            } else {
              _tmp_14 = _cursor.getString(_cursorIndexOfPaymentMethod);
            }
            _tmpPaymentMethod = __converters.toPaymentMethod(_tmp_14);
            final BigDecimal _tmpAmountPaid;
            final String _tmp_15;
            if (_cursor.isNull(_cursorIndexOfAmountPaid)) {
              _tmp_15 = null;
            } else {
              _tmp_15 = _cursor.getString(_cursorIndexOfAmountPaid);
            }
            final BigDecimal _tmp_16 = __converters.toBigDecimal(_tmp_15);
            if (_tmp_16 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpAmountPaid = _tmp_16;
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpFinalizedAt;
            if (_cursor.isNull(_cursorIndexOfFinalizedAt)) {
              _tmpFinalizedAt = null;
            } else {
              _tmpFinalizedAt = _cursor.getLong(_cursorIndexOfFinalizedAt);
            }
            _item = new InvoiceEntity(_tmpId,_tmpInvoiceNumber,_tmpInvoiceDate,_tmpDueDate,_tmpCustomerId,_tmpCustomerName,_tmpCustomerGstin,_tmpCustomerState,_tmpCustomerAddress,_tmpSubtotal,_tmpTotalDiscount,_tmpTotalTax,_tmpGrandTotal,_tmpTaxType,_tmpStatus,_tmpPaymentStatus,_tmpPaymentMethod,_tmpAmountPaid,_tmpNotes,_tmpCreatedAt,_tmpUpdatedAt,_tmpFinalizedAt);
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
  public Object getById(final long id, final Continuation<? super InvoiceEntity> $completion) {
    final String _sql = "SELECT * FROM invoices WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<InvoiceEntity>() {
      @Override
      @Nullable
      public InvoiceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceNumber");
          final int _cursorIndexOfInvoiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceDate");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfCustomerName = CursorUtil.getColumnIndexOrThrow(_cursor, "customerName");
          final int _cursorIndexOfCustomerGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "customerGstin");
          final int _cursorIndexOfCustomerState = CursorUtil.getColumnIndexOrThrow(_cursor, "customerState");
          final int _cursorIndexOfCustomerAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "customerAddress");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTotalDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDiscount");
          final int _cursorIndexOfTotalTax = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTax");
          final int _cursorIndexOfGrandTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "grandTotal");
          final int _cursorIndexOfTaxType = CursorUtil.getColumnIndexOrThrow(_cursor, "taxType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPaymentStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStatus");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfAmountPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaid");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfFinalizedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "finalizedAt");
          final InvoiceEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpInvoiceNumber;
            _tmpInvoiceNumber = _cursor.getString(_cursorIndexOfInvoiceNumber);
            final long _tmpInvoiceDate;
            _tmpInvoiceDate = _cursor.getLong(_cursorIndexOfInvoiceDate);
            final Long _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate);
            }
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final String _tmpCustomerName;
            _tmpCustomerName = _cursor.getString(_cursorIndexOfCustomerName);
            final String _tmpCustomerGstin;
            if (_cursor.isNull(_cursorIndexOfCustomerGstin)) {
              _tmpCustomerGstin = null;
            } else {
              _tmpCustomerGstin = _cursor.getString(_cursorIndexOfCustomerGstin);
            }
            final String _tmpCustomerState;
            if (_cursor.isNull(_cursorIndexOfCustomerState)) {
              _tmpCustomerState = null;
            } else {
              _tmpCustomerState = _cursor.getString(_cursorIndexOfCustomerState);
            }
            final String _tmpCustomerAddress;
            if (_cursor.isNull(_cursorIndexOfCustomerAddress)) {
              _tmpCustomerAddress = null;
            } else {
              _tmpCustomerAddress = _cursor.getString(_cursorIndexOfCustomerAddress);
            }
            final BigDecimal _tmpSubtotal;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSubtotal)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSubtotal);
            }
            final BigDecimal _tmp_1 = __converters.toBigDecimal(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpSubtotal = _tmp_1;
            }
            final BigDecimal _tmpTotalDiscount;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfTotalDiscount)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfTotalDiscount);
            }
            final BigDecimal _tmp_3 = __converters.toBigDecimal(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalDiscount = _tmp_3;
            }
            final BigDecimal _tmpTotalTax;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfTotalTax)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfTotalTax);
            }
            final BigDecimal _tmp_5 = __converters.toBigDecimal(_tmp_4);
            if (_tmp_5 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalTax = _tmp_5;
            }
            final BigDecimal _tmpGrandTotal;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfGrandTotal)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfGrandTotal);
            }
            final BigDecimal _tmp_7 = __converters.toBigDecimal(_tmp_6);
            if (_tmp_7 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpGrandTotal = _tmp_7;
            }
            final TaxType _tmpTaxType;
            final String _tmp_8;
            if (_cursor.isNull(_cursorIndexOfTaxType)) {
              _tmp_8 = null;
            } else {
              _tmp_8 = _cursor.getString(_cursorIndexOfTaxType);
            }
            final TaxType _tmp_9 = __converters.toTaxType(_tmp_8);
            if (_tmp_9 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.TaxType', but it was NULL.");
            } else {
              _tmpTaxType = _tmp_9;
            }
            final InvoiceStatus _tmpStatus;
            final String _tmp_10;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_10 = null;
            } else {
              _tmp_10 = _cursor.getString(_cursorIndexOfStatus);
            }
            final InvoiceStatus _tmp_11 = __converters.toInvoiceStatus(_tmp_10);
            if (_tmp_11 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.InvoiceStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_11;
            }
            final PaymentStatus _tmpPaymentStatus;
            final String _tmp_12;
            if (_cursor.isNull(_cursorIndexOfPaymentStatus)) {
              _tmp_12 = null;
            } else {
              _tmp_12 = _cursor.getString(_cursorIndexOfPaymentStatus);
            }
            final PaymentStatus _tmp_13 = __converters.toPaymentStatus(_tmp_12);
            if (_tmp_13 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.PaymentStatus', but it was NULL.");
            } else {
              _tmpPaymentStatus = _tmp_13;
            }
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp_14;
            if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
              _tmp_14 = null;
            } else {
              _tmp_14 = _cursor.getString(_cursorIndexOfPaymentMethod);
            }
            _tmpPaymentMethod = __converters.toPaymentMethod(_tmp_14);
            final BigDecimal _tmpAmountPaid;
            final String _tmp_15;
            if (_cursor.isNull(_cursorIndexOfAmountPaid)) {
              _tmp_15 = null;
            } else {
              _tmp_15 = _cursor.getString(_cursorIndexOfAmountPaid);
            }
            final BigDecimal _tmp_16 = __converters.toBigDecimal(_tmp_15);
            if (_tmp_16 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpAmountPaid = _tmp_16;
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpFinalizedAt;
            if (_cursor.isNull(_cursorIndexOfFinalizedAt)) {
              _tmpFinalizedAt = null;
            } else {
              _tmpFinalizedAt = _cursor.getLong(_cursorIndexOfFinalizedAt);
            }
            _result = new InvoiceEntity(_tmpId,_tmpInvoiceNumber,_tmpInvoiceDate,_tmpDueDate,_tmpCustomerId,_tmpCustomerName,_tmpCustomerGstin,_tmpCustomerState,_tmpCustomerAddress,_tmpSubtotal,_tmpTotalDiscount,_tmpTotalTax,_tmpGrandTotal,_tmpTaxType,_tmpStatus,_tmpPaymentStatus,_tmpPaymentMethod,_tmpAmountPaid,_tmpNotes,_tmpCreatedAt,_tmpUpdatedAt,_tmpFinalizedAt);
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

  @Override
  public Object getItemsByInvoiceId(final long invoiceId,
      final Continuation<? super List<InvoiceItemEntity>> $completion) {
    final String _sql = "SELECT * FROM invoice_items WHERE invoiceId = ? ORDER BY sortOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, invoiceId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<InvoiceItemEntity>>() {
      @Override
      @NonNull
      public List<InvoiceItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfInvoiceId = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfItemName = CursorUtil.getColumnIndexOrThrow(_cursor, "itemName");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfHsnCode = CursorUtil.getColumnIndexOrThrow(_cursor, "hsnCode");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfUnitPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "unitPrice");
          final int _cursorIndexOfDiscountPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "discountPercent");
          final int _cursorIndexOfDiscountAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "discountAmount");
          final int _cursorIndexOfGstRate = CursorUtil.getColumnIndexOrThrow(_cursor, "gstRate");
          final int _cursorIndexOfTaxableAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "taxableAmount");
          final int _cursorIndexOfCgstAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cgstAmount");
          final int _cursorIndexOfSgstAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "sgstAmount");
          final int _cursorIndexOfIgstAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "igstAmount");
          final int _cursorIndexOfTaxAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "taxAmount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final List<InvoiceItemEntity> _result = new ArrayList<InvoiceItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InvoiceItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpInvoiceId;
            _tmpInvoiceId = _cursor.getLong(_cursorIndexOfInvoiceId);
            final Long _tmpProductId;
            if (_cursor.isNull(_cursorIndexOfProductId)) {
              _tmpProductId = null;
            } else {
              _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            }
            final String _tmpItemName;
            _tmpItemName = _cursor.getString(_cursorIndexOfItemName);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpHsnCode;
            if (_cursor.isNull(_cursorIndexOfHsnCode)) {
              _tmpHsnCode = null;
            } else {
              _tmpHsnCode = _cursor.getString(_cursorIndexOfHsnCode);
            }
            final BigDecimal _tmpQuantity;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfQuantity)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfQuantity);
            }
            final BigDecimal _tmp_1 = __converters.toBigDecimal(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpQuantity = _tmp_1;
            }
            final String _tmpUnit;
            if (_cursor.isNull(_cursorIndexOfUnit)) {
              _tmpUnit = null;
            } else {
              _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            }
            final BigDecimal _tmpUnitPrice;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfUnitPrice)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfUnitPrice);
            }
            final BigDecimal _tmp_3 = __converters.toBigDecimal(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpUnitPrice = _tmp_3;
            }
            final BigDecimal _tmpDiscountPercent;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfDiscountPercent)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfDiscountPercent);
            }
            final BigDecimal _tmp_5 = __converters.toBigDecimal(_tmp_4);
            if (_tmp_5 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpDiscountPercent = _tmp_5;
            }
            final BigDecimal _tmpDiscountAmount;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfDiscountAmount)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfDiscountAmount);
            }
            final BigDecimal _tmp_7 = __converters.toBigDecimal(_tmp_6);
            if (_tmp_7 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpDiscountAmount = _tmp_7;
            }
            final BigDecimal _tmpGstRate;
            final String _tmp_8;
            if (_cursor.isNull(_cursorIndexOfGstRate)) {
              _tmp_8 = null;
            } else {
              _tmp_8 = _cursor.getString(_cursorIndexOfGstRate);
            }
            final BigDecimal _tmp_9 = __converters.toBigDecimal(_tmp_8);
            if (_tmp_9 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpGstRate = _tmp_9;
            }
            final BigDecimal _tmpTaxableAmount;
            final String _tmp_10;
            if (_cursor.isNull(_cursorIndexOfTaxableAmount)) {
              _tmp_10 = null;
            } else {
              _tmp_10 = _cursor.getString(_cursorIndexOfTaxableAmount);
            }
            final BigDecimal _tmp_11 = __converters.toBigDecimal(_tmp_10);
            if (_tmp_11 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTaxableAmount = _tmp_11;
            }
            final BigDecimal _tmpCgstAmount;
            final String _tmp_12;
            if (_cursor.isNull(_cursorIndexOfCgstAmount)) {
              _tmp_12 = null;
            } else {
              _tmp_12 = _cursor.getString(_cursorIndexOfCgstAmount);
            }
            _tmpCgstAmount = __converters.toBigDecimal(_tmp_12);
            final BigDecimal _tmpSgstAmount;
            final String _tmp_13;
            if (_cursor.isNull(_cursorIndexOfSgstAmount)) {
              _tmp_13 = null;
            } else {
              _tmp_13 = _cursor.getString(_cursorIndexOfSgstAmount);
            }
            _tmpSgstAmount = __converters.toBigDecimal(_tmp_13);
            final BigDecimal _tmpIgstAmount;
            final String _tmp_14;
            if (_cursor.isNull(_cursorIndexOfIgstAmount)) {
              _tmp_14 = null;
            } else {
              _tmp_14 = _cursor.getString(_cursorIndexOfIgstAmount);
            }
            _tmpIgstAmount = __converters.toBigDecimal(_tmp_14);
            final BigDecimal _tmpTaxAmount;
            final String _tmp_15;
            if (_cursor.isNull(_cursorIndexOfTaxAmount)) {
              _tmp_15 = null;
            } else {
              _tmp_15 = _cursor.getString(_cursorIndexOfTaxAmount);
            }
            final BigDecimal _tmp_16 = __converters.toBigDecimal(_tmp_15);
            if (_tmp_16 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTaxAmount = _tmp_16;
            }
            final BigDecimal _tmpTotal;
            final String _tmp_17;
            if (_cursor.isNull(_cursorIndexOfTotal)) {
              _tmp_17 = null;
            } else {
              _tmp_17 = _cursor.getString(_cursorIndexOfTotal);
            }
            final BigDecimal _tmp_18 = __converters.toBigDecimal(_tmp_17);
            if (_tmp_18 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotal = _tmp_18;
            }
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _item = new InvoiceItemEntity(_tmpId,_tmpInvoiceId,_tmpProductId,_tmpItemName,_tmpDescription,_tmpHsnCode,_tmpQuantity,_tmpUnit,_tmpUnitPrice,_tmpDiscountPercent,_tmpDiscountAmount,_tmpGstRate,_tmpTaxableAmount,_tmpCgstAmount,_tmpSgstAmount,_tmpIgstAmount,_tmpTaxAmount,_tmpTotal,_tmpSortOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<InvoiceEntity>> search(final String query) {
    final String _sql = "SELECT * FROM invoices WHERE invoiceNumber LIKE '%' || ? || '%' OR customerName LIKE '%' || ? || '%' ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"invoices"}, new Callable<List<InvoiceEntity>>() {
      @Override
      @NonNull
      public List<InvoiceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceNumber");
          final int _cursorIndexOfInvoiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceDate");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfCustomerName = CursorUtil.getColumnIndexOrThrow(_cursor, "customerName");
          final int _cursorIndexOfCustomerGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "customerGstin");
          final int _cursorIndexOfCustomerState = CursorUtil.getColumnIndexOrThrow(_cursor, "customerState");
          final int _cursorIndexOfCustomerAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "customerAddress");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTotalDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDiscount");
          final int _cursorIndexOfTotalTax = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTax");
          final int _cursorIndexOfGrandTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "grandTotal");
          final int _cursorIndexOfTaxType = CursorUtil.getColumnIndexOrThrow(_cursor, "taxType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPaymentStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStatus");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfAmountPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaid");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfFinalizedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "finalizedAt");
          final List<InvoiceEntity> _result = new ArrayList<InvoiceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InvoiceEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpInvoiceNumber;
            _tmpInvoiceNumber = _cursor.getString(_cursorIndexOfInvoiceNumber);
            final long _tmpInvoiceDate;
            _tmpInvoiceDate = _cursor.getLong(_cursorIndexOfInvoiceDate);
            final Long _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate);
            }
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final String _tmpCustomerName;
            _tmpCustomerName = _cursor.getString(_cursorIndexOfCustomerName);
            final String _tmpCustomerGstin;
            if (_cursor.isNull(_cursorIndexOfCustomerGstin)) {
              _tmpCustomerGstin = null;
            } else {
              _tmpCustomerGstin = _cursor.getString(_cursorIndexOfCustomerGstin);
            }
            final String _tmpCustomerState;
            if (_cursor.isNull(_cursorIndexOfCustomerState)) {
              _tmpCustomerState = null;
            } else {
              _tmpCustomerState = _cursor.getString(_cursorIndexOfCustomerState);
            }
            final String _tmpCustomerAddress;
            if (_cursor.isNull(_cursorIndexOfCustomerAddress)) {
              _tmpCustomerAddress = null;
            } else {
              _tmpCustomerAddress = _cursor.getString(_cursorIndexOfCustomerAddress);
            }
            final BigDecimal _tmpSubtotal;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSubtotal)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSubtotal);
            }
            final BigDecimal _tmp_1 = __converters.toBigDecimal(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpSubtotal = _tmp_1;
            }
            final BigDecimal _tmpTotalDiscount;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfTotalDiscount)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfTotalDiscount);
            }
            final BigDecimal _tmp_3 = __converters.toBigDecimal(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalDiscount = _tmp_3;
            }
            final BigDecimal _tmpTotalTax;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfTotalTax)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfTotalTax);
            }
            final BigDecimal _tmp_5 = __converters.toBigDecimal(_tmp_4);
            if (_tmp_5 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalTax = _tmp_5;
            }
            final BigDecimal _tmpGrandTotal;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfGrandTotal)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfGrandTotal);
            }
            final BigDecimal _tmp_7 = __converters.toBigDecimal(_tmp_6);
            if (_tmp_7 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpGrandTotal = _tmp_7;
            }
            final TaxType _tmpTaxType;
            final String _tmp_8;
            if (_cursor.isNull(_cursorIndexOfTaxType)) {
              _tmp_8 = null;
            } else {
              _tmp_8 = _cursor.getString(_cursorIndexOfTaxType);
            }
            final TaxType _tmp_9 = __converters.toTaxType(_tmp_8);
            if (_tmp_9 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.TaxType', but it was NULL.");
            } else {
              _tmpTaxType = _tmp_9;
            }
            final InvoiceStatus _tmpStatus;
            final String _tmp_10;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_10 = null;
            } else {
              _tmp_10 = _cursor.getString(_cursorIndexOfStatus);
            }
            final InvoiceStatus _tmp_11 = __converters.toInvoiceStatus(_tmp_10);
            if (_tmp_11 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.InvoiceStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_11;
            }
            final PaymentStatus _tmpPaymentStatus;
            final String _tmp_12;
            if (_cursor.isNull(_cursorIndexOfPaymentStatus)) {
              _tmp_12 = null;
            } else {
              _tmp_12 = _cursor.getString(_cursorIndexOfPaymentStatus);
            }
            final PaymentStatus _tmp_13 = __converters.toPaymentStatus(_tmp_12);
            if (_tmp_13 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.PaymentStatus', but it was NULL.");
            } else {
              _tmpPaymentStatus = _tmp_13;
            }
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp_14;
            if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
              _tmp_14 = null;
            } else {
              _tmp_14 = _cursor.getString(_cursorIndexOfPaymentMethod);
            }
            _tmpPaymentMethod = __converters.toPaymentMethod(_tmp_14);
            final BigDecimal _tmpAmountPaid;
            final String _tmp_15;
            if (_cursor.isNull(_cursorIndexOfAmountPaid)) {
              _tmp_15 = null;
            } else {
              _tmp_15 = _cursor.getString(_cursorIndexOfAmountPaid);
            }
            final BigDecimal _tmp_16 = __converters.toBigDecimal(_tmp_15);
            if (_tmp_16 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpAmountPaid = _tmp_16;
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpFinalizedAt;
            if (_cursor.isNull(_cursorIndexOfFinalizedAt)) {
              _tmpFinalizedAt = null;
            } else {
              _tmpFinalizedAt = _cursor.getLong(_cursorIndexOfFinalizedAt);
            }
            _item = new InvoiceEntity(_tmpId,_tmpInvoiceNumber,_tmpInvoiceDate,_tmpDueDate,_tmpCustomerId,_tmpCustomerName,_tmpCustomerGstin,_tmpCustomerState,_tmpCustomerAddress,_tmpSubtotal,_tmpTotalDiscount,_tmpTotalTax,_tmpGrandTotal,_tmpTaxType,_tmpStatus,_tmpPaymentStatus,_tmpPaymentMethod,_tmpAmountPaid,_tmpNotes,_tmpCreatedAt,_tmpUpdatedAt,_tmpFinalizedAt);
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
  public Flow<List<InvoiceEntity>> filterByStatus(final InvoiceStatus status) {
    final String _sql = "SELECT * FROM invoices WHERE status = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromInvoiceStatus(status);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"invoices"}, new Callable<List<InvoiceEntity>>() {
      @Override
      @NonNull
      public List<InvoiceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceNumber");
          final int _cursorIndexOfInvoiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceDate");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfCustomerName = CursorUtil.getColumnIndexOrThrow(_cursor, "customerName");
          final int _cursorIndexOfCustomerGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "customerGstin");
          final int _cursorIndexOfCustomerState = CursorUtil.getColumnIndexOrThrow(_cursor, "customerState");
          final int _cursorIndexOfCustomerAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "customerAddress");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTotalDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDiscount");
          final int _cursorIndexOfTotalTax = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTax");
          final int _cursorIndexOfGrandTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "grandTotal");
          final int _cursorIndexOfTaxType = CursorUtil.getColumnIndexOrThrow(_cursor, "taxType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPaymentStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStatus");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfAmountPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaid");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfFinalizedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "finalizedAt");
          final List<InvoiceEntity> _result = new ArrayList<InvoiceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InvoiceEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpInvoiceNumber;
            _tmpInvoiceNumber = _cursor.getString(_cursorIndexOfInvoiceNumber);
            final long _tmpInvoiceDate;
            _tmpInvoiceDate = _cursor.getLong(_cursorIndexOfInvoiceDate);
            final Long _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate);
            }
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final String _tmpCustomerName;
            _tmpCustomerName = _cursor.getString(_cursorIndexOfCustomerName);
            final String _tmpCustomerGstin;
            if (_cursor.isNull(_cursorIndexOfCustomerGstin)) {
              _tmpCustomerGstin = null;
            } else {
              _tmpCustomerGstin = _cursor.getString(_cursorIndexOfCustomerGstin);
            }
            final String _tmpCustomerState;
            if (_cursor.isNull(_cursorIndexOfCustomerState)) {
              _tmpCustomerState = null;
            } else {
              _tmpCustomerState = _cursor.getString(_cursorIndexOfCustomerState);
            }
            final String _tmpCustomerAddress;
            if (_cursor.isNull(_cursorIndexOfCustomerAddress)) {
              _tmpCustomerAddress = null;
            } else {
              _tmpCustomerAddress = _cursor.getString(_cursorIndexOfCustomerAddress);
            }
            final BigDecimal _tmpSubtotal;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfSubtotal)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfSubtotal);
            }
            final BigDecimal _tmp_2 = __converters.toBigDecimal(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpSubtotal = _tmp_2;
            }
            final BigDecimal _tmpTotalDiscount;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfTotalDiscount)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfTotalDiscount);
            }
            final BigDecimal _tmp_4 = __converters.toBigDecimal(_tmp_3);
            if (_tmp_4 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalDiscount = _tmp_4;
            }
            final BigDecimal _tmpTotalTax;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfTotalTax)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfTotalTax);
            }
            final BigDecimal _tmp_6 = __converters.toBigDecimal(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalTax = _tmp_6;
            }
            final BigDecimal _tmpGrandTotal;
            final String _tmp_7;
            if (_cursor.isNull(_cursorIndexOfGrandTotal)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getString(_cursorIndexOfGrandTotal);
            }
            final BigDecimal _tmp_8 = __converters.toBigDecimal(_tmp_7);
            if (_tmp_8 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpGrandTotal = _tmp_8;
            }
            final TaxType _tmpTaxType;
            final String _tmp_9;
            if (_cursor.isNull(_cursorIndexOfTaxType)) {
              _tmp_9 = null;
            } else {
              _tmp_9 = _cursor.getString(_cursorIndexOfTaxType);
            }
            final TaxType _tmp_10 = __converters.toTaxType(_tmp_9);
            if (_tmp_10 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.TaxType', but it was NULL.");
            } else {
              _tmpTaxType = _tmp_10;
            }
            final InvoiceStatus _tmpStatus;
            final String _tmp_11;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_11 = null;
            } else {
              _tmp_11 = _cursor.getString(_cursorIndexOfStatus);
            }
            final InvoiceStatus _tmp_12 = __converters.toInvoiceStatus(_tmp_11);
            if (_tmp_12 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.InvoiceStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_12;
            }
            final PaymentStatus _tmpPaymentStatus;
            final String _tmp_13;
            if (_cursor.isNull(_cursorIndexOfPaymentStatus)) {
              _tmp_13 = null;
            } else {
              _tmp_13 = _cursor.getString(_cursorIndexOfPaymentStatus);
            }
            final PaymentStatus _tmp_14 = __converters.toPaymentStatus(_tmp_13);
            if (_tmp_14 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.PaymentStatus', but it was NULL.");
            } else {
              _tmpPaymentStatus = _tmp_14;
            }
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp_15;
            if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
              _tmp_15 = null;
            } else {
              _tmp_15 = _cursor.getString(_cursorIndexOfPaymentMethod);
            }
            _tmpPaymentMethod = __converters.toPaymentMethod(_tmp_15);
            final BigDecimal _tmpAmountPaid;
            final String _tmp_16;
            if (_cursor.isNull(_cursorIndexOfAmountPaid)) {
              _tmp_16 = null;
            } else {
              _tmp_16 = _cursor.getString(_cursorIndexOfAmountPaid);
            }
            final BigDecimal _tmp_17 = __converters.toBigDecimal(_tmp_16);
            if (_tmp_17 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpAmountPaid = _tmp_17;
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpFinalizedAt;
            if (_cursor.isNull(_cursorIndexOfFinalizedAt)) {
              _tmpFinalizedAt = null;
            } else {
              _tmpFinalizedAt = _cursor.getLong(_cursorIndexOfFinalizedAt);
            }
            _item = new InvoiceEntity(_tmpId,_tmpInvoiceNumber,_tmpInvoiceDate,_tmpDueDate,_tmpCustomerId,_tmpCustomerName,_tmpCustomerGstin,_tmpCustomerState,_tmpCustomerAddress,_tmpSubtotal,_tmpTotalDiscount,_tmpTotalTax,_tmpGrandTotal,_tmpTaxType,_tmpStatus,_tmpPaymentStatus,_tmpPaymentMethod,_tmpAmountPaid,_tmpNotes,_tmpCreatedAt,_tmpUpdatedAt,_tmpFinalizedAt);
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
  public Flow<List<InvoiceEntity>> filterByDateRange(final long startDate, final long endDate) {
    final String _sql = "SELECT * FROM invoices WHERE invoiceDate >= ? AND invoiceDate <= ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"invoices"}, new Callable<List<InvoiceEntity>>() {
      @Override
      @NonNull
      public List<InvoiceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfInvoiceNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceNumber");
          final int _cursorIndexOfInvoiceDate = CursorUtil.getColumnIndexOrThrow(_cursor, "invoiceDate");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfCustomerId = CursorUtil.getColumnIndexOrThrow(_cursor, "customerId");
          final int _cursorIndexOfCustomerName = CursorUtil.getColumnIndexOrThrow(_cursor, "customerName");
          final int _cursorIndexOfCustomerGstin = CursorUtil.getColumnIndexOrThrow(_cursor, "customerGstin");
          final int _cursorIndexOfCustomerState = CursorUtil.getColumnIndexOrThrow(_cursor, "customerState");
          final int _cursorIndexOfCustomerAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "customerAddress");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfTotalDiscount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDiscount");
          final int _cursorIndexOfTotalTax = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTax");
          final int _cursorIndexOfGrandTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "grandTotal");
          final int _cursorIndexOfTaxType = CursorUtil.getColumnIndexOrThrow(_cursor, "taxType");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPaymentStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStatus");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfAmountPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "amountPaid");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfFinalizedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "finalizedAt");
          final List<InvoiceEntity> _result = new ArrayList<InvoiceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final InvoiceEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpInvoiceNumber;
            _tmpInvoiceNumber = _cursor.getString(_cursorIndexOfInvoiceNumber);
            final long _tmpInvoiceDate;
            _tmpInvoiceDate = _cursor.getLong(_cursorIndexOfInvoiceDate);
            final Long _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getLong(_cursorIndexOfDueDate);
            }
            final Long _tmpCustomerId;
            if (_cursor.isNull(_cursorIndexOfCustomerId)) {
              _tmpCustomerId = null;
            } else {
              _tmpCustomerId = _cursor.getLong(_cursorIndexOfCustomerId);
            }
            final String _tmpCustomerName;
            _tmpCustomerName = _cursor.getString(_cursorIndexOfCustomerName);
            final String _tmpCustomerGstin;
            if (_cursor.isNull(_cursorIndexOfCustomerGstin)) {
              _tmpCustomerGstin = null;
            } else {
              _tmpCustomerGstin = _cursor.getString(_cursorIndexOfCustomerGstin);
            }
            final String _tmpCustomerState;
            if (_cursor.isNull(_cursorIndexOfCustomerState)) {
              _tmpCustomerState = null;
            } else {
              _tmpCustomerState = _cursor.getString(_cursorIndexOfCustomerState);
            }
            final String _tmpCustomerAddress;
            if (_cursor.isNull(_cursorIndexOfCustomerAddress)) {
              _tmpCustomerAddress = null;
            } else {
              _tmpCustomerAddress = _cursor.getString(_cursorIndexOfCustomerAddress);
            }
            final BigDecimal _tmpSubtotal;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSubtotal)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSubtotal);
            }
            final BigDecimal _tmp_1 = __converters.toBigDecimal(_tmp);
            if (_tmp_1 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpSubtotal = _tmp_1;
            }
            final BigDecimal _tmpTotalDiscount;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfTotalDiscount)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfTotalDiscount);
            }
            final BigDecimal _tmp_3 = __converters.toBigDecimal(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalDiscount = _tmp_3;
            }
            final BigDecimal _tmpTotalTax;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfTotalTax)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfTotalTax);
            }
            final BigDecimal _tmp_5 = __converters.toBigDecimal(_tmp_4);
            if (_tmp_5 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpTotalTax = _tmp_5;
            }
            final BigDecimal _tmpGrandTotal;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfGrandTotal)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfGrandTotal);
            }
            final BigDecimal _tmp_7 = __converters.toBigDecimal(_tmp_6);
            if (_tmp_7 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpGrandTotal = _tmp_7;
            }
            final TaxType _tmpTaxType;
            final String _tmp_8;
            if (_cursor.isNull(_cursorIndexOfTaxType)) {
              _tmp_8 = null;
            } else {
              _tmp_8 = _cursor.getString(_cursorIndexOfTaxType);
            }
            final TaxType _tmp_9 = __converters.toTaxType(_tmp_8);
            if (_tmp_9 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.TaxType', but it was NULL.");
            } else {
              _tmpTaxType = _tmp_9;
            }
            final InvoiceStatus _tmpStatus;
            final String _tmp_10;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_10 = null;
            } else {
              _tmp_10 = _cursor.getString(_cursorIndexOfStatus);
            }
            final InvoiceStatus _tmp_11 = __converters.toInvoiceStatus(_tmp_10);
            if (_tmp_11 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.InvoiceStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_11;
            }
            final PaymentStatus _tmpPaymentStatus;
            final String _tmp_12;
            if (_cursor.isNull(_cursorIndexOfPaymentStatus)) {
              _tmp_12 = null;
            } else {
              _tmp_12 = _cursor.getString(_cursorIndexOfPaymentStatus);
            }
            final PaymentStatus _tmp_13 = __converters.toPaymentStatus(_tmp_12);
            if (_tmp_13 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.kjbilling.app.domain.model.PaymentStatus', but it was NULL.");
            } else {
              _tmpPaymentStatus = _tmp_13;
            }
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp_14;
            if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
              _tmp_14 = null;
            } else {
              _tmp_14 = _cursor.getString(_cursorIndexOfPaymentMethod);
            }
            _tmpPaymentMethod = __converters.toPaymentMethod(_tmp_14);
            final BigDecimal _tmpAmountPaid;
            final String _tmp_15;
            if (_cursor.isNull(_cursorIndexOfAmountPaid)) {
              _tmp_15 = null;
            } else {
              _tmp_15 = _cursor.getString(_cursorIndexOfAmountPaid);
            }
            final BigDecimal _tmp_16 = __converters.toBigDecimal(_tmp_15);
            if (_tmp_16 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.math.BigDecimal', but it was NULL.");
            } else {
              _tmpAmountPaid = _tmp_16;
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Long _tmpFinalizedAt;
            if (_cursor.isNull(_cursorIndexOfFinalizedAt)) {
              _tmpFinalizedAt = null;
            } else {
              _tmpFinalizedAt = _cursor.getLong(_cursorIndexOfFinalizedAt);
            }
            _item = new InvoiceEntity(_tmpId,_tmpInvoiceNumber,_tmpInvoiceDate,_tmpDueDate,_tmpCustomerId,_tmpCustomerName,_tmpCustomerGstin,_tmpCustomerState,_tmpCustomerAddress,_tmpSubtotal,_tmpTotalDiscount,_tmpTotalTax,_tmpGrandTotal,_tmpTaxType,_tmpStatus,_tmpPaymentStatus,_tmpPaymentMethod,_tmpAmountPaid,_tmpNotes,_tmpCreatedAt,_tmpUpdatedAt,_tmpFinalizedAt);
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
