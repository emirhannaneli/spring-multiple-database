package dev.emirman.util.spring.multiple.database.exception;

public class DatabaseNotDefined extends RuntimeException{
    public DatabaseNotDefined() {
        super("There is no database in the header.");
    }
}
