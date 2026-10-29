/**
 * ws-tracking.js — Module WebSocket Real-time Tracking cho UTE Express
 *
 * Sử dụng STOMP over SockJS để nhận cập nhật trạng thái đơn hàng
 * thời gian thực mà không cần F5 reload trang.
 *
 * Chức năng:
 * - Kết nối WebSocket tới server qua /ws-uteexpress
 * - Subscribe channel /topic/orders/{maDh} theo mã đơn hàng
 * - Tự động cập nhật UI (stepper, timeline, badge)
 * - Phát âm thanh thông báo ngắn "Ting"
 * - Hiển thị toast thông báo trạng thái mới
 * - Auto-reconnect sau 5 giây khi mất kết nối
 */

// === Biến toàn cục quản lý kết nối ===
let stompClient = null;
let currentSubscription = null;
let reconnectTimer = null;
let currentMaDh = null;
let isConnecting = false;

// === Trạng thái kết nối hiển thị trên UI ===
const WS_STATUS = {
    DISCONNECTED: 'disconnected',
    CONNECTING: 'connecting',
    CONNECTED: 'connected'
};

/**
 * Kết nối WebSocket tới server và subscribe theo mã đơn hàng.
 * Nếu đang kết nối đơn khác, sẽ ngắt cũ trước khi kết nối mới.
 *
 * @param {string} maDh - Mã đơn hàng cần theo dõi (VD: "DH000001")
 */
function connectWebSocket(maDh) {
    if (!maDh) {
        console.warn('⚠️ WebSocket: Thiếu mã đơn hàng, không kết nối.');
        return;
    }

    // Ngắt kết nối cũ nếu đang subscribe đơn khác
    disconnectWebSocket();
    currentMaDh = maDh;
    isConnecting = true;
    updateWsStatusIndicator(WS_STATUS.CONNECTING);

    try {
        // Tạo kết nối SockJS tới endpoint server
        const socket = new SockJS('/ws-uteexpress');
        stompClient = Stomp.over(socket);

        // Tắt log debug STOMP trên console (bật lại khi cần debug)
        stompClient.debug = null;

        // Thiết lập kết nối STOMP
        stompClient.connect({}, function (frame) {
            console.log('✅ WebSocket connected:', frame);
            isConnecting = false;
            clearTimeout(reconnectTimer);
            updateWsStatusIndicator(WS_STATUS.CONNECTED);

            // Subscribe channel riêng của đơn hàng đang xem
            currentSubscription = stompClient.subscribe(
                '/topic/orders/' + maDh,
                onMessageReceived
            );
            console.log('📡 Subscribed to /topic/orders/' + maDh);

        }, function (error) {
            // Xử lý lỗi kết nối
            console.warn('❌ WebSocket connection error:', error);
            isConnecting = false;
            updateWsStatusIndicator(WS_STATUS.DISCONNECTED);
            scheduleReconnect(maDh);
        });

        // Xử lý sự kiện socket bị đóng
        socket.onclose = function () {
            console.warn('🔌 WebSocket socket closed');
            updateWsStatusIndicator(WS_STATUS.DISCONNECTED);
            if (currentMaDh && !isConnecting) {
                scheduleReconnect(currentMaDh);
            }
        };

    } catch (e) {
        console.error('❌ WebSocket: Lỗi khởi tạo kết nối:', e);
        isConnecting = false;
        updateWsStatusIndicator(WS_STATUS.DISCONNECTED);
        scheduleReconnect(maDh);
    }
}

/**
 * Xử lý message nhận được từ WebSocket server.
 * Được gọi tự động khi có cập nhật trạng thái đơn hàng.
 *
 * @param {Object} message - STOMP message frame chứa JSON body
 */
