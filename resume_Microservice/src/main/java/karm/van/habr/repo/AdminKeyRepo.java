package karm.van.habr.repo;

import karm.van.habr.entity.AdminKey;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface AdminKeyRepo extends JpaRepository<AdminKey,Long> {
    boolean existsById(@NotNull Long id);
}
