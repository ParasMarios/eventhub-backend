package com.paraske.EventHub.repository;

import com.paraske.EventHub.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Βρίσκει τις κύριες κατηγορίες (αυτές που δεν έχουν γονιό)
    List<Category> findByParentIsNull();

    // Βρίσκει τις υποκατηγορίες ενός συγκεκριμένου γονιού
    List<Category> findByParent(Category parent);

    Optional<Category> findByName(String name);
}
