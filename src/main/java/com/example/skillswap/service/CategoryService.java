package com.example.skillswap.service;

import com.example.skillswap.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

	List<Category> getAllCategories();

	Optional<Category> getCategoryById(Long id);
}
