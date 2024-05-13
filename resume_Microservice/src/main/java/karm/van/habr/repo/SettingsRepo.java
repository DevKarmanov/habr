package karm.van.habr.repo;

import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepo extends JpaRepository<Settings,Long> {
    Settings findByUser(MyUser user);
}
