package com.b2c.Local.B2C.store.dao;

import com.b2c.Local.B2C.store.model.LocalStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocalStoreRepository extends JpaRepository<LocalStore, Long> {

    List<LocalStore> findByUserId(UUID userId);

}