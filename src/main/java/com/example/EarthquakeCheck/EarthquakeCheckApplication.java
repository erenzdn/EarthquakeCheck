package com.example.EarthquakeCheck;

import com.example.EarthquakeCheck.entity.PgaValue;
import com.example.EarthquakeCheck.model.Building;
import com.example.EarthquakeCheck.model.EvaluationResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackageClasses = {Building.class, EvaluationResult.class, PgaValue.class})
public class EarthquakeCheckApplication {

	public static void main(String[] args) {
		SpringApplication.run(EarthquakeCheckApplication.class, args);
	}

}
