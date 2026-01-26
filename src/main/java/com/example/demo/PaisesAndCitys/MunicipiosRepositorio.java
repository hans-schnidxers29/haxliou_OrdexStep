package com.example.demo.PaisesAndCitys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MunicipiosRepositorio extends JpaRepository<Municipios,Integer> {
}
