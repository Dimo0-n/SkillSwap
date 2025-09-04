package com.example.skillswap.security;

import java.io.Serializable;

public interface GrantedAuthority extends Serializable {

    String getAuthority();
}
