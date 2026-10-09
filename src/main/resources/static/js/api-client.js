/**
 * UTE EXPRESS LOGISTICS - API CLIENT
 * Kết nối giao diện Frontend với Backend Spring Boot thật qua REST APIs
 */

const UteApi = (function() {
    // Tự động nhận diện URL: Nếu chạy qua Spring Boot (http://localhost:8080) thì dùng relative '',
    // Nếu mở trực tiếp file:// trong trình duyệt thì gọi đến http://localhost:8080
    const BASE_URL = window.location.protocol.startsWith('http') ? '' : 'http://localhost:8080';

    async function request(endpoint, options = {}) {
        const url = `${BASE_URL}${endpoint}`;
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...(options.headers || {})
            }
        };

        try {
            const response = await fetch(url, config);
            const resData = await response.json().catch(() => null);

            if (!response.ok) {
                const message = (resData && resData.message) ? resData.message : `Lỗi HTTP ${response.status}`;
                throw new Error(message);
            }

            return resData;
        } catch (error) {
            console.error(`API Error [${options.method || 'GET'} ${endpoint}]:`, error);
            throw error;
        }
    }

    return {
        // ========================== ĐIỀU PHỐI (NVDP) ==========================

        // 1. Dashboard
        getDispatcherDashboard: () => request('/api/dieu-phoi/dashboard'),
        getPendingDispatches: () => request('/api/dieu-phoi/don-can-dieu-phoi'),
        getRunningTrips: () => request('/api/dieu-phoi/chuyen-dang-chay'),
        getAvailableDrivers: () => request('/api/dieu-phoi/tai-xe-kha-dung'),
        getAvailableVehicles: () => request('/api/dieu-phoi/phuong-tien-kha-dung'),

        // 2. Tuyến vận chuyển
        getRoutes: () => request('/api/dieu-phoi/tuyen'),
        getRouteById: (maTuyen) => request(`/api/dieu-phoi/tuyen/${encodeURIComponent(maTuyen)}`),
        createRoute: (routeData) => request('/api/dieu-phoi/tuyen', {
            method: 'POST',
            body: JSON.stringify(routeData)
        }),
        updateRoute: (maTuyen, routeData) => request(`/api/dieu-phoi/tuyen/${encodeURIComponent(maTuyen)}`, {
            method: 'PUT',
            body: JSON.stringify(routeData)
        }),
        deleteRoute: (maTuyen) => request(`/api/dieu-phoi/tuyen/${encodeURIComponent(maTuyen)}`, {
            method: 'DELETE'
        }),

        // 3. Danh mục kho, tài xế, phương tiện
        getWarehouses: () => request('/api/dieu-phoi/kho'),
        getDrivers: () => request('/api/dieu-phoi/tai-xe'),
        getVehicles: () => request('/api/dieu-phoi/phuong-tien'),
        getAvailablePackages: (maKho) => {
            const query = maKho ? `?maKho=${encodeURIComponent(maKho)}` : '';
            return request(`/api/dieu-phoi/kien-hang-kha-dung${query}`);
        },

        // 4. Quản lý chuyến giao
        getTrips: (maNvDieuPhoi) => {
            const query = maNvDieuPhoi ? `?maNvDieuPhoi=${encodeURIComponent(maNvDieuPhoi)}` : '';
            return request(`/api/dieu-phoi/chuyen-giao${query}`);
        },
        getDetailedTrips: () => request('/api/dieu-phoi/chuyen-giao-chi-tiet'),
        getTripPackages: (maChuyen) => request(`/api/dieu-phoi/chuyen-giao/${encodeURIComponent(maChuyen)}/chi-tiet`),
        createTrip: (tripData) => request('/api/dieu-phoi/chuyen-giao', {
            method: 'POST',
            body: JSON.stringify(tripData)
        }),
        updateTrip: (maChuyen, updateData) => request(`/api/dieu-phoi/chuyen-giao/${encodeURIComponent(maChuyen)}`, {
            method: 'PUT',
            body: JSON.stringify(updateData)
        }),
        cancelTrip: (maChuyen, maNd) => {
            const query = maNd ? `?maNd=${encodeURIComponent(maNd)}` : '';
            return request(`/api/dieu-phoi/chuyen-giao/${encodeURIComponent(maChuyen)}/huy${query}`, {
                method: 'POST'
            });
        },
        deleteTrip: (maChuyen, maNd) => {
            const query = maNd ? `?maNd=${encodeURIComponent(maNd)}` : '';
            return request(`/api/dieu-phoi/chuyen-giao/${encodeURIComponent(maChuyen)}${query}`, {
                method: 'DELETE'
            });
        },
        assignPackageToTrip: (assignData) => request('/api/dieu-phoi/chuyen-giao/gan-kien', {
            method: 'POST',
            body: JSON.stringify(assignData)
        }),

        // ========================== TÀI XẾ (DRIVER) ==========================

        // 1. Dashboard
        getDriverDashboard: (maTx) => {
            const query = maTx ? `?maTx=${encodeURIComponent(maTx)}` : '';
            return request(`/api/tai-xe/dashboard${query}`);
        },

        // 2. Chuyến giao của tài xế
        getDriverTrips: (maTx, trangThai) => {
            let url = `/api/tai-xe/chuyen-giao?maTx=${encodeURIComponent(maTx)}`;
            if (trangThai) {
                url += `&trangThai=${encodeURIComponent(trangThai)}`;
            }
            return request(url);
        },
        getDriverAssignedTrips: (maTx) => request(`/api/tai-xe/chuyen-phan-cong?maTx=${encodeURIComponent(maTx)}`),
        getDriverPendingPackages: (maTx) => request(`/api/tai-xe/kien-can-giao?maTx=${encodeURIComponent(maTx)}`),
        getDriverDeliveredPackages: (maTx) => request(`/api/tai-xe/kien-da-giao?maTx=${encodeURIComponent(maTx)}`),
        getDriverTripDetail: (maChuyen) => request(`/api/tai-xe/chuyen-giao/${encodeURIComponent(maChuyen)}/chi-tiet-day-du`),
        getDriverTripPackagesFull: (maChuyen) => request(`/api/tai-xe/chuyen-giao/${encodeURIComponent(maChuyen)}/kien-hang`),

        // 3. Thao tác chuyến & kiện
        scanPackage: (scanData) => request('/api/tai-xe/quet-kien', {
            method: 'POST',
            body: JSON.stringify(scanData)
        }),
        startTrip: (tripData) => request('/api/tai-xe/xuat-phat', {
            method: 'POST',
            body: JSON.stringify(tripData)
        }),
        deliverSuccess: (successData) => request('/api/tai-xe/giao-thanh-cong', {
            method: 'POST',
            body: JSON.stringify(successData)
        }),
        deliverFailed: (failedData) => request('/api/tai-xe/giao-that-bai', {
            method: 'POST',
            body: JSON.stringify(failedData)
        }),
        completeTrip: (maChuyen, maNd) => {
            const query = maNd ? `?maNd=${encodeURIComponent(maNd)}` : '';
            return request(`/api/tai-xe/chuyen-giao/${encodeURIComponent(maChuyen)}/hoan-thanh${query}`, {
                method: 'POST'
            });
        }
    };
})();
