package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FamilyTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyTransferRepository extends JpaRepository<FamilyTransferEntity, Long> {
    List<FamilyTransferEntity> findByFamilyIdOrderByTransferDateDesc(Long familyId);
}
