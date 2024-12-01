package karm.van.habr.repo;

import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeRepo extends JpaRepository<Resume,Long> {


    @Query("SELECT r from Resume " +
            "r WHERE LOWER(r.title) like LOWER(CONCAT('%', :filter, '%'))")
    Page<Resume> findByTitleContaining(String filter, Pageable pageable);

    List<Resume> findByAuthor(MyUser user);

    @Query("SELECT r FROM Resume r LEFT JOIN FETCH r.images LEFT JOIN FETCH r.author LEFT JOIN FETCH r.comments WHERE r.id = :id")
    Optional<Resume> findWithDetails(@Param("id") Long id);
}
