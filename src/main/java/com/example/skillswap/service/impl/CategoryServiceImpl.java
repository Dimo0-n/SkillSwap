package com.example.skillswap.service.impl;

import com.example.skillswap.config.CacheConfig;
import com.example.skillswap.entity.Category;
import com.example.skillswap.repository.CategoryRepository;
import com.example.skillswap.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	@Cacheable(cacheNames = CacheConfig.CATEGORY_ALL_CACHE, key = "'all'")
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@Override
	@Cacheable(cacheNames = CacheConfig.CATEGORY_BY_ID_CACHE, key = "#id")
	public Optional<Category> getCategoryById(Long id) {
		return categoryRepository.findById(id);
	}

}