function onMessageReceived(message) {
    try {
        const data = JSON.parse(message.body);
        console.log('📡 Nhận cập nhật real-time:', data);

        // 1. Cập nhật stepper tiến trình (hàm có sẵn trong tracking page)
        if (typeof updateStepper === 'function') {
            updateStepper(data.maTrangThaiMoi);

            // Cập nhật timestamp cho bước trạng thái trên UI
            const timeEl = document.getElementById('stepTime-' + data.maTrangThaiMoi);
            if (timeEl) {
                const now = data.thoiGian ? new Date(data.thoiGian) : new Date();
                timeEl.innerText = now.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }) + ' ' + 
                                   now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
            }
        }

        // 2. Cập nhật badge trạng thái ở header
        updateStatusBadge(data);

        // 3. Thêm timeline item mới lên đầu danh sách lịch sử
        prependTimelineItem(data);

        // 4. Phát âm thanh thông báo ngắn "Ting"
        playNotificationSound();

        // 5. Hiển thị toast thông báo trạng thái vừa thay đổi
        showTrackingToast(data);

    } catch (e) {
        console.error('❌ WebSocket: Lỗi xử lý message:', e);
    }
}

/**
 * Cập nhật badge trạng thái trên header kết quả tracking.
 *
 * @param {Object} data - Payload cập nhật từ WebSocket
 */
function updateStatusBadge(data) {
    const badgeEl = document.getElementById('resBadge');
    if (badgeEl && typeof getStatusBadge === 'function') {
        badgeEl.innerHTML = getStatusBadge(data.tenTrangThaiMoi);
    }

    // Cập nhật thời gian cập nhật cuối
    const lastUpdateEl = document.getElementById('resLastUpdate');
    if (lastUpdateEl) {
        const now = new Date();
        const timeStr = now.toLocaleString('vi-VN', {
            hour: '2-digit', minute: '2-digit', second: '2-digit',
            day: '2-digit', month: '2-digit', year: 'numeric'
        });
        lastUpdateEl.innerText = 'Cập nhật lúc: ' + timeStr;
    }
}

/**
 * Thêm một mục lịch sử mới lên đầu timeline trong UI.
 *
 * @param {Object} data - Payload cập nhật từ WebSocket
 */
function prependTimelineItem(data) {
    const container = document.getElementById('timelineContainer');
    if (!container) return;

    // Định dạng thời gian hiển thị
    const timeStr = data.thoiGian
        ? (typeof formatDateTime === 'function' ? formatDateTime(data.thoiGian) : new Date(data.thoiGian).toLocaleString('vi-VN'))
        : new Date().toLocaleString('vi-VN');

    // Tạo HTML timeline item mới
    const newItem = document.createElement('div');
    newItem.className = 'timeline-item active';
    newItem.innerHTML = `
        <div class="timeline-point"></div>
        <div class="timeline-time">${timeStr}</div>
        <div class="timeline-title">${data.tenTrangThaiMoi || data.maTrangThaiMoi}</div>
        <div class="timeline-desc">${data.ghiChu || 'Đã ghi nhận trạng thái vào hệ thống điều phối.'}</div>
    `;

    // Chuyển item cũ nhất đang active thành completed
    const existingActive = container.querySelector('.timeline-item.active');
    if (existingActive) {
        existingActive.classList.remove('active');
        existingActive.classList.add('completed');
    }

    // Chèn item mới lên đầu với animation
    newItem.style.opacity = '0';
    newItem.style.transform = 'translateY(-10px)';
    container.insertBefore(newItem, container.firstChild);

    // Trigger animation hiển thị
    requestAnimationFrame(() => {
        newItem.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
        newItem.style.opacity = '1';
        newItem.style.transform = 'translateY(0)';
    });
}

/**
 * Phát âm thanh thông báo ngắn "Ting" bằng Web Audio API.
 * Không cần file âm thanh bên ngoài.
 */
