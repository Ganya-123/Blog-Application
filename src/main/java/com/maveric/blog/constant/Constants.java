package com.maveric.blog.constant;

public class Constants {
    public static final String AUTHOR_VALIDATION_FAILED =
            "The author ID from the request does not match the ID extracted from the token";
    public static final String AUTHOR_NOT_FOUND = "Author Not Found";
    public static final String CATEGORY_NOT_FOUND = "Category Not Found";
    public static final String POST_NOT_FOUND = "Post Not Found";
    public static final String AUTHOR_IS_DIFFERENT =
            "The author of the post does not match the provided authorId";
    public static final String COMMENT_NOT_FOUND = "Comment Not Found";
    public static final String PARENT_COMMENT_NOT_FOUND = "Parent comment Not Found";
    public static final String POST_ALREADY_PUBLISHED = "Post Already in Published State";
    public static final String POST_ALREADY_UNPUBLISHED = "Post Already in Un-Published State";
    public static final String POST_NOT_PUBLISHED = "Post Not Published, you cannot add a comment";
    public static final String PARENT_COMMENT_NOT_BELONGS_TO_POST =
            "Parent comment doesn't belong to the post";
    public static final String POST_DELETE_SUCCESS = "Post deleted successfully";

    public static final String CATEGORY_EXISTS = "Category already present";
    public static final String CATEGORY_DELETE_SUCCESS = "Category deleted successfully";
    public static final String EMAIL_EXISTS = "Email Already Registered";
    public static final String PASSWORD_CHANGE_SUCCESS = "Password Changed Successfully";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset is successful";
    public static final String COMMENT_DELETE_SUCCESS = "Comment deleted successfully";
    public static final String CATEGORY_ALREADY_PRESENT = "Category Already Present";
}
