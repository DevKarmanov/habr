package karm.van.habr.repo;

import karm.van.habr.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepo extends JpaRepository<Complaint,Long> {
}
