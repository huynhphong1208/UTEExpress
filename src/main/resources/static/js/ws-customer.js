/**
 * ws-customer.js — Real-time WebSocket Client & Notification Popover cho Customer Dashboard (UTE Express)
 *
 * Chức năng:
 * - Nạp lịch sử thông báo từ REST API /api/khach-hang/thong-bao
 * - Kết nối WebSocket STOMP qua /ws-uteexpress, lắng nghe /topic/customer/{maKh}
 * - Khi có thông báo mới:
 *   1. Hiển thị Toast góc màn hình
 *   2. Phát âm thanh "Ting" (Web Audio API)
 *   3. Tăng số đếm ở chuông thông báo (Notification Bell +1)
 *   4. Chèn trực tiếp lên ĐẦU khung dropdown thông báo (mới nhất -> cũ dần)
 *   5. Cập nhật dòng đơn hàng trên bảng và thẻ thống kê
 * - Dropdown popover: Hiển thị tối đa 5 thông báo trước khi cuộn chuột (scroll),
 *   Click vào thông báo sẽ chuyển sang trang chi tiết đơn hàng /customer/order-detail?maDh=...
 */

let customerWsClient = null;
let customerWsSubscription = null;
let customerReconnectTimer = null;
let currentCustomerMaKh = null;
let unreadNotificationCount = 0;
let loadedNotifications = [];

/**
 * Bật / tắt hiển thị Dropdown Popover thông báo
 */
function toggleNotificationDropdown(event) {
    if (event) {
        event.stopPropagation();
    }
    const dropdown = document.getElementById('notifDropdown');
    if (!dropdown) return;

    const isOpen = dropdown.style.display === 'block';
    if (isOpen) {
        dropdown.style.display = 'none';
    } else {
        dropdown.style.display = 'block';
        // Khi mở dropdown, xóa badge số đếm đỏ
        markAllNotificationsAsRead();
    }
}

/**
 * Đóng dropdown khi click ra ngoài màn hình
 */
document.addEventListener('click', function (e) {
    const wrapper = document.getElementById('notifBellWrapper');
    const dropdown = document.getElementById('notifDropdown');
    if (dropdown && dropdown.style.display === 'block') {
        if (wrapper && !wrapper.contains(e.target)) {
            dropdown.style.display = 'none';
        }
    }
});

/**
 * Đánh dấu đã đọc tất cả thông báo
 */
function markAllNotificationsAsRead(event) {
    if (event) event.stopPropagation();
    unreadNotificationCount = 0;
    const badge = document.getElementById('notifBellBadge');
    if (badge) {
        badge.style.display = 'none';
        badge.innerText = '0';
    }
    // Làm mờ nhẹ nền các item chưa đọc
    const unreadItems = document.querySelectorAll('.notif-item-unread');
    unreadItems.forEach(el => {
        el.classList.remove('notif-item-unread');
        el.style.backgroundColor = '#ffffff';
    });
}

/**
 * Điều hướng sang trang chi tiết đơn hàng khi click vào một thông báo
 */
function goToOrderDetail(maDh) {
    if (!maDh) return;
    window.location.href = `/customer/order-detail?maDh=${encodeURIComponent(maDh)}`;
}

/**
 * Tạo HTML element cho một record thông báo
 */
