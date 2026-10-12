package edu.anurag.auconnect.repo;

import edu.anurag.auconnect.model.HelpItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelpItemRepository extends JpaRepository<HelpItem, Long> {
    List<HelpItem> findAllByOrderByCreatedAtDesc();
}
