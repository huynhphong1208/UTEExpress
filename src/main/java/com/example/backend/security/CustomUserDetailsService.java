package com.example.backend.security;

import com.example.backend.entity.NguoiDung;
import com.example.backend.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Load thông tin User từ database cho Spring Security
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản: " + username));

        return User.builder()
                .username(nd.getTenDangNhap())
                .password(nd.getMatKhau())
                .authorities(List.of(new SimpleGrantedAuthority(nd.getVaiTro().getMaVaiTro())))
                .accountLocked("Bị khóa".equals(nd.getTrangThai()))
                .build();
    }
}
