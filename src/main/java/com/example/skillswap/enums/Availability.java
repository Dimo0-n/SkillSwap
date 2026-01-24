package com.example.skillswap.enums;

public enum Availability {

    DIMINEATA(1 << 0),      // 1
    DUPA_AMIAZA(1 << 1),    // 2
    SEARA(1 << 2),          // 4
    WEEKEND(1 << 3);        // 8

    private final int bit;

    Availability(int bit) {
        this.bit = bit;
    }

    public int getBit() {
        return bit;
    }
}
