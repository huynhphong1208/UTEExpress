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
        case 'Đã thanh toán':
        case 'Sẵn sàng':
            return `<span class="badge badge-success"><span class="badge-dot"></span>${status}</span>`;
            
        case 'Đang đi':
        case 'Đang thực hiện':
        case 'Đang vận chuyển':
        case 'Đang xử lý':
        case 'Đang chạy':
        case 'Đã quét':
            return `<span class="badge badge-info"><span class="badge-dot"></span>${status}</span>`;
            
        case 'Chưa đi':
        case 'Chưa xuất phát':
        case 'Đã gán':
        case 'Mới tạo':
        case 'Đã tiếp nhận':
        case 'Đến kho':
        case 'Đang giao':
        case 'Chờ tiếp nhận':
        case 'Chưa thanh toán':
            return `<span class="badge badge-warning"><span class="badge-dot"></span>${status}</span>`;
            
        case 'Đã hủy':
        case 'Hủy':
        case 'Trả hàng':
        case 'Bị khóa':
        case 'Thất bại':
        case 'Bảo trì':
            return `<span class="badge badge-danger"><span class="badge-dot"></span>${status}</span>`;
            
        default:
            return `<span class="badge badge-gray"><span class="badge-dot"></span>${status || 'Không rõ'}</span>`;
    }
}

// 4. BỘ ĐIỀU KHIỂN PHÂN TRANG BẢNG DỮ LIỆU (5, 10, 20 DÒNG/TRANG)
class TablePagination {
    constructor({ containerId, defaultPageSize = 5, pageSizeOptions = [5, 10, 20], onChange }) {
        this.containerId = containerId;
        this.pageSize = defaultPageSize;
        this.currentPage = 1;
        this.totalItems = 0;
        this.pageSizeOptions = pageSizeOptions;
        this.onChange = onChange;
    }

    getContainer() {
        return typeof this.containerId === 'string' ? document.getElementById(this.containerId) : this.containerId;
    }

    setPage(page) {
        const totalPages = Math.max(1, Math.ceil(this.totalItems / this.pageSize));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        if (this.currentPage !== page) {
            this.currentPage = page;
            if (typeof this.onChange === 'function') {
                this.onChange(this.currentPage, this.pageSize);
            }
        }
    }

    setPageSize(size) {
        const s = parseInt(size, 10);
        if (!isNaN(s) && this.pageSize !== s) {
            this.pageSize = s;
            this.currentPage = 1;
            if (typeof this.onChange === 'function') {
                this.onChange(this.currentPage, this.pageSize);
            }
        }
    }

    reset() {
        this.currentPage = 1;
    }

    paginate(items) {
        if (!Array.isArray(items)) items = [];
        this.totalItems = items.length;
        const totalPages = Math.max(1, Math.ceil(this.totalItems / this.pageSize));
        if (this.currentPage > totalPages) {
            this.currentPage = totalPages;
        }
        if (this.currentPage < 1) {
            this.currentPage = 1;
        }
        this.render();
        const start = (this.currentPage - 1) * this.pageSize;
        return items.slice(start, start + this.pageSize);
    }

    setServerPageInfo(info) {
        if (!info) return;
        if (info.page !== undefined) this.currentPage = info.page;
        if (info.size !== undefined) this.pageSize = info.size;
        if (info.totalItems !== undefined) this.totalItems = info.totalItems;
        this.render();
    }


    render() {
        const container = this.getContainer();
        if (!container) return;

        const totalPages = Math.max(1, Math.ceil(this.totalItems / this.pageSize));
        const startItem = this.totalItems === 0 ? 0 : (this.currentPage - 1) * this.pageSize + 1;
        const endItem = Math.min(this.currentPage * this.pageSize, this.totalItems);

        const sizeOptionsHtml = this.pageSizeOptions.map(opt => 
            `<option value="${opt}" ${opt === this.pageSize ? 'selected' : ''}>${opt} / trang</option>`
        ).join('');

        let pageNumbers = [];
        if (totalPages <= 7) {
            for (let i = 1; i <= totalPages; i++) pageNumbers.push(i);
        } else {
            pageNumbers.push(1);
            if (this.currentPage > 3) {
                pageNumbers.push('...');
            }
            const startP = Math.max(2, this.currentPage - 1);
            const endP = Math.min(totalPages - 1, this.currentPage + 1);
            for (let i = startP; i <= endP; i++) {
                if (!pageNumbers.includes(i)) pageNumbers.push(i);
            }
            if (this.currentPage < totalPages - 2) {
                pageNumbers.push('...');
            }
            if (!pageNumbers.includes(totalPages)) {
                pageNumbers.push(totalPages);
            }
        }

        const pagesButtonsHtml = pageNumbers.map(p => {
            if (p === '...') {
                return `<span class="page-btn" style="border:none; cursor:default; background:none; opacity:0.6;">...</span>`;
            }
            return `<button type="button" class="page-btn ${p === this.currentPage ? 'active' : ''}" data-page="${p}">${p}</button>`;
        }).join('');

        container.className = 'pagination-wrapper';
        container.innerHTML = `
            <div class="pagination-info">
                <span>Hiển thị <strong>${startItem} - ${endItem}</strong> trên <strong>${this.totalItems}</strong> bản ghi</span>
                <label style="display:flex; align-items:center; gap:6px; margin:0;">
                    <span style="color:var(--text-muted); font-size:12px;">Số dòng:</span>
                    <select class="pagination-size-select">
                        ${sizeOptionsHtml}
                    </select>
                </label>
            </div>
            <div class="pagination">
                <button type="button" class="page-btn btn-first" title="Trang đầu" ${this.currentPage <= 1 ? 'disabled' : ''}>«</button>
                <button type="button" class="page-btn btn-prev" title="Trang trước" ${this.currentPage <= 1 ? 'disabled' : ''}>‹</button>
                ${pagesButtonsHtml}
                <button type="button" class="page-btn btn-next" title="Trang sau" ${this.currentPage >= totalPages ? 'disabled' : ''}>›</button>
                <button type="button" class="page-btn btn-last" title="Trang cuối" ${this.currentPage >= totalPages ? 'disabled' : ''}>»</button>
            </div>
        `;

        const selectEl = container.querySelector('.pagination-size-select');
        if (selectEl) {
            selectEl.addEventListener('change', (e) => {
                this.setPageSize(e.target.value);
            });
        }

        const btnFirst = container.querySelector('.btn-first');
        if (btnFirst && !btnFirst.disabled) {
            btnFirst.addEventListener('click', () => this.setPage(1));
        }

        const btnPrev = container.querySelector('.btn-prev');
        if (btnPrev && !btnPrev.disabled) {
            btnPrev.addEventListener('click', () => this.setPage(this.currentPage - 1));
        }

        const btnNext = container.querySelector('.btn-next');
        if (btnNext && !btnNext.disabled) {
            btnNext.addEventListener('click', () => this.setPage(this.currentPage + 1));
        }

        const btnLast = container.querySelector('.btn-last');
        if (btnLast && !btnLast.disabled) {
            btnLast.addEventListener('click', () => this.setPage(totalPages));
        }

        const numBtns = container.querySelectorAll('.page-btn[data-page]');
        numBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const p = parseInt(e.currentTarget.getAttribute('data-page'), 10);
                if (p) this.setPage(p);
            });
        });
    }
}

// Global Logout Helper
function logout() {
    localStorage.clear();
    sessionStorage.clear();
    document.cookie = 'accessToken=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC; SameSite=Lax';
    window.location.href = '/logout';
}
window.logout = logout;


