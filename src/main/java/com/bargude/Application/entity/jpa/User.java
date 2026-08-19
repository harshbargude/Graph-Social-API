package com.bargude.Application.entity.jpa;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Enter valid username")
    @Column(nullable = false, unique = true)
    private String username;

    @Size(min = 4, max = 255, message = "Password must be between 4 and 255 characters")
    @Column(nullable = false)
    private String password;

    private String role;

    @ManyToMany
    @JoinTable(
            name = "user_follows",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "followed_id")
    )
    private List<User> following ;

    public User(){

    }

    public List<User> getFollowing() {
        return following;
    }

    public void setFollowing(User theFollowing) {
        if(following == null){
            following = new ArrayList<User>();
        }
        following.add(theFollowing);
    }

    public User(Long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public @NotBlank(message = "Enter valid username") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Enter valid username") String username) {
        this.username = username;
    }

    public @Size(min = 4, max = 255, message = "Password must be between 4 and 255 characters") String getPassword() {
        return password;
    }

    public void setPassword(@Size(min = 4, max = 255, message = "Password must be between 4 and 255 characters") String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
