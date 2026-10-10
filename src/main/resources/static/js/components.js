/**
 * UTE EXPRESS LOGISTICS - UI COMPONENTS & HELPERS
 * Dành riêng cho 2 vai trò: Nhân viên Điều phối & Tài xế
 */

// 1. POPUP MODAL CONTROLS
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('show');
        document.body.style.overflow = 'hidden';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('show');
        document.body.style.overflow = '';
    }
}

// Đóng modal khi click ra ngoài vùng backdrop
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-backdrop')) {
        e.target.classList.remove('show');
        document.body.style.overflow = '';
    }
});

// 2. TOAST NOTIFICATION SYSTEM
function showToast(title, message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div style="flex: 1;">
            <div class="toast-title">${title}</div>
            <div class="toast-msg">${message}</div>
        </div>
        <button style="background:none;border:none;cursor:pointer;color:#94A3B8;font-size:16px;" onclick="this.parentElement.remove()">✕</button>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// 3. RENDER STATUS BADGE HELPER
function getStatusBadge(status) {
    switch (status) {
        case 'Hoàn thành':
        case 'Giao thành công':
        case 'Đã giao':
        case 'Hoạt động':
        case 'Sẵn sàng':
            return `<span class="badge badge-success"><span class="badge-dot"></span>${status}</span>`;
            
        case 'Đang đi':
        case 'Đang thực hiện':
        case 'Đang vận chuyển':
        case 'Đã quét':
            return `<span class="badge badge-info"><span class="badge-dot"></span>${status}</span>`;
            
        case 'Chưa đi':
        case 'Chưa xuất phát':
        case 'Đã gán':
        case 'Đang giao':
            return `<span class="badge badge-warning"><span class="badge-dot"></span>${status}</span>`;
            
        case 'Đã hủy':
        case 'Hủy':
        case 'Thất bại':
        case 'Bảo trì':
            return `<span class="badge badge-danger"><span class="badge-dot"></span>${status}</span>`;
            
        default:
            return `<span class="badge badge-gray"><span class="badge-dot"></span>${status || 'Không rõ'}</span>`;
    }
}

