package com.bank;

import com.bank.controller.BankController;

public class Main {
    public static void main(String[] args) {
        BankController controller = new BankController();
        controller.start();
    }
}