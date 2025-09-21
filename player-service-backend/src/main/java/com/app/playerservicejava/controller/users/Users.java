package com.app.playerservicejava.controller.users;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Users implements Serializable {
    private List<User> users;

    public Users() {
        this.users = new ArrayList<>();
    }

}
