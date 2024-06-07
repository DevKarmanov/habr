package karm.van.habr.repo;

import karm.van.habr.entity.Complaint;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepo extends JpaRepository<Complaint,Long> {
    Optional<Complaint> findComplaintByInspectResume(Resume resume);
    Optional<Complaint> findComplaintByInspectUser(MyUser user);

}
