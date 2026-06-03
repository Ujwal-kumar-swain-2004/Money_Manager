package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FamilyMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyMemberRepository extends JpaRepository<FamilyMemberEntity, Long> {
    List<FamilyMemberEntity> findByFamilyIdOrderByIdAsc(Long familyId);
    Optional<FamilyMemberEntity> findByIdAndFamilyId(Long id, Long familyId);
}
