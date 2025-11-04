package com.kyc.scraper.services;

import com.kyc.scraper.persistance.models.Member;

import java.util.List;

public interface ScraperService {
    public List<Member> scrapeSenateData();
}
