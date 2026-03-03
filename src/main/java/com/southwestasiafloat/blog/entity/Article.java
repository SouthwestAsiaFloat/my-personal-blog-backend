package com.southwestasiafloat.blog.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "article") // 对应数据库中的表名
public class Article {

    // 主键 id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 文章标题
    @Column(nullable = false)
    private String title;

    // 文章内容（TEXT）
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    // 摘要
    @Column(length = 512)
    private String summary;

    // 关联的用户 id（数据库列 user_id）
    @Column(name = "user_id")
    private Long userId;

    // 文章分类 id（数据库列 category_id）
    @Column(name = "category_id")
    private Long categoryId;

    // 状态，例如 published/draft
    @Column(nullable = false)
    private String status = "published";

    // 创建时间，对应数据库 create_time
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    // 更新时间，对应数据库 update_time
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // 逻辑删除标记，对应数据库 is_deleted（0/1）
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.updateTime = now;
        if (this.isDeleted == null) this.isDeleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    // Getter/Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
