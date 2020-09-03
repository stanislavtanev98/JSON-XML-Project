package hiberspring.repository;


import hiberspring.domain.entities.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Branch getByName(String name);

    @Query("SELECT b FROM Branch AS b " +
            "JOIN Product AS p ON b.id = p.branch.id")
    List<Branch> findAllByBranchesWithProducts();
}
