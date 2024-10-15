package com.levodev.data.repository;


import com.levodev.data.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LocationRepository
        extends
            JpaRepository<Location, Long>,
            JpaSpecificationExecutor<Location> {

}
