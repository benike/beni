package hu.beni.amusementpark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hu.beni.amusementpark.entity.AmusementPark;

@Repository
public interface AmusementParkRepository extends JpaRepository<AmusementPark, Long> {

    @Modifying
    @Query("Update AmusementPark a set a.capital = a.capital - :ammount where a.id = :id")
    public void decreaseCapitalById(@Param("ammount") Integer ammount, @Param("id") Long id);

    @Modifying
    @Query("Update AmusementPark a set a.capital = a.capital + :ammount where a.id = :id")
    public void incrementCapitalById(@Param("ammount") Integer ammount, @Param("id") Long id);

    @Query("Select a.entranceFee from AmusementPark a where a.id = :id")
    public Integer findEntranceFeeById(@Param("id") Long id);

}