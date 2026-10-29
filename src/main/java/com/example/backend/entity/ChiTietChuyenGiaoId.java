package com.example.backend.entity;

import java.io.Serializable;
import java.util.Objects;

public class ChiTietChuyenGiaoId implements Serializable {

    private String maChuyen;
    private String maKien;

    public ChiTietChuyenGiaoId() {
    }

    public ChiTietChuyenGiaoId(String maChuyen, String maKien) {
        this.maChuyen = maChuyen;
        this.maKien = maKien;
    }

    public String getMaChuyen() {
        return maChuyen;
    }

    public void setMaChuyen(String maChuyen) {
        this.maChuyen = maChuyen;
    }

    public String getMaKien() {
        return maKien;
    }

    public void setMaKien(String maKien) {
        this.maKien = maKien;
    }

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
