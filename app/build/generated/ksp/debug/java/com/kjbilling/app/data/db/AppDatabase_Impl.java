package com.kjbilling.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.kjbilling.app.data.db.dao.AppSettingsDao;
import com.kjbilling.app.data.db.dao.AppSettingsDao_Impl;
import com.kjbilling.app.data.db.dao.BusinessProfileDao;
import com.kjbilling.app.data.db.dao.BusinessProfileDao_Impl;
import com.kjbilling.app.data.db.dao.CustomerDao;
import com.kjbilling.app.data.db.dao.CustomerDao_Impl;
import com.kjbilling.app.data.db.dao.InvoiceDao;
import com.kjbilling.app.data.db.dao.InvoiceDao_Impl;
import com.kjbilling.app.data.db.dao.ProductDao;
import com.kjbilling.app.data.db.dao.ProductDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile BusinessProfileDao _businessProfileDao;

  private volatile CustomerDao _customerDao;

  private volatile ProductDao _productDao;

  private volatile InvoiceDao _invoiceDao;

  private volatile AppSettingsDao _appSettingsDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `business_profiles` (`id` INTEGER NOT NULL, `businessName` TEXT NOT NULL, `ownerName` TEXT NOT NULL, `mobile` TEXT NOT NULL, `address` TEXT NOT NULL, `state` TEXT NOT NULL, `gstin` TEXT, `email` TEXT, `city` TEXT, `pincode` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `customers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `mobile` TEXT, `email` TEXT, `billingAddress` TEXT, `state` TEXT, `pincode` TEXT, `gstin` TEXT, `businessName` TEXT, `notes` TEXT, `isWalkIn` INTEGER NOT NULL, `lastUsedAt` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_name` ON `customers` (`name`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_mobile` ON `customers` (`mobile`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_gstin` ON `customers` (`gstin`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_lastUsedAt` ON `customers` (`lastUsedAt`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `products` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `sellingPrice` TEXT NOT NULL, `hsnCode` TEXT, `unit` TEXT NOT NULL, `gstRate` TEXT, `description` TEXT, `sku` TEXT, `useCount` INTEGER NOT NULL, `lastUsedAt` INTEGER, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_name` ON `products` (`name`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_sku` ON `products` (`sku`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_lastUsedAt` ON `products` (`lastUsedAt`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_useCount` ON `products` (`useCount`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `invoices` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `invoiceNumber` TEXT NOT NULL, `invoiceDate` INTEGER NOT NULL, `dueDate` INTEGER, `customerId` INTEGER, `customerName` TEXT NOT NULL, `customerGstin` TEXT, `customerState` TEXT, `customerAddress` TEXT, `subtotal` TEXT NOT NULL, `totalDiscount` TEXT NOT NULL, `totalTax` TEXT NOT NULL, `grandTotal` TEXT NOT NULL, `taxType` TEXT NOT NULL, `status` TEXT NOT NULL, `paymentStatus` TEXT NOT NULL, `paymentMethod` TEXT, `amountPaid` TEXT NOT NULL, `notes` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `finalizedAt` INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_invoices_invoiceNumber` ON `invoices` (`invoiceNumber`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_customerId` ON `invoices` (`customerId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_customerName` ON `invoices` (`customerName`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_createdAt` ON `invoices` (`createdAt`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_status` ON `invoices` (`status`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `invoice_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `invoiceId` INTEGER NOT NULL, `productId` INTEGER, `itemName` TEXT NOT NULL, `description` TEXT, `hsnCode` TEXT, `quantity` TEXT NOT NULL, `unit` TEXT, `unitPrice` TEXT NOT NULL, `discountPercent` TEXT NOT NULL, `discountAmount` TEXT NOT NULL, `gstRate` TEXT NOT NULL, `taxableAmount` TEXT NOT NULL, `cgstAmount` TEXT, `sgstAmount` TEXT, `igstAmount` TEXT, `taxAmount` TEXT NOT NULL, `total` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, FOREIGN KEY(`invoiceId`) REFERENCES `invoices`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoice_items_invoiceId` ON `invoice_items` (`invoiceId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_settings` (`id` INTEGER NOT NULL, `gstEnabled` INTEGER NOT NULL, `defaultGstRate` TEXT NOT NULL, `defaultTaxType` TEXT NOT NULL, `invoicePrefix` TEXT NOT NULL, `nextInvoiceNumber` INTEGER NOT NULL, `onboardingCompleted` INTEGER NOT NULL, `defaultPaymentStatus` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'dde54a20938c817461a34a14dcce2c3b')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `business_profiles`");
        db.execSQL("DROP TABLE IF EXISTS `customers`");
        db.execSQL("DROP TABLE IF EXISTS `products`");
        db.execSQL("DROP TABLE IF EXISTS `invoices`");
        db.execSQL("DROP TABLE IF EXISTS `invoice_items`");
        db.execSQL("DROP TABLE IF EXISTS `app_settings`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsBusinessProfiles = new HashMap<String, TableInfo.Column>(12);
        _columnsBusinessProfiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("businessName", new TableInfo.Column("businessName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("ownerName", new TableInfo.Column("ownerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("mobile", new TableInfo.Column("mobile", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("address", new TableInfo.Column("address", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("state", new TableInfo.Column("state", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("gstin", new TableInfo.Column("gstin", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("city", new TableInfo.Column("city", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("pincode", new TableInfo.Column("pincode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBusinessProfiles.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBusinessProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBusinessProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBusinessProfiles = new TableInfo("business_profiles", _columnsBusinessProfiles, _foreignKeysBusinessProfiles, _indicesBusinessProfiles);
        final TableInfo _existingBusinessProfiles = TableInfo.read(db, "business_profiles");
        if (!_infoBusinessProfiles.equals(_existingBusinessProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "business_profiles(com.kjbilling.app.data.db.entity.BusinessProfileEntity).\n"
                  + " Expected:\n" + _infoBusinessProfiles + "\n"
                  + " Found:\n" + _existingBusinessProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsCustomers = new HashMap<String, TableInfo.Column>(14);
        _columnsCustomers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("mobile", new TableInfo.Column("mobile", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("billingAddress", new TableInfo.Column("billingAddress", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("state", new TableInfo.Column("state", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("pincode", new TableInfo.Column("pincode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("gstin", new TableInfo.Column("gstin", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("businessName", new TableInfo.Column("businessName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("isWalkIn", new TableInfo.Column("isWalkIn", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("lastUsedAt", new TableInfo.Column("lastUsedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomers.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCustomers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCustomers = new HashSet<TableInfo.Index>(4);
        _indicesCustomers.add(new TableInfo.Index("index_customers_name", false, Arrays.asList("name"), Arrays.asList("ASC")));
        _indicesCustomers.add(new TableInfo.Index("index_customers_mobile", false, Arrays.asList("mobile"), Arrays.asList("ASC")));
        _indicesCustomers.add(new TableInfo.Index("index_customers_gstin", false, Arrays.asList("gstin"), Arrays.asList("ASC")));
        _indicesCustomers.add(new TableInfo.Index("index_customers_lastUsedAt", false, Arrays.asList("lastUsedAt"), Arrays.asList("ASC")));
        final TableInfo _infoCustomers = new TableInfo("customers", _columnsCustomers, _foreignKeysCustomers, _indicesCustomers);
        final TableInfo _existingCustomers = TableInfo.read(db, "customers");
        if (!_infoCustomers.equals(_existingCustomers)) {
          return new RoomOpenHelper.ValidationResult(false, "customers(com.kjbilling.app.data.db.entity.CustomerEntity).\n"
                  + " Expected:\n" + _infoCustomers + "\n"
                  + " Found:\n" + _existingCustomers);
        }
        final HashMap<String, TableInfo.Column> _columnsProducts = new HashMap<String, TableInfo.Column>(12);
        _columnsProducts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("sellingPrice", new TableInfo.Column("sellingPrice", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("hsnCode", new TableInfo.Column("hsnCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("gstRate", new TableInfo.Column("gstRate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("sku", new TableInfo.Column("sku", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("useCount", new TableInfo.Column("useCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("lastUsedAt", new TableInfo.Column("lastUsedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProducts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProducts = new HashSet<TableInfo.Index>(4);
        _indicesProducts.add(new TableInfo.Index("index_products_name", false, Arrays.asList("name"), Arrays.asList("ASC")));
        _indicesProducts.add(new TableInfo.Index("index_products_sku", false, Arrays.asList("sku"), Arrays.asList("ASC")));
        _indicesProducts.add(new TableInfo.Index("index_products_lastUsedAt", false, Arrays.asList("lastUsedAt"), Arrays.asList("ASC")));
        _indicesProducts.add(new TableInfo.Index("index_products_useCount", false, Arrays.asList("useCount"), Arrays.asList("ASC")));
        final TableInfo _infoProducts = new TableInfo("products", _columnsProducts, _foreignKeysProducts, _indicesProducts);
        final TableInfo _existingProducts = TableInfo.read(db, "products");
        if (!_infoProducts.equals(_existingProducts)) {
          return new RoomOpenHelper.ValidationResult(false, "products(com.kjbilling.app.data.db.entity.ProductEntity).\n"
                  + " Expected:\n" + _infoProducts + "\n"
                  + " Found:\n" + _existingProducts);
        }
        final HashMap<String, TableInfo.Column> _columnsInvoices = new HashMap<String, TableInfo.Column>(22);
        _columnsInvoices.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("invoiceNumber", new TableInfo.Column("invoiceNumber", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("invoiceDate", new TableInfo.Column("invoiceDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("dueDate", new TableInfo.Column("dueDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("customerId", new TableInfo.Column("customerId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("customerName", new TableInfo.Column("customerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("customerGstin", new TableInfo.Column("customerGstin", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("customerState", new TableInfo.Column("customerState", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("customerAddress", new TableInfo.Column("customerAddress", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("subtotal", new TableInfo.Column("subtotal", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("totalDiscount", new TableInfo.Column("totalDiscount", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("totalTax", new TableInfo.Column("totalTax", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("grandTotal", new TableInfo.Column("grandTotal", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("taxType", new TableInfo.Column("taxType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("paymentStatus", new TableInfo.Column("paymentStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("paymentMethod", new TableInfo.Column("paymentMethod", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("amountPaid", new TableInfo.Column("amountPaid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoices.put("finalizedAt", new TableInfo.Column("finalizedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInvoices = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInvoices = new HashSet<TableInfo.Index>(5);
        _indicesInvoices.add(new TableInfo.Index("index_invoices_invoiceNumber", true, Arrays.asList("invoiceNumber"), Arrays.asList("ASC")));
        _indicesInvoices.add(new TableInfo.Index("index_invoices_customerId", false, Arrays.asList("customerId"), Arrays.asList("ASC")));
        _indicesInvoices.add(new TableInfo.Index("index_invoices_customerName", false, Arrays.asList("customerName"), Arrays.asList("ASC")));
        _indicesInvoices.add(new TableInfo.Index("index_invoices_createdAt", false, Arrays.asList("createdAt"), Arrays.asList("ASC")));
        _indicesInvoices.add(new TableInfo.Index("index_invoices_status", false, Arrays.asList("status"), Arrays.asList("ASC")));
        final TableInfo _infoInvoices = new TableInfo("invoices", _columnsInvoices, _foreignKeysInvoices, _indicesInvoices);
        final TableInfo _existingInvoices = TableInfo.read(db, "invoices");
        if (!_infoInvoices.equals(_existingInvoices)) {
          return new RoomOpenHelper.ValidationResult(false, "invoices(com.kjbilling.app.data.db.entity.InvoiceEntity).\n"
                  + " Expected:\n" + _infoInvoices + "\n"
                  + " Found:\n" + _existingInvoices);
        }
        final HashMap<String, TableInfo.Column> _columnsInvoiceItems = new HashMap<String, TableInfo.Column>(19);
        _columnsInvoiceItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("invoiceId", new TableInfo.Column("invoiceId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("productId", new TableInfo.Column("productId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("itemName", new TableInfo.Column("itemName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("hsnCode", new TableInfo.Column("hsnCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("quantity", new TableInfo.Column("quantity", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("unit", new TableInfo.Column("unit", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("unitPrice", new TableInfo.Column("unitPrice", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("discountPercent", new TableInfo.Column("discountPercent", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("discountAmount", new TableInfo.Column("discountAmount", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("gstRate", new TableInfo.Column("gstRate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("taxableAmount", new TableInfo.Column("taxableAmount", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("cgstAmount", new TableInfo.Column("cgstAmount", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("sgstAmount", new TableInfo.Column("sgstAmount", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("igstAmount", new TableInfo.Column("igstAmount", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("taxAmount", new TableInfo.Column("taxAmount", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("total", new TableInfo.Column("total", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvoiceItems.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInvoiceItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysInvoiceItems.add(new TableInfo.ForeignKey("invoices", "CASCADE", "NO ACTION", Arrays.asList("invoiceId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesInvoiceItems = new HashSet<TableInfo.Index>(1);
        _indicesInvoiceItems.add(new TableInfo.Index("index_invoice_items_invoiceId", false, Arrays.asList("invoiceId"), Arrays.asList("ASC")));
        final TableInfo _infoInvoiceItems = new TableInfo("invoice_items", _columnsInvoiceItems, _foreignKeysInvoiceItems, _indicesInvoiceItems);
        final TableInfo _existingInvoiceItems = TableInfo.read(db, "invoice_items");
        if (!_infoInvoiceItems.equals(_existingInvoiceItems)) {
          return new RoomOpenHelper.ValidationResult(false, "invoice_items(com.kjbilling.app.data.db.entity.InvoiceItemEntity).\n"
                  + " Expected:\n" + _infoInvoiceItems + "\n"
                  + " Found:\n" + _existingInvoiceItems);
        }
        final HashMap<String, TableInfo.Column> _columnsAppSettings = new HashMap<String, TableInfo.Column>(8);
        _columnsAppSettings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSettings.put("gstEnabled", new TableInfo.Column("gstEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSettings.put("defaultGstRate", new TableInfo.Column("defaultGstRate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSettings.put("defaultTaxType", new TableInfo.Column("defaultTaxType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSettings.put("invoicePrefix", new TableInfo.Column("invoicePrefix", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSettings.put("nextInvoiceNumber", new TableInfo.Column("nextInvoiceNumber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSettings.put("onboardingCompleted", new TableInfo.Column("onboardingCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSettings.put("defaultPaymentStatus", new TableInfo.Column("defaultPaymentStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppSettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppSettings = new TableInfo("app_settings", _columnsAppSettings, _foreignKeysAppSettings, _indicesAppSettings);
        final TableInfo _existingAppSettings = TableInfo.read(db, "app_settings");
        if (!_infoAppSettings.equals(_existingAppSettings)) {
          return new RoomOpenHelper.ValidationResult(false, "app_settings(com.kjbilling.app.data.db.entity.AppSettingsEntity).\n"
                  + " Expected:\n" + _infoAppSettings + "\n"
                  + " Found:\n" + _existingAppSettings);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "dde54a20938c817461a34a14dcce2c3b", "b12984954ab50d99d201aa959da5d116");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "business_profiles","customers","products","invoices","invoice_items","app_settings");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `business_profiles`");
      _db.execSQL("DELETE FROM `customers`");
      _db.execSQL("DELETE FROM `products`");
      _db.execSQL("DELETE FROM `invoices`");
      _db.execSQL("DELETE FROM `invoice_items`");
      _db.execSQL("DELETE FROM `app_settings`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(BusinessProfileDao.class, BusinessProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CustomerDao.class, CustomerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProductDao.class, ProductDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InvoiceDao.class, InvoiceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AppSettingsDao.class, AppSettingsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public BusinessProfileDao businessProfileDao() {
    if (_businessProfileDao != null) {
      return _businessProfileDao;
    } else {
      synchronized(this) {
        if(_businessProfileDao == null) {
          _businessProfileDao = new BusinessProfileDao_Impl(this);
        }
        return _businessProfileDao;
      }
    }
  }

  @Override
  public CustomerDao customerDao() {
    if (_customerDao != null) {
      return _customerDao;
    } else {
      synchronized(this) {
        if(_customerDao == null) {
          _customerDao = new CustomerDao_Impl(this);
        }
        return _customerDao;
      }
    }
  }

  @Override
  public ProductDao productDao() {
    if (_productDao != null) {
      return _productDao;
    } else {
      synchronized(this) {
        if(_productDao == null) {
          _productDao = new ProductDao_Impl(this);
        }
        return _productDao;
      }
    }
  }

  @Override
  public InvoiceDao invoiceDao() {
    if (_invoiceDao != null) {
      return _invoiceDao;
    } else {
      synchronized(this) {
        if(_invoiceDao == null) {
          _invoiceDao = new InvoiceDao_Impl(this);
        }
        return _invoiceDao;
      }
    }
  }

  @Override
  public AppSettingsDao appSettingsDao() {
    if (_appSettingsDao != null) {
      return _appSettingsDao;
    } else {
      synchronized(this) {
        if(_appSettingsDao == null) {
          _appSettingsDao = new AppSettingsDao_Impl(this);
        }
        return _appSettingsDao;
      }
    }
  }
}
