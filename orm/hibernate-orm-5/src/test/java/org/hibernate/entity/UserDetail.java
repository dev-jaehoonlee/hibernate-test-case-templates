package org.hibernate.entity;

import org.hibernate.annotations.Where;

import javax.persistence.*;

import static java.text.MessageFormat.format;
import static java.util.Objects.hash;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Where(clause = "is_active = true")
@Table(name = "user_details")
@Entity(name = "UserDetail")
public class UserDetail {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "detail_id")
    private Long id;

    @Column(name = "city")
    private String city;

    @Column(name = "is_active")
    private Boolean active;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserDetail that = (UserDetail) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return hash(id);
    }

    @Override
    public String toString() {
        return format("UserDetail(id={0}, city={1}, active={2})", id, city, active);
    }
}