function createNotificationItemHtml(item, isUnread = false) {
    const maDh = item.maDh || '';
    const tenTrangThai = item.tenTrangThai || 'Cập nhật trạng thái';
    const ghiChu = item.ghiChu || `Đơn hàng đã chuyển sang trạng thái: ${tenTrangThai}`;
    const timeStr = item.thoiGianDisplay || (item.thoiGian ? new Date(item.thoiGian).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : 'Vừa xong');
    const bgStyle = isUnread ? 'background-color: #F0FDF4;' : 'background-color: #ffffff;';

    return `
        <div class="notif-item ${isUnread ? 'notif-item-unread' : ''}" 
             onclick="goToOrderDetail('${maDh}')"
             style="${bgStyle} padding: 12px 14px; border-bottom: 1px solid #F1F5F9; display: flex; gap: 12px; align-items: flex-start; cursor: pointer; transition: background-color 0.2s;"
             onmouseover="this.style.backgroundColor='#F8FAFC'" 
             onmouseout="this.style.backgroundColor='${isUnread ? '#F0FDF4' : '#ffffff'}'">
            
            <div style="width: 32px; height: 32px; border-radius: 50%; background: #EFF6FF; color: var(--primary-blue, #0033A0); display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 2px;">
                <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
                </svg>
            </div>
            
            <div style="flex: 1; min-width: 0;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2px;">
                    <strong style="color: var(--primary-blue, #0033A0); font-size: 13px;">${maDh}</strong>
                    <span style="font-size: 11px; color: #94A3B8;">${timeStr}</span>
                </div>
                <div style="font-size: 12px; font-weight: 600; color: #1E293B; margin-bottom: 2px;">
                    ${tenTrangThai}
                </div>
                <div style="font-size: 12px; color: #64748B; line-height: 1.35; white-space: normal; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                    ${ghiChu}
                </div>
            </div>
        </div>
    `;
}

/**
 * Nạp danh sách thông báo lịch sử từ API
 */
async function loadNotificationHistory() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
        const res = await fetch('/api/khach-hang/thong-bao?limit=20', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!res.ok) return;

        const data = await res.json();
        if (data.success && Array.isArray(data.data)) {
            loadedNotifications = data.data;
            renderNotificationList();
        }
    } catch (err) {
        console.debug('Lỗi tải lịch sử thông báo:', err);
    }
}

/**
 * Render toàn bộ danh sách thông báo vào khung cuộn Dropdown
 */
function renderNotificationList() {
    const container = document.getElementById('notifListContainer');
    const totalBadge = document.getElementById('notifTotalBadge');
    if (!container) return;

    if (!loadedNotifications || loadedNotifications.length === 0) {
        container.innerHTML = `
            <div id="notifEmptyState" style="padding: 36px 16px; text-align: center; color: #94A3B8; font-size: 13px;">
                <svg width="32" height="32" fill="none" stroke="currentColor" viewBox="0 0 24 24" style="margin: 0 auto 8px; opacity: 0.5;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg>
                Chưa có thông báo đơn hàng nào.
            </div>
        `;
        if (totalBadge) totalBadge.innerText = '0';
        return;
    }

    if (totalBadge) totalBadge.innerText = loadedNotifications.length;

    container.innerHTML = loadedNotifications
        .map(item => createNotificationItemHtml(item, false))
        .join('');
}

/**
 * Thêm một thông báo mới Real-time lên ĐẦU danh sách dropdown
 */
function prependNotificationItem(item) {
    const container = document.getElementById('notifListContainer');
    const totalBadge = document.getElementById('notifTotalBadge');
    if (!container) return;

    const empty = document.getElementById('notifEmptyState');
    if (empty) empty.remove();

    const itemHtml = createNotificationItemHtml(item, true);
    container.insertAdjacentHTML('afterbegin', itemHtml);

    // Cập nhật số tổng
    if (totalBadge) {
        const cur = parseInt(totalBadge.innerText || '0', 10);
        totalBadge.innerText = cur + 1;
    }

    // Đẩy vào đầu mảng lưu trữ
    loadedNotifications.unshift(item);
}

/**
 * Cập nhật số đếm chuông thông báo (Bell Badge)
 */
function updateNotificationBell(increment = 1) {
    unreadNotificationCount += increment;
    const badge = document.getElementById('notifBellBadge');
    if (badge) {
        if (unreadNotificationCount > 0) {
            badge.innerText = unreadNotificationCount > 99 ? '99+' : unreadNotificationCount;
            badge.style.display = 'inline-flex';
        } else {
            badge.style.display = 'none';
        }
    }
}

/**
 * Âm thanh Ting bằng Web Audio API
 */
