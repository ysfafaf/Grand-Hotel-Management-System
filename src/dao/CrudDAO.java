package dao;

import java.util.List;
import model.DatabaseException;

public interface CrudDAO<T> {

    void tambah(T data) throws DatabaseException;

    List<T> semua() throws DatabaseException;

    void ubah(T data) throws DatabaseException;

    void hapus(T data) throws DatabaseException;
}

