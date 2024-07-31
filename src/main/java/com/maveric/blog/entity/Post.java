package com.maveric.blog.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private String title;

    private String content;

    @ManyToOne
    @JoinColumn(name = "authorId")
    private User author;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    private boolean published = false;
    private boolean featured = false;
    private boolean draft = true;


}

