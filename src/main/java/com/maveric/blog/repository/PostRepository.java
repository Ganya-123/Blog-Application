package com.maveric.blog.repository;

import com.maveric.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCategoryId(Long categoryId);

    List<Post> findByPublishedTrue();

    List<Post> findByFeaturedTrue();
}