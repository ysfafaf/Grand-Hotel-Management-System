package model;

public class DatabaseException extends Exception {

    public DatabaseException(String pesan) {
        super(pesan);
    }

    public DatabaseException(String pesan, Throwable penyebab) {
        super(pesan, penyebab);
    }
}

