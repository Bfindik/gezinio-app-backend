package com.example.gezinio.customergroup.model;

import com.example.gezinio.auth.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_group_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_group_member", columnNames = {"group_id", "user_id"})
})
public class CustomerGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private CustomerGroup group;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRelationship relationship;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CustomerGroup getGroup() { return group; }
    public void setGroup(CustomerGroup group) { this.group = group; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public MemberRelationship getRelationship() { return relationship; }
    public void setRelationship(MemberRelationship relationship) { this.relationship = relationship; }

    public LocalDateTime getAddedAt() { return addedAt; }
}
