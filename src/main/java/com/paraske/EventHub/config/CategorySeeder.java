package com.paraske.EventHub.config;

import com.paraske.EventHub.model.Category;
import com.paraske.EventHub.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CategorySeeder implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Αν υπάρχουν ήδη κατηγορίες, μην κάνεις τίποτα
        if (categoryRepository.count() > 0) {
            return;
        }

        System.out.println("Εισαγωγή Ταξονομίας Κατηγοριών στη βάση...");

        // 1. Cultural and Entertainment Events
        Category cultural = createCategory("Cultural and Entertainment Events", null);
        createCategory("Music Events", cultural);
        createCategory("Performance Arts", cultural);
        createCategory("Arts & Culture", cultural);
        createCategory("Entertainment", cultural);

        // 2. Social and Private Events
        Category social = createCategory("Social and Private Events", null);
        createCategory("Ceremonies & Milestones", social);
        createCategory("Parties & Receptions", social);
        createCategory("Reunions", social);

        // 3. Professional and Corporate Events
        Category professional = createCategory("Professional and Corporate Events", null);
        createCategory("Meetings & Conferences", professional);
        createCategory("Professional Development", professional);
        createCategory("Networking & Trade", professional);

        // 4. Public and Civic Events
        Category publicCivic = createCategory("Public and Civic Events", null);
        createCategory("Political Events", publicCivic);
        createCategory("Community Events", publicCivic);
        createCategory("Religious Events", publicCivic);

        System.out.println("Η Ταξονομία ολοκληρώθηκε επιτυχώς!");
    }

    private Category createCategory(String name, Category parent) {
        Category category = new Category(name, parent);
        return categoryRepository.save(category);
    }
}
