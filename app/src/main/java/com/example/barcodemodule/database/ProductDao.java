package com.example.barcodemodule.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.barcodemodule.models.Product;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert
    void insertProduct(Product product);

    @Query("DELETE FROM productDb")
    void clearProductDb();

    @Query("SELECT * FROM productDb")
    List<Product> loadAllProducts();

    @Query("SELECT COUNT(*) FROM productDb")
    int countProducts();
}
