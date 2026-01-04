package com.smartcrops.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartcrops.model.Crop;

import java.util.List;

public interface CropRepository extends JpaRepository<Crop, Long> {

    List<Crop> findBySeason(String season);
}
