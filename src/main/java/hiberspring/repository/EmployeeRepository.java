package hiberspring.repository;

import hiberspring.domain.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee AS e " +
            "JOIN Branch AS b ON b.id = e.branch.id " +
            "JOIN Product AS p ON b.id = p.branch.id " +
            "ORDER BY CONCAT(e.firstName,' ',e.lastName) ASC, " +
            "LENGTH(e.position) DESC")
    List<Employee> findAllWhichBranchesHaveMoreThanOneProduct();
}
