package com.example.EarthquakeCheck.service.impl;

import com.example.EarthquakeCheck.DTO.BuildingRequestDTO;
import com.example.EarthquakeCheck.model.Building;
import com.example.EarthquakeCheck.repository.BuildingRepository;
import com.example.EarthquakeCheck.service.BuildingManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BuildingManagementServiceImpl implements BuildingManagementService {

    private final BuildingRepository buildingRepository;

    @Override
    @Transactional
    public Building createBuilding(BuildingRequestDTO requestDTO) {
        Building building = new Building();
        building.setFloorCount(requestDTO.getKatSayisi());
        building.setYearBuilt(requestDTO.getYapimYili());
        return buildingRepository.save(building);
    }

    @Override
    @Transactional
    public Building updateBuilding(Long id, BuildingRequestDTO requestDTO) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Bina bulunamadi: " + id));

        building.setFloorCount(requestDTO.getKatSayisi());
        building.setYearBuilt(requestDTO.getYapimYili());
        return buildingRepository.save(building);
    }
}
