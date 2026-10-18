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

        const token = localStorage.getItem('accessToken') || localStorage.getItem('token');
        if (token) {
            defaultHeaders['Authorization'] = `Bearer ${token}`;
        }

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
        getPendingDispatches: (options = {}) => {
            if (typeof options !== 'object' || options === null) options = {};
            const params = new URLSearchParams();
            if (options.page) params.append('page', options.page);
            if (options.size) params.append('size', options.size);
            if (options.maKho) params.append('maKho', options.maKho);
            if (options.loaiChuyen) params.append('loaiChuyen', options.loaiChuyen);
            if (options.maTuyen) params.append('maTuyen', options.maTuyen);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/dieu-phoi/don-can-dieu-phoi${query}`);
        },
        getRunningTrips: (page, size) => {
            const params = new URLSearchParams();
            if (page) params.append('page', page);
            if (size) params.append('size', size);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/dieu-phoi/chuyen-dang-chay${query}`);
        },
        getAvailableDrivers: (page, size) => {
            const params = new URLSearchParams();
            if (page) params.append('page', page);
            if (size) params.append('size', size);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/dieu-phoi/tai-xe-kha-dung${query}`);
        },
        getAvailableVehicles: (page, size) => {
            const params = new URLSearchParams();
            if (page) params.append('page', page);
            if (size) params.append('size', size);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/dieu-phoi/phuong-tien-kha-dung${query}`);
        },

        // 2. Tuyến vận chuyển
        getRoutes: (page, size, search) => {
            const params = new URLSearchParams();
            if (page) params.append('page', page);
            if (size) params.append('size', size);
            if (search) params.append('search', search);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/dieu-phoi/tuyen${query}`);
        },
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
        getAvailablePackages: (maKho, loaiChuyen, maTuyen) => {
            const params = new URLSearchParams();
            if (maKho) params.append('maKho', maKho);
            if (loaiChuyen) params.append('loaiChuyen', loaiChuyen);
            if (maTuyen) params.append('maTuyen', maTuyen);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/dieu-phoi/kien-hang-kha-dung${query}`);
        },

        // 4. Quản lý chuyến giao
        getTrips: (maNvDieuPhoi, page, size) => {
            const params = new URLSearchParams();
            if (maNvDieuPhoi) params.append('maNvDieuPhoi', maNvDieuPhoi);
            if (page) params.append('page', page);
            if (size) params.append('size', size);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/dieu-phoi/chuyen-giao${query}`);
        },
        getDetailedTrips: (options = {}) => {
            if (typeof options !== 'object' || options === null) options = {};
            const params = new URLSearchParams();
            if (options.page) params.append('page', options.page);
            if (options.size) params.append('size', options.size);
            if (options.trangThai) params.append('trangThai', options.trangThai);
            if (options.search) params.append('search', options.search);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/dieu-phoi/chuyen-giao-chi-tiet${query}`);
        },
        getTripPackages: (maChuyen, page, size) => {
            let url = `/api/dieu-phoi/chuyen-giao/${encodeURIComponent(maChuyen)}/chi-tiet`;
            if (page && size) url += `?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;
            return request(url);
        },
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
        getDriverTrips: (maTx, trangThai, page, size) => {
            const params = new URLSearchParams();
            if (maTx) params.append('maTx', maTx);
            if (trangThai) params.append('trangThai', trangThai);
            if (page) params.append('page', page);
            if (size) params.append('size', size);
            return request(`/api/tai-xe/chuyen-giao?${params.toString()}`);
        },
        getDriverAssignedTrips: (maTx, page, size, trangThai) => {
            const params = new URLSearchParams();
            if (maTx) params.append('maTx', maTx);
            if (trangThai) params.append('trangThai', trangThai);
            if (page) params.append('page', page);
            if (size) params.append('size', size);
            return request(`/api/tai-xe/chuyen-phan-cong?${params.toString()}`);
        },
        getDriverPendingPackages: (maTx, page, size) => {
            const params = new URLSearchParams();
            if (maTx) params.append('maTx', maTx);
            if (page) params.append('page', page);
            if (size) params.append('size', size);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/tai-xe/kien-can-giao${query}`);
        },
        getDriverDeliveredPackages: (maTx, page, size) => {
            const params = new URLSearchParams();
            if (maTx) params.append('maTx', maTx);
            if (page) params.append('page', page);
            if (size) params.append('size', size);
            const query = params.toString() ? `?${params.toString()}` : '';
            return request(`/api/tai-xe/kien-da-giao${query}`);
        },
        getDriverTripDetail: (maChuyen) => request(`/api/tai-xe/chuyen-giao/${encodeURIComponent(maChuyen)}/chi-tiet-day-du`),
        getDriverTripPackagesFull: (maChuyen, page, size) => {
            let url = `/api/tai-xe/chuyen-giao/${encodeURIComponent(maChuyen)}/kien-hang`;
            if (page && size) url += `?page=${encodeURIComponent(page)}&size=${encodeURIComponent(size)}`;
            return request(url);
        },

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
