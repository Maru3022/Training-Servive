package com.example.training_service.specification;

import com.example.training_service.model.Training;
import com.example.training_service.model.TrainingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TrainingSpecification {

    private TrainingSpecification() {
    }

    public static Specification<Training> filter(UUID userId, TrainingStatus status,
                                                  LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("user_id"), userId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("training_status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("training_date"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("training_date"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
