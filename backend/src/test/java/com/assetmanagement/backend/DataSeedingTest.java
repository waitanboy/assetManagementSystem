package com.assetmanagement.backend;

import com.assetmanagement.backend.entity.Asset;
import com.assetmanagement.backend.mapper.AssetMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Random;

@SpringBootTest
public class DataSeedingTest {

    @Autowired
    private AssetMapper assetMapper;

    private final String[] itNames = {
        "MacBook Pro M3", "ThinkPad X1 Carbon", "Dell XPS 15", "LG Gram 17",
        "iPad Pro 12.9\"", "iPad Air", "Samsung Galaxy Tab S9",
        "Dell UltraSharp 27\"", "LG UltraFine 4K", "Samsung Odyssey G7",
        "Logitech MX Master 3", "Keychron K2 Wireless", "Magic Keyboard", "Magic Mouse",
        "Wacom Intuos Pro", "Sony WH-1000XM5", "AirPods Pro", "Cisco IP Phone"
    };

    private final String[] officeNames = {
        "Ergonomic Chair", "Standing Desk", "Whiteboard", "Desk Lamp",
        "Filing Cabinet", "Meeting Table", "Paper Shredder", "Air Purifier"
    };

    private final String[] locations = {
        "7th Floor - IT Office", "8th Floor - Engineering", "6th Floor - Marketing",
        "Lobby", "Conference Room A", "Conference Room B", "Storage Room 1", "R&D Lab"
    };

    @Test
    public void seedAssets() {
        Random random = new Random();
        int count = 150; // Seeding 150 assets

        for (int i = 1; i <= count; i++) {
            boolean isIT = random.nextBoolean();
            String name = isIT ? itNames[random.nextInt(itNames.length)] : officeNames[random.nextInt(officeNames.length)];
            Long categoryId = isIT ? 1L : 2L;
            String serialSuffix = String.format("%04d", i + 10);
            String serialNumber = (isIT ? "SN-IT-" : "SN-OFF-") + serialSuffix;
            String status = i % 10 == 0 ? "RENTED" : (i % 25 == 0 ? "REPAIRING" : "AVAILABLE");
            String location = locations[random.nextInt(locations.length)];

            Asset asset = Asset.builder()
                .categoryId(categoryId)
                .name(name + " (#" + i + ")")
                .serialNumber(serialNumber)
                .status(status)
                .location(location)
                .useYn("Y")
                .build();

            try {
                assetMapper.insert(asset);
                System.out.println("Inserted: " + serialNumber);
            } catch (Exception e) {
                // Ignore duplicates if re-run
                System.err.println("Duplicate or error: " + serialNumber);
            }
        }
        System.out.println("✅ Successfully seeded assets.");
    }
}
