package com.kyc.scraper.controllers;

import com.kyc.scraper.persistance.models.Member;
import com.kyc.scraper.services.ScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ScraperController {
    @Autowired
    ScraperService scraperService;

    @GetMapping("/scrape")
    public List<Member> scrapeData() {
        return scraperService.scrapeSenateData();
    }
}