function playNotificationSound() {
    try {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtx) return;

        const ctx = new AudioCtx();
        const oscillator = ctx.createOscillator();
        const gainNode = ctx.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(ctx.destination);

        // Nốt La cao (A5 = 880Hz) — âm thanh sáng, dễ nhận biết
        oscillator.frequency.setValueAtTime(880, ctx.currentTime);
        oscillator.type = 'sine';

        // Âm lượng nhẹ, không gây khó chịu
        gainNode.gain.setValueAtTime(0.3, ctx.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);

        oscillator.start(ctx.currentTime);
        oscillator.stop(ctx.currentTime + 0.5);
    } catch (e) {
        // Bỏ qua nếu trình duyệt chặn autoplay audio
        console.warn('🔇 Không thể phát âm thanh:', e.message);
    }
}

/**
 * Hiển thị thông báo toast khi nhận cập nhật WebSocket.
 * Toast slide-in từ phải, tự động biến mất sau 5 giây.
 *
 * @param {Object} data - Payload cập nhật từ WebSocket
 */
function showTrackingToast(data) {
    // Xóa toast cũ nếu có
    const oldToast = document.querySelector('.ws-toast');
    if (oldToast) oldToast.remove();

    const toast = document.createElement('div');
    toast.className = 'ws-toast';
    toast.innerHTML = `
        <div class="ws-toast-icon">📡</div>
        <div class="ws-toast-body">
            <strong>Cập nhật đơn ${data.maDh || ''}</strong>
            <p>${data.tenTrangThaiCu || ''} → <b>${data.tenTrangThaiMoi || ''}</b></p>
            <small>${data.ghiChu || ''}</small>
        </div>
        <button class="ws-toast-close" onclick="this.parentElement.remove()" title="Đóng">&times;</button>
    `;
    document.body.appendChild(toast);

    // Animation: slide in → hold 5 giây → fade out
    requestAnimationFrame(() => toast.classList.add('show'));
    setTimeout(() => {
        if (toast.parentElement) {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 400);
        }
    }, 5000);
}

/**
 * Tự động kết nối lại WebSocket sau 5 giây khi bị mất kết nối.
 *
 * @param {string} maDh - Mã đơn hàng cần kết nối lại
 */
function scheduleReconnect(maDh) {
    clearTimeout(reconnectTimer);
    reconnectTimer = setTimeout(() => {
        if (maDh && !isConnecting) {
            console.log('🔄 Đang thử kết nối lại WebSocket cho đơn ' + maDh + '...');
            connectWebSocket(maDh);
        }
    }, 5000); // 5 giây
}

/**
 * Ngắt kết nối WebSocket hiện tại (gọi khi rời trang hoặc search đơn mới).
 */
function disconnectWebSocket() {
    clearTimeout(reconnectTimer);
    currentMaDh = null;

    if (currentSubscription) {
        try { currentSubscription.unsubscribe(); } catch (e) { /* ignore */ }
        currentSubscription = null;
    }

    if (stompClient) {
        try {
            if (stompClient.connected) {
                stompClient.disconnect();
            }
        } catch (e) { /* ignore */ }
        stompClient = null;
    }

    updateWsStatusIndicator(WS_STATUS.DISCONNECTED);
}

/**
 * Cập nhật indicator trạng thái kết nối WebSocket trên UI (nếu có).
 * Element ID: #wsStatusIndicator
 *
 * @param {string} status - Một trong: 'connected', 'connecting', 'disconnected'
 */
function updateWsStatusIndicator(status) {
    const indicator = document.getElementById('wsStatusIndicator');
    if (!indicator) return;

    indicator.className = 'ws-status-indicator ws-' + status;

    const labels = {
        'connected': '🟢 Real-time đang hoạt động',
        'connecting': '🟡 Đang kết nối...',
        'disconnected': '🔴 Mất kết nối'
    };
    indicator.innerText = labels[status] || '';
    indicator.style.display = status === WS_STATUS.DISCONNECTED && !currentMaDh ? 'none' : 'inline-flex';
}

// Ngắt kết nối khi người dùng rời trang
window.addEventListener('beforeunload', disconnectWebSocket);
