package com.smartcrops.config;

import com.smartcrops.model.Crop;
import com.smartcrops.repository.CropRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CropSeeder implements CommandLineRunner {

    @Autowired
    private CropRepository cropRepository;

    @Override
    public void run(String... args) throws Exception {

        // 🔐 Prevent duplicate inserts
        if (cropRepository.count() > 50) {
            System.out.println("🌱 Crops already exist, skipping CSV import");
            return;
        }

        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                new ClassPathResource("crops_22_2200_entries.csv").getInputStream()
            )
        );

        String line;
        boolean header = true;
        List<Crop> batch = new ArrayList<>();

        while ((line = br.readLine()) != null) {

            // skip header row
            if (header) {
                header = false;
                continue;
            }

            String[] d = line.split(",");

            Crop crop = new Crop();
            crop.setName(d[0]);                    // name
            crop.setSeason(d[1]);                  // season
            crop.setMinPh(Double.parseDouble(d[2]));
            crop.setMaxPh(Double.parseDouble(d[3]));
            crop.setMinRainfall(Double.parseDouble(d[4]));
            crop.setMaxRainfall(Double.parseDouble(d[5]));
            crop.setYieldPerAcre(Double.parseDouble(d[6]));

            batch.add(crop);

            // 🚀 Save in batches for performance
            if (batch.size() == 200) {
                cropRepository.saveAll(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            cropRepository.saveAll(batch);
        }

        br.close();

        System.out.println("✅ Crops CSV imported successfully!");
    }
}
