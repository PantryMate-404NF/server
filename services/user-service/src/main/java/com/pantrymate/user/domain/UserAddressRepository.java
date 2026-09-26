package com.pantrymate.user.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserIdOrderByDefaultAddressDescAddressIdDesc(Long userId);

    Optional<UserAddress> findByAddressIdAndUserId(Long addressId, Long userId);

    Optional<UserAddress> findByUserIdAndDefaultAddressTrue(Long userId);

    Optional<UserAddress> findFirstByUserIdOrderByAddressIdDesc(Long userId);

    long countByUserId(Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE UserAddress a SET a.defaultAddress = false WHERE a.userId = :userId AND a.defaultAddress = true")
    int clearDefault(@Param("userId") Long userId);
}
