//package org.mafiagame.mafia.entity;
//
//import javax.persistence.*;
//import java.util.Objects;
//
//@Entity
//@Table(name = "player")
//public class PlayerEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    private String name;
//    private String role;
//
//    public PlayerEntity() {
//
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getRole() {
//        return role;
//    }
//
//    public void setRole(String role) {
//        this.role = role;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        PlayerEntity that = (PlayerEntity) o;
//        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(role, that.role);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id, name, role);
//    }
//}
