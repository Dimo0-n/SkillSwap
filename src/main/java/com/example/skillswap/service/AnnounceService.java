package com.example.skillswap.service;

import com.example.skillswap.entity.Announce;

import java.util.List;

public interface AnnounceService {

    List<Announce> getLatest5Announces();

    List<Announce> getAnnouncesList();
}
