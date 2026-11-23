package com.sulitbites;

/** INTERFACE: anything that can run out of stock. */
public interface Stockable {
    boolean isInStock();
    String getStatus();
}
