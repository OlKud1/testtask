package com.example.demo.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationUtils {

    public static <S> Specification<S> iLike(String field, String value) {
        if (value == null || value.isBlank()) {
            return Specification.where(null);
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
    }

    public static <S, T extends Comparable<? super T>> Specification<S> gte(String field, T value) {
        if (value == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(field), value);
    }

    public static <S, T extends Comparable<? super T>> Specification<S> lte(String field, T value) {
        if (value == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(field), value);
    }

    public static <S, T> Specification<S> in(String field, Collection<? extends T> values) {
        if (values == null || values.isEmpty()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> root.get(field).in(values);
    }
}