function playCustomerNotificationSound() {
    try {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtx) return;
        const ctx = new AudioCtx();
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();

        osc.connect(gain);
        gain.connect(ctx.destination);

        osc.type = 'sine';
        osc.frequency.setValueAtTime(880, ctx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(1320, ctx.currentTime + 0.12);

        gain.gain.setValueAtTime(0.2, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);

        osc.start(ctx.currentTime);
        osc.stop(ctx.currentTime + 0.35);
    } catch (e) {
        console.debug('Không thể phát âm thanh thông báo:', e);
    }
}

/**
 * Hiển thị Toast nổi góc màn hình
 */
function showCustomerToast(title, message, maDh) {
    let container = document.getElementById('customerToastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'customerToastContainer';
        container.style.cssText = `
            position: fixed;
            bottom: 24px;
            right: 24px;
            z-index: 99999;
            display: flex;
            flex-direction: column;
            gap: 12px;
            max-width: 380px;
            pointer-events: none;
        `;
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'customer-ws-toast';
    toast.style.cssText = `
        background: #ffffff;
        border-radius: 10px;
        box-shadow: 0 10px 25px rgba(0, 51, 160, 0.18), 0 4px 10px rgba(0,0,0,0.06);
        border-left: 4px solid var(--primary-blue, #0033A0);
        padding: 14px 16px;
        color: #1e293b;
        pointer-events: auto;
        transform: translateY(20px);
        opacity: 0;
        transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
        display: flex;
        align-items: flex-start;
        gap: 12px;
        cursor: pointer;
    `;

    toast.innerHTML = `
        <div style="width: 36px; height: 36px; border-radius: 50%; background: #EFF6FF; display: flex; align-items: center; justify-content: center; flex-shrink: 0; color: var(--primary-blue, #0033A0);">
            <svg width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
            </svg>
        </div>
        <div style="flex: 1; min-width: 0;" onclick="goToOrderDetail('${maDh}')">
            <div style="font-weight: 700; font-size: 14px; color: #0f172a; margin-bottom: 2px;">${title}</div>
            <div style="font-size: 13px; color: #475569; line-height: 1.4;">${message}</div>
            ${maDh ? `<span style="display: inline-block; margin-top: 6px; font-size: 12px; font-weight: 600; color: var(--primary-blue, #0033A0);">Bấm để xem chi tiết &rarr;</span>` : ''}
        </div>
        <button type="button" onclick="event.stopPropagation(); this.parentElement.remove()" style="background: none; border: none; color: #94a3b8; cursor: pointer; padding: 2px; font-size: 16px; line-height: 1;">&times;</button>
    `;

    container.appendChild(toast);

    requestAnimationFrame(() => {
        toast.style.transform = 'translateY(0)';
        toast.style.opacity = '1';
    });

    setTimeout(() => {
        toast.style.transform = 'translateY(20px)';
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 6000);
}

/**
 * Xử lý khi nhận được bản tin cập nhật trạng thái đơn hàng qua WebSocket
 */
function handleCustomerOrderUpdate(data) {
    console.log('⚡ WebSocket nhận cập nhật đơn hàng:', data);

    const maDh = data.maDh || '';
    const tenTrangThaiMoi = data.tenTrangThaiMoi || data.maTrangThaiMoi || 'Cập nhật trạng thái';
    const ghiChu = data.ghiChu || `Đơn hàng đã đổi sang trạng thái: ${tenTrangThaiMoi}`;

    // 1. Âm thanh thông báo
    playCustomerNotificationSound();

    // 2. Hiển thị Toast
    showCustomerToast(
        `Đơn hàng #${maDh}`,
        `Trạng thái mới: <strong>${tenTrangThaiMoi}</strong>${data.tenKhoHienTai ? `<br><small style="color:#64748b">Vị trí: ${data.tenKhoHienTai}</small>` : ''}`,
        maDh
    );

    // 3. Tăng chuông thông báo
    updateNotificationBell(1);

    // 4. Chèn vào ĐẦU danh sách dropdown
    prependNotificationItem({
        maDh: maDh,
        tenTrangThai: tenTrangThaiMoi,
        ghiChu: ghiChu,
        thoiGianDisplay: 'Vừa xong'
    });

    // 5. Cập nhật trực tiếp dòng đơn hàng trên bảng nếu có
    const tbody = document.getElementById('ordersTbody');
    if (tbody) {
        const rows = tbody.querySelectorAll('tr');
        let found = false;

        rows.forEach(row => {
            const firstCol = row.querySelector('td strong');
            if (firstCol && firstCol.innerText.trim() === maDh) {
                found = true;
                const cells = row.querySelectorAll('td');
                if (cells.length >= 7) {
                    const badgeCell = cells[6];
                    badgeCell.innerHTML = `<span class="badge badge-info" style="animation: pulseBadge 1.5s 2;"><span class="badge-dot"></span>${tenTrangThaiMoi}</span>`;
                }
                row.style.transition = 'background-color 0.4s ease';
                row.style.backgroundColor = '#FEF3C7';
                setTimeout(() => {
                    row.style.backgroundColor = '';
                }, 3000);
            }
        });

        if (!found && typeof loadMyOrders === 'function') {
            loadMyOrders();
        }
    }
}

/**
 * Kết nối WebSocket tới channel /topic/customer/{maKh}
 */
function connectCustomerWebSocket(maKh) {
    if (!maKh) {
        console.warn('⚠️ WebSocket: Chưa có mã khách hàng để subscribe.');
        return;
    }

    if (customerWsClient && customerWsClient.connected && currentCustomerMaKh === maKh) {
        return;
    }

    currentCustomerMaKh = maKh;

    try {
        const socket = new SockJS('/ws-uteexpress');
        customerWsClient = Stomp.over(socket);
        customerWsClient.debug = null;

        customerWsClient.connect({}, function (frame) {
            console.log('✅ WebSocket: Khách hàng [' + maKh + '] đã kết nối thành công!');

            const topic = '/topic/customer/' + maKh;
            customerWsSubscription = customerWsClient.subscribe(topic, function (msg) {
                try {
                    const data = JSON.parse(msg.body);
                    handleCustomerOrderUpdate(data);
                } catch (err) {
                    console.error('Lỗi parse WebSocket message:', err);
                }
            });

            const dot = document.getElementById('customerWsIndicator');
            if (dot) {
                dot.style.background = '#10B981';
                dot.title = 'Real-time đã kết nối';
            }
        }, function (error) {
            console.warn('⚠️ WebSocket lỗi / ngắt kết nối. Thử lại sau 5s...', error);
            const dot = document.getElementById('customerWsIndicator');
            if (dot) {
                dot.style.background = '#EF4444';
                dot.title = 'Mất kết nối real-time';
            }
            if (customerReconnectTimer) clearTimeout(customerReconnectTimer);
            customerReconnectTimer = setTimeout(() => {
                connectCustomerWebSocket(currentCustomerMaKh);
            }, 5000);
        });
    } catch (e) {
        console.error('Không thể tạo kết nối WebSocket:', e);
    }
}

/**
 * Khởi tạo dữ liệu và WebSocket khi mở Dashboard
 */
async function initCustomerRealtime() {
    // 1. Tải trước danh sách thông báo lịch sử
    loadNotificationHistory();

    // 2. Tìm mã khách hàng để kích hoạt Real-time
    let maKh = localStorage.getItem('maKh');

    const token = localStorage.getItem('accessToken');
    if (!maKh && token) {
        try {
            const res = await fetch('/api/khach-hang/profile', {
                headers: { 'Authorization': 'Bearer ' + token }
            });
            if (res.ok) {
                const json = await res.json();
                if (json && json.data && json.data.maKh) {
                    maKh = json.data.maKh;
                    localStorage.setItem('maKh', maKh);
                }
            }
        } catch (err) {
            console.debug('Chưa lấy được profile khách qua API:', err);
        }
    }

    if (!maKh) {
        const el = document.querySelector('[data-makh]');
        if (el) maKh = el.getAttribute('data-makh');
    }

    if (maKh) {
        connectCustomerWebSocket(maKh);
    }
}

// Khởi chạy khi DOM sẵn sàng
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCustomerRealtime);
} else {
    initCustomerRealtime();
}
