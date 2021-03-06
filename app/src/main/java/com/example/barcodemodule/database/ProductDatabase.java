package com.example.barcodemodule.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.barcodemodule.models.Product;

@Database(entities = {Product.class}, version = 1, exportSchema = false)
public abstract class ProductDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "productDb";
    private static ProductDatabase sInstance;

    public static ProductDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(),
                    ProductDatabase.class, ProductDatabase.DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return sInstance;
    }

    public abstract ProductDao ProductDao();
}
