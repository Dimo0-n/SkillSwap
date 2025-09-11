package com.example.skillswap.sevice;

import com.example.skillswap.entity.Announce;
import com.example.skillswap.repository.AnnounceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnounceServiceImpl implements AnnounceService {

    @Autowired
    private AnnounceRepository announceRepository;

    @Override
    public List<Announce> getLatest5Announces() {
        List<Announce> latest5Announces = announceRepository.findAll();
        return latest5Announces.subList(0, Math.min(5, latest5Announces.size()));
    }


}

