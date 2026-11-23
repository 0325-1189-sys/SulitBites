package com.sulitbites;

/** Where the data is kept. The Store does not care if it is a file or an online database (polymorphism). */
public interface Repository {
    void load(Store store);
    void save(Store store);
}
