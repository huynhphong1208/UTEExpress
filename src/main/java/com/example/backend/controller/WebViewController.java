package com.example.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller điều hướng giao diện Web (Thymeleaf views)
 */
@Controller
public class WebViewController {

    // Trang chủ
    @GetMapping("/")
    public String index() {
        return "guest/03-index";
    }

    // Trang Đăng nhập (UC02)
    @GetMapping("/login")
    public String login() {
        return "guest/02-login";
    }

    // Trang Đăng ký (UC01)
    @GetMapping("/register")
    public String register() {
        return "guest/01-register";
    }

    // Trang Tra cứu đơn hàng công khai (UC03)
    @GetMapping("/tracking")
    public String tracking(@RequestParam(required = false) String maDh, Model model) {
        if (maDh != null && !maDh.isBlank()) {
            model.addAttribute("maDh", maDh);
        }
        return "guest/04-tracking";
    }

    // Trang Khách hàng - Tổng quan & Đơn hàng của tôi (UC05)
    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer/05-dashboard";
    }

    // Trang Khách hàng - Tạo đơn hàng mới (UC04)
    @GetMapping("/customer/create-order")
    public String customerCreateOrder() {
        return "customer/06-create-order";
    }

    // Trang Admin - Quản lý tài khoản người dùng (UC15)
    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/19-users";
    }
}
