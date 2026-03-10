package org.example.webimagebackend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;
}
