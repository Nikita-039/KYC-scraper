package com.kyc.scraper.services.impl;

import com.kyc.scraper.persistance.models.Member;
import com.kyc.scraper.services.ScraperService;
import com.kyc.scraper.utils.PlaywrightScraper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScraperServiceImpl implements ScraperService {

    @Override
    public List<Member> scrapeSenateData() {
        PlaywrightScraper scraper = new PlaywrightScraper();
        return scraper.scrapeData();
    }
}
