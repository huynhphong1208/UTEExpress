package com.example.backend.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ChiTietChuyenGiaoId implements Serializable {

    private String maChuyen;
    private String maKien;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietChuyenGiaoId that = (ChiTietChuyenGiaoId) o;
        return Objects.equals(maChuyen, that.maChuyen) && Objects.equals(maKien, that.maKien);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maChuyen, maKien);
    }
}
