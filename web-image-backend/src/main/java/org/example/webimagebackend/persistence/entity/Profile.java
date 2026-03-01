package org.example.webimagebackend.persistence.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Profile {
    private Long id;
    private String username;
    private String passwordHash;
}
