package com.example.j.upa.DAO;


import android.widget.EditText;

public class Checker {
    public boolean Nullcheck (EditText test){
        return test.getText().toString().equals("");
    }
}
