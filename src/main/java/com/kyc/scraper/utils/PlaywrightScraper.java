package com.kyc.scraper.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kyc.scraper.persistance.models.Member;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PlaywrightScraper {
    public List<Member> scrapeData() {
        String baseUrl = "https://akleg.gov/senate.php";
        List<Member> members = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(baseUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            // 1️⃣ Collect only profile links (pattern: /basis/Member/Detail/)
            List<Object> rawHrefs = (List<Object>) page.evalOnSelectorAll("a", "els => els.map(a => a.href)");
            Set<String> profileLinks = new LinkedHashSet<>();
            for (Object o : rawHrefs) {
                String h = o.toString();
                if (h.contains("/basis/Member/Detail/")) {
                    profileLinks.add(h);
                }
            }

            // 2️⃣ Visit each senator's profile page
            for (String profileUrl : profileLinks) {
                try {
                    Page p = context.newPage();
                    p.navigate(profileUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    Member m = new Member();
                    m.setUrl(profileUrl);
                    m.setTitle("Senator");

                    // --- Extract Name (cleaned) ---
                    String name = "";
                    if (p.locator(".memberName").count() > 0) {
                        name = p.locator(".memberName").first().innerText().trim();
                    } else if (p.locator("h1").count() > 0) {
                        name = p.locator("h1").first().innerText().replaceAll("(?i)SENATE", "").trim();
                    }
                    // Remove any extra lines or legislature info
                    name = name.replaceAll("\\n.*", "").trim();
                    m.setName(name);

                    // --- Extract Party, District, Phone ---
                    m.setParty(findLabelValue(p, "Party"));
                    m.setPosition(findLabelValue(p, "District"));
                    m.setPhone(findLabelValue(p, "Phone"));

                    // --- Extract Address (Session + Interim Contact) ---
                    String address = "";

                    // Collect all text under the "Session Contact" section
                    if (p.locator("text=Session Contact").count() > 0) {
                        Locator section = p.locator("text=Session Contact").first().locator("xpath=following-sibling::br/..");
                        address += section.innerText().replace("Session Contact", "").trim();
                    }

                    // Collect all text under the "Interim Contact" section
                    if (p.locator("text=Interim Contact").count() > 0) {
                        Locator section = p.locator("text=Interim Contact").first().locator("xpath=following-sibling::br/..");
                        String interim = section.innerText().replace("Interim Contact", "").trim();
                        if (!interim.isEmpty()) {
                            address += " | " + interim;
                        }
                    }

                    // Clean the address
                    address = address.replaceAll("\\s{2,}", " ").replaceAll("(?i)phone.*", "").trim();
                    m.setAddress(address);

                    // --- Extract Email ---
                    List<Object> mailtos = (List<Object>) p.evalOnSelectorAll(
                            "a[href^='mailto:']",
                            "els => els.map(a => a.getAttribute('href'))"
                    );
                    if (!mailtos.isEmpty()) {
                        m.setEmail(mailtos.get(0).toString().replace("mailto:", ""));
                    } else {
                        m.setEmail("");
                    }

                    members.add(m);
                    p.close();
                } catch (Exception ignored) {}
            }

            // 3️⃣ Save JSON file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter("ak_senate_members.json")) {
                gson.toJson(members, writer);
            }

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return members;
    }

    private static String textOrEmpty(Page p, String sel) {
        try {
            Locator loc = p.locator(sel);
            if (loc.count()>0) return loc.first().innerText().trim();
        } catch (Exception ignored) {}
        return "";
    }

    private static String findLabelValue(Page p, String label) {
        try {
            List<Object> nodes = (List<Object>) p.evalOnSelectorAll("body *",
                    "els => els.filter(e => (e.innerText||'').toLowerCase().includes('"+label.toLowerCase()+"')).map(e => e.innerText)");
            if (!nodes.isEmpty()) {
                for (Object o : nodes) {
                    String s = o.toString();
                    int idx = s.toLowerCase().indexOf(label.toLowerCase());
                    if (idx >= 0) {
                        String after = s.substring(idx + label.length()).replaceFirst("^\\s*[:\\-]*\\s*", "");
                        return after.split("\\n")[0].trim();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "";
    }
}