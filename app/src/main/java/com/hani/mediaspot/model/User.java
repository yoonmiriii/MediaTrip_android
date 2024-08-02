package com.hani.mediaspot.model;

public class User {

    public String email;
    public String password;

    public String name;
    public boolean gender;



    public User(){

    }


    // 회원가입
    public User(String name, String email, String password, boolean gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
    }

    // 로그인
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // 정보수정
    public User(String name, String password, boolean gender) {

        this.name = name;
        this.password = password;
        this.gender = gender;

    }
}
