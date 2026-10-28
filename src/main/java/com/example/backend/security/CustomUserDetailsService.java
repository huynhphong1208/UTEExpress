package com.example.backend.security;

import com.example.backend.entity.NguoiDung;
import com.example.backend.repository.NguoiDungRepository;
import com.example.backend.repository.KhachHangRepository;
import com.example.backend.repository.NhanVienRepository;
import com.example.backend.repository.TaiXeRepository;
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
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;
    private final TaiXeRepository taiXeRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NguoiDung nd = nguoiDungRepository.findByTenDangNhap(username)
                .or(() -> nhanVienRepository.findBySdt(username).map(com.example.backend.entity.NhanVien::getNguoiDung))
                .or(() -> khachHangRepository.findBySdt(username).map(com.example.backend.entity.KhachHang::getNguoiDung))
                .or(() -> taiXeRepository.findBySdt(username).map(com.example.backend.entity.TaiXe::getNguoiDung))
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
