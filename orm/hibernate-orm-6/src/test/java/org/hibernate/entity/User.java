package org.hibernate.entity;

import jakarta.persistence.*;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.text.MessageFormat.format;
import static java.util.Objects.hash;

@NamedEntityGraph(
        name = "user-entity-graph",
        attributeNodes = {
                @NamedAttributeNode(value = "detail")
        }
)
@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name")
    private String name;

    @OneToOne(mappedBy = "user", fetch = LAZY)
    private UserDetail detail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserDetail getDetail() {
        return detail;
    }

    public void setDetail(UserDetail detail) {
        this.detail = detail;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User that = (User) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return hash(id);
    }

    @Override
    public String toString() {
        return format("User(id={0}, name={1}, detail={2}, skills=[{3}])", id, name, detail);
    }
}
