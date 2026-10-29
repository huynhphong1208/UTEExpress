/**
 * account-settings.js - Quản lý Cài đặt tài khoản (Xem Thông Tin, Đổi Avatar & Đổi Mật Khẩu) cho tất cả các Role
 * UTE Express Logistics
 * Lưu trữ ảnh trực tiếp trên hệ thống tệp Backend, không cần cột trong cơ sở dữ liệu.
 */
(function() {
    let currentUserProfile = null;

    function getCookie(name) {
        const v = document.cookie.match('(^|;) ?' + name + '=([^;]*)(;|$)');
        return v ? decodeURIComponent(v[2]) : null;
    }

    // Helper lấy JWT token
    function getAuthToken() {
        const t = localStorage.getItem('accessToken') || localStorage.getItem('token') || getCookie('accessToken') || '';
        return (t && t !== 'null' && t !== 'undefined') ? t : '';
    }

    // Cập nhật hiển thị tên người dùng
    function updateNameElements(name) {
        if (!name || /^[0-9+]+$/.test(name.trim())) return;
        const nameDisplays = document.querySelectorAll('#userNameDisplay, .user-name');
        nameDisplays.forEach(el => {
            el.innerText = name;
        });
    }

    // Hiển thị ảnh đại diện hoặc chữ cái viết tắt
    function renderAvatars(avatarUrl) {
        const avatarElements = document.querySelectorAll('.user-avatar, #userAvatar');
        avatarElements.forEach(el => {
            if (avatarUrl) {
                el.innerHTML = `<img src="${avatarUrl}" alt="Avatar" style="width: 100%; height: 100%; border-radius: 50%; object-fit: cover; display: block;">`;
            } else {
                const currentName = localStorage.getItem('hoTen') || el.innerText || 'AD';
                const words = currentName.trim().split(' ');
                let initials = 'AD';
                if (words.length >= 2) {
                    initials = (words[words.length - 2][0] + words[words.length - 1][0]).toUpperCase();
                } else if (words.length === 1 && words[0].length >= 2) {
                    initials = words[0].substring(0, 2).toUpperCase();
                }
                el.innerText = initials;
            }
            el.style.cursor = 'pointer';
            el.title = 'Bấm để xem thông tin / cài đặt tài khoản';
            el.onclick = openAccountSettingsModal;
        });
    }

    // 1. Đồng bộ Avatar và Tên hiển thị trên toàn trang
    function applyUserAvatarAndName() {
        const token = getAuthToken();
        const hoTen = localStorage.getItem('hoTen');
        const cachedAvatar = localStorage.getItem('userAvatarUrl');

        // Hiển thị nhanh từ cache localStorage
        if (hoTen && !/^[0-9+]+$/.test(hoTen.trim())) {
            updateNameElements(hoTen);
        }
        renderAvatars(cachedAvatar);

        // Luôn đồng bộ dữ liệu chuẩn từ Backend API /api/auth/me
        if (token) {
            fetch('/api/auth/me', {
                headers: { 'Authorization': 'Bearer ' + token },
                credentials: 'include'
            })
            .then(res => res.ok ? res.json() : null)
            .then(data => {
                if (data && data.success && data.data) {
                    currentUserProfile = data.data;
                    const user = data.data;
                    if (user.hoTen) {
                        localStorage.setItem('hoTen', user.hoTen);
                        updateNameElements(user.hoTen);
                    }
                    if (user.avatarUrl) {
                        localStorage.setItem('userAvatarUrl', user.avatarUrl);
                        renderAvatars(user.avatarUrl);
                    } else {
                        localStorage.removeItem('userAvatarUrl');
                        renderAvatars(null);
                    }
                }
            })
            .catch(() => {});
        }

        // Bổ sung nút bánh răng Cài đặt vào sidebar-footer nếu chưa có
        const sideFooters = document.querySelectorAll('.sidebar-footer');
        sideFooters.forEach(footer => {
            if (!footer.querySelector('.btn-settings-gear') && !footer.querySelector('.sidebar-settings-btn')) {
                const gearBtn = document.createElement('button');
                gearBtn.type = 'button';
                gearBtn.className = 'sidebar-settings-btn';
                gearBtn.title = 'Cài đặt tài khoản (Xem thông tin, đổi avatar & mật khẩu)';
                gearBtn.innerHTML = `<svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>`;
                gearBtn.onclick = openAccountSettingsModal;
                footer.appendChild(gearBtn);
            }
        });

        // Dọn dẹp bất kỳ mục "Cài đặt tài khoản" nào trong sidebar-nav (người dùng mở qua avatar/tên/bánh răng ở sidebar footer)
        document.querySelectorAll('.nav-item-account-settings').forEach(el => el.remove());

        // Bổ sung sự kiện click mở modal cho phần user-info và header actions
        const userInfos = document.querySelectorAll('.user-info, .sidebar-footer .user-info, #userNameDisplay, #userSubDisplay');
        userInfos.forEach(el => {
            el.style.cursor = 'pointer';
            el.title = 'Bấm để cài đặt tài khoản';
            el.onclick = openAccountSettingsModal;
        });
    }

    // 2. Chèn Modal Cài Đặt Tài Khoản vào DOM
    function injectAccountModal() {
        if (document.getElementById('accountSettingsModal')) return;

        const modalHtml = `
        <div id="accountSettingsModal" class="account-modal-backdrop" onclick="if(event.target===this) closeAccountSettingsModal()">
            <div class="account-modal-box" style="max-width: 580px;">
                <div class="account-modal-header">
                    <h3 class="account-modal-title">
                        <span>⚙️ Cài đặt tài khoản</span>
                    </h3>
                    <button type="button" class="account-modal-close" onclick="closeAccountSettingsModal()">✕</button>
                </div>

                <div class="account-tabs">
                    <button type="button" id="tabBtnInfo" class="account-tab-btn active" onclick="switchAccountTab('info')">
                        👤 Thông tin tài khoản
                    </button>
                    <button type="button" id="tabBtnAvatar" class="account-tab-btn" onclick="switchAccountTab('avatar')">
                        🖼️ Ảnh đại diện
                    </button>
                    <button type="button" id="tabBtnPassword" class="account-tab-btn" onclick="switchAccountTab('password')">
                        🔒 Đổi mật khẩu
                    </button>
                </div>

                <!-- Tab 1: Thông Tin & Cập Nhật Tài Khoản -->
                <div id="tabContentInfo" class="account-tab-content active">
                    <div class="profile-card-header" style="margin-bottom: 16px;">
                        <div id="infoCardAvatar" class="profile-card-avatar">KH</div>
                        <div class="profile-card-info">
                            <div id="infoCardName" class="profile-card-name">Đang tải...</div>
                            <div class="profile-card-badges">
                                <span id="infoCardRole" class="profile-badge-role">Khách hàng</span>
                                <span id="infoCardStatus" class="profile-badge-status">Hoạt động</span>
                            </div>
                        </div>
                    </div>

                    <div id="profileAlert" style="display: none; padding: 10px 14px; border-radius: 8px; font-size: 13px; margin-bottom: 14px;"></div>

                    <form id="editProfileForm" onsubmit="handleSaveProfile(event)">
                        <div class="account-input-group">
                            <label for="profileEditHoTen">Họ và tên <span style="color: #E31B23;">*</span></label>
                            <input type="text" id="profileEditHoTen" required placeholder="Nhập họ và tên">
                        </div>

                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                            <div class="account-input-group">
                                <label for="profileEditSdt">Số điện thoại (SĐT) <span style="color: #E31B23;">*</span></label>
                                <input type="tel" id="profileEditSdt" required pattern="[0-9]{10}" maxlength="10" placeholder="10 chữ số">
                            </div>
                            <div class="account-input-group">
                                <label for="profileEditEmail">Gmail / Email <span style="color: #E31B23;">*</span></label>
                                <input type="email" id="profileEditEmail" placeholder="example@gmail.com">
                            </div>
                        </div>

                        <div class="account-input-group" id="profileEditDiaChiGroup">
                            <label for="profileEditDiaChi" id="profileEditDiaChiLabel">Địa chỉ mặc định</label>
                            <input type="text" id="profileEditDiaChi" placeholder="Nhập địa chỉ của bạn">
                        </div>

                        <div class="profile-grid" style="margin-top: 10px; margin-bottom: 12px;">
                            <div class="profile-item">
                                <div class="profile-item-label">🆔 Mã tài khoản</div>
                                <div id="infoItemMaNd" class="profile-item-value">--</div>
                            </div>
                            <div class="profile-item">
                                <div class="profile-item-label">🏷️ Mã định danh</div>
                                <div id="infoItemMaDinhDanh" class="profile-item-value">--</div>
                            </div>
                            <div class="profile-item" style="grid-column: span 2;">
                                <div class="profile-item-label" id="infoItemLocationLabel">🏢 Bưu cục / Đơn vị</div>
                                <div id="infoItemLocation" class="profile-item-value">--</div>
                            </div>
                        </div>

                        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 16px; padding-top: 14px; border-top: 1px solid #E2E8F0;">
                            <div style="display: flex; gap: 8px;">
                                <button type="button" class="btn btn-secondary btn-sm" onclick="switchAccountTab('avatar')" style="padding: 7px 12px; border: 1px solid #CBD5E1; background: #F1F5F9; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: 500;">
                                    🖼️ Đổi ảnh
                                </button>
                                <button type="button" class="btn btn-secondary btn-sm" onclick="switchAccountTab('password')" style="padding: 7px 12px; border: 1px solid #CBD5E1; background: #F1F5F9; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: 500;">
                                    🔒 Mật khẩu
                                </button>
                            </div>
                            <button type="submit" id="btnSaveProfile" class="btn btn-primary btn-sm" style="background: #E31B23; color: white; border: none; padding: 8px 18px; border-radius: 6px; font-weight: 600; cursor: pointer;">
                                💾 Lưu thay đổi thông tin
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Tab 2: Cập nhật Avatar -->
                <div id="tabContentAvatar" class="account-tab-content">
                    <div class="avatar-preview-box">
                        <div id="modalAvatarPreview" class="avatar-preview-img">KH</div>
                        <span style="font-size: 12px; color: #64748B;">Xem trước ảnh đại diện</span>
                    </div>

                    <div id="avatarAlert" style="display: none; padding: 10px 14px; border-radius: 8px; font-size: 13px; margin-bottom: 14px;"></div>

                    <!-- Cách 1: Tải ảnh từ máy tính -->
                    <div class="account-input-group">
                        <label>Cách 1: Tải ảnh từ máy tính (PNG, JPG, WEBP tối đa 5MB)</label>
                        <input type="file" id="avatarFileInput" accept="image/*" style="padding: 7px 10px;">
                    </div>

                    <!-- Cách 2: Dán đường dẫn link ảnh -->
                    <div class="account-input-group">
                        <label>Cách 2: Nhập đường dẫn link ảnh trực tiếp (URL)</label>
                        <div style="display: flex; gap: 8px;">
                            <input type="url" id="avatarUrlInput" placeholder="https://example.com/avatar.jpg" style="flex: 1;">
                            <button type="button" class="btn btn-secondary btn-sm" onclick="applyAvatarFromUrl()" style="white-space: nowrap; padding: 8px 14px;">Áp dụng</button>
                        </div>
                    </div>

                    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 14px; border-top: 1px solid #E2E8F0;">
                        <button type="button" class="btn btn-outline-danger btn-sm" onclick="removeAvatar()" style="color: #E31B23; border: 1px solid #E31B23; background: transparent; padding: 7px 14px; border-radius: 6px; cursor: pointer;">
                            Xóa ảnh (Mặc định)
                        </button>
                        <button type="button" id="btnSaveAvatar" class="btn btn-primary btn-sm" onclick="saveAvatarSettings()" style="background: #E31B23; color: white; border: none; padding: 8px 20px; border-radius: 6px; font-weight: 600; cursor: pointer;">
                            ✓ Lưu Avatar
                        </button>
                    </div>
                </div>

                <!-- Tab 3: Đổi Mật Khẩu -->
                <div id="tabContentPassword" class="account-tab-content">
                    <div id="passwordAlert" style="display: none; padding: 10px 14px; border-radius: 8px; font-size: 13px; margin-bottom: 14px;"></div>

                    <form id="changePasswordForm" onsubmit="handleChangePassword(event)">
                        <div class="account-input-group">
                            <label for="oldPassword">Mật khẩu hiện tại <span style="color: #E31B23;">*</span></label>
                            <input type="password" id="oldPassword" required placeholder="Nhập mật khẩu hiện tại">
                        </div>

                        <div class="account-input-group">
                            <label for="newPassword">Mật khẩu mới (tối thiểu 6 ký tự) <span style="color: #E31B23;">*</span></label>
                            <input type="password" id="newPassword" minlength="6" required placeholder="Nhập mật khẩu mới">
                        </div>

                        <div class="account-input-group">
                            <label for="confirmNewPassword">Xác nhận mật khẩu mới <span style="color: #E31B23;">*</span></label>
                            <input type="password" id="confirmNewPassword" minlength="6" required placeholder="Nhập lại mật khẩu mới">
                        </div>

                        <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; padding-top: 14px; border-top: 1px solid #E2E8F0;">
                            <button type="button" class="btn btn-secondary btn-sm" onclick="closeAccountSettingsModal()" style="padding: 8px 16px; border: 1px solid #CBD5E1; background: #F1F5F9; border-radius: 6px; cursor: pointer;">
                                Hủy
                            </button>
                            <button type="submit" id="btnSubmitChangePass" class="btn btn-primary btn-sm" style="background: #E31B23; color: white; border: none; padding: 8px 22px; border-radius: 6px; font-weight: 600; cursor: pointer;">
                                🔒 Đổi mật khẩu
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        // Lắng nghe chọn file upload từ máy tính
        const fileInput = document.getElementById('avatarFileInput');
        if (fileInput) {
            fileInput.addEventListener('change', function(e) {
                const file = e.target.files[0];
                if (file) {
                    if (file.size > 5 * 1024 * 1024) {
                        showAvatarAlert('Kích thước ảnh không được vượt quá 5MB!', false);
                        fileInput.value = '';
                        return;
                    }
                    const reader = new FileReader();
                    reader.onload = function(event) {
                        const dataUrl = event.target.result;
                        setModalPreview(dataUrl);
                        showAvatarAlert('Đã chọn ảnh từ máy tính. Bấm "Lưu Avatar" để lưu vào hệ thống!', true);
                    };
                    reader.readAsDataURL(file);
                }
            });
        }
    }

    // Mở / Đóng Modal
    window.openAccountSettingsModal = function() {
        injectAccountModal();
        const modal = document.getElementById('accountSettingsModal');
        modal.classList.add('show');
        resetModalState();
        switchAccountTab('info');
        loadAccountInfo();
    };

    window.closeAccountSettingsModal = function() {
        const modal = document.getElementById('accountSettingsModal');
        if (modal) modal.classList.remove('show');
    };

    window.switchAccountTab = function(tabName) {
        const btnInfo = document.getElementById('tabBtnInfo');
        const btnAvatar = document.getElementById('tabBtnAvatar');
        const btnPass = document.getElementById('tabBtnPassword');
        const contentInfo = document.getElementById('tabContentInfo');
        const contentAvatar = document.getElementById('tabContentAvatar');
        const contentPass = document.getElementById('tabContentPassword');

        [btnInfo, btnAvatar, btnPass].forEach(b => b && b.classList.remove('active'));
        [contentInfo, contentAvatar, contentPass].forEach(c => c && c.classList.remove('active'));

        if (tabName === 'info') {
            if (btnInfo) btnInfo.classList.add('active');
            if (contentInfo) contentInfo.classList.add('active');
            loadAccountInfo();
        } else if (tabName === 'avatar') {
            if (btnAvatar) btnAvatar.classList.add('active');
            if (contentAvatar) contentAvatar.classList.add('active');
        } else {
            if (btnPass) btnPass.classList.add('active');
            if (contentPass) contentPass.classList.add('active');
        }
    };

    // Tải và hiển thị Thông Tin Tài Khoản vào Tab 1
    function loadAccountInfo() {
        const token = getAuthToken();
        const renderProfile = (user) => {
            if (!user) return;
            const nameEl = document.getElementById('infoCardName');
            const roleEl = document.getElementById('infoCardRole');
            const statusEl = document.getElementById('infoCardStatus');
            const avatarEl = document.getElementById('infoCardAvatar');
            const phoneEl = document.getElementById('infoItemPhone');
            const emailEl = document.getElementById('infoItemEmail');
            const maNdEl = document.getElementById('infoItemMaNd');
            const maDinhDanhEl = document.getElementById('infoItemMaDinhDanh');
            const locationEl = document.getElementById('infoItemLocation');
            const locationLabel = document.getElementById('infoItemLocationLabel');

            if (nameEl) nameEl.innerText = user.hoTen || user.tenDangNhap || '--';
            if (roleEl) roleEl.innerText = user.tenVaiTro || user.vaiTro || '--';
            if (statusEl) statusEl.innerText = user.trangThai || 'Hoạt động';

            // Avatar trong thẻ profile
            if (avatarEl) {
                if (user.avatarUrl) {
                    avatarEl.innerHTML = `<img src="${user.avatarUrl}" style="width: 100%; height: 100%; border-radius: 50%; object-fit: cover;">`;
                } else {
                    const initials = getInitials(user.hoTen || user.tenDangNhap || 'KH');
                    avatarEl.innerText = initials;
                }
            }

            if (phoneEl) phoneEl.innerText = user.sdt || user.tenDangNhap || '--';
            if (emailEl) emailEl.innerText = user.email || 'Chưa cập nhật';
            if (maNdEl) maNdEl.innerText = user.maNd || '--';
            if (maDinhDanhEl) maDinhDanhEl.innerText = user.maDinhDanh || user.maNd || '--';

            if (locationEl) {
                if (user.vaiTro === 'TAI_XE' && user.bangLai) {
                    if (locationLabel) locationLabel.innerText = '🪪 Giấy phép lái xe / Địa bàn';
                    locationEl.innerText = 'Hạng ' + user.bangLai;
                } else if (user.khoLamViec) {
                    if (locationLabel) locationLabel.innerText = '🏢 Bưu cục / Kho làm việc';
                    locationEl.innerText = user.khoLamViec + (user.chucVu ? ' (' + user.chucVu + ')' : '');
                } else {
                    locationEl.innerText = user.diaChi || 'Trụ sở chính';
                }
            }

            // Điền vào các trường chỉnh sửa
            const editName = document.getElementById('profileEditHoTen');
            const editPhone = document.getElementById('profileEditSdt');
            const editEmail = document.getElementById('profileEditEmail');
            const editLocation = document.getElementById('profileEditDiaChi');
            const editDiaChiGroup = document.getElementById('profileEditDiaChiGroup');

            if (editName && (!editName.value || editName.value === '--')) editName.value = user.hoTen || user.tenDangNhap || '';
            if (editPhone && (!editPhone.value || editPhone.value === '--')) editPhone.value = user.sdt || user.tenDangNhap || '';
            if (editEmail && (!editEmail.value || editEmail.value === '--')) editEmail.value = (user.email && user.email !== 'Chưa cập nhật') ? user.email : '';
            if (editLocation && (!editLocation.value || editLocation.value === '--')) editLocation.value = (user.diaChi && user.diaChi !== 'Chưa cập nhật') ? user.diaChi : '';
            if (editDiaChiGroup && user.vaiTro !== 'KHACH_HANG') {
                const label = document.getElementById('profileEditDiaChiLabel');
                if (label) label.innerText = 'Địa chỉ / Ghi chú nơi làm việc';
            }
        };

        if (currentUserProfile) {
            renderProfile(currentUserProfile);
        }

        if (token) {
            fetch('/api/auth/me', {
                headers: { 'Authorization': 'Bearer ' + token },
                credentials: 'include'
            })
            .then(res => res.ok ? res.json() : null)
            .then(data => {
                if (data && data.success && data.data) {
                    currentUserProfile = data.data;
                    renderProfile(data.data);
                }
            })
            .catch(() => {});
        }
    }

    function showProfileAlert(msg, isSuccess) {
        const box = document.getElementById('profileAlert');
        if (!box) return;
        box.style.display = 'block';
        box.style.background = isSuccess ? '#DCFCE7' : '#FEE2E2';
        box.style.color = isSuccess ? '#15803D' : '#B91C1C';
        box.innerText = msg;
    }

    window.handleSaveProfile = async function(e) {
        e.preventDefault();
        const hoTen = document.getElementById('profileEditHoTen').value.trim();
        const sdt = document.getElementById('profileEditSdt').value.trim();
        const email = document.getElementById('profileEditEmail').value.trim();
        const diaChi = document.getElementById('profileEditDiaChi') ? document.getElementById('profileEditDiaChi').value.trim() : '';
        const btn = document.getElementById('btnSaveProfile');

        if (!hoTen) {
            showProfileAlert('Vui lòng nhập họ và tên!', false);
            return;
        }
        if (!/^[0-9]{10}$/.test(sdt)) {
            showProfileAlert('Số điện thoại phải có đúng 10 chữ số!', false);
            return;
        }
        if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            showProfileAlert('Địa chỉ email / gmail không đúng định dạng!', false);
            return;
        }

        const token = getAuthToken();
        if (!token) {
            showProfileAlert('Phiên làm việc hết hạn. Vui lòng đăng nhập lại!', false);
            return;
        }

        btn.disabled = true;
        btn.innerText = 'Đang lưu...';

        try {
            const res = await fetch('/api/auth/profile', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                credentials: 'include',
                body: JSON.stringify({ hoTen, sdt, email, diaChi })
            });
            const data = await res.json();
            if (res.ok && data.success) {
                currentUserProfile = data.data;
                localStorage.setItem('hoTen', hoTen);
                updateNameElements(hoTen);
                if (!localStorage.getItem('userAvatarUrl')) {
                    renderAvatars(null);
                }
                loadAccountInfo();
                showProfileAlert('✓ Cập nhật tên, gmail và số điện thoại thành công!', true);
                setTimeout(() => {
                    const box = document.getElementById('profileAlert');
                    if (box) box.style.display = 'none';
                }, 3500);
            } else {
                showProfileAlert(data.message || 'Lỗi khi cập nhật thông tin!', false);
            }
        } catch (err) {
            showProfileAlert('Lỗi kết nối máy chủ! Vui lòng thử lại sau.', false);
        } finally {
            btn.disabled = false;
            btn.innerText = '💾 Lưu thay đổi thông tin';
        }
    };

    function getInitials(name) {
        if (!name) return 'KH';
        const words = name.trim().split(' ');
        if (words.length >= 2) {
            return (words[words.length - 2][0] + words[words.length - 1][0]).toUpperCase();
        } else if (words.length === 1 && words[0].length >= 2) {
            return words[0].substring(0, 2).toUpperCase();
        }
        return 'KH';
    }

    function resetModalState() {
        const currentAvatar = localStorage.getItem('userAvatarUrl');
        setModalPreview(currentAvatar);
        const urlInput = document.getElementById('avatarUrlInput');
        if (urlInput) urlInput.value = currentAvatar && !currentAvatar.startsWith('data:') && !currentAvatar.startsWith('/uploads/') ? currentAvatar : '';
        const fileInput = document.getElementById('avatarFileInput');
        if (fileInput) fileInput.value = '';
        const alertAv = document.getElementById('avatarAlert');
        if (alertAv) alertAv.style.display = 'none';
        const alertPw = document.getElementById('passwordAlert');
        if (alertPw) alertPw.style.display = 'none';
        const pwForm = document.getElementById('changePasswordForm');
        if (pwForm) pwForm.reset();
    }

    function setModalPreview(url) {
        const preview = document.getElementById('modalAvatarPreview');
        if (!preview) return;
        if (url) {
            preview.innerHTML = `<img src="${url}" style="width: 100%; height: 100%; border-radius: 50%; object-fit: cover;">`;
        } else {
            const name = localStorage.getItem('hoTen') || 'KH';
            preview.innerText = getInitials(name);
        }
    }

    window.applyAvatarFromUrl = function() {
        const urlInput = document.getElementById('avatarUrlInput');
        const val = urlInput ? urlInput.value.trim() : '';
        if (!val) {
            showAvatarAlert('Vui lòng nhập đường dẫn hình ảnh hợp lệ!', false);
            return;
        }
        setModalPreview(val);
        const fileInput = document.getElementById('avatarFileInput');
        if (fileInput) fileInput.value = '';
        showAvatarAlert('Đã áp dụng xem trước link ảnh. Bấm "Lưu Avatar" để lưu vào hệ thống.', true);
    };

    // Lưu cài đặt Avatar lên Backend (Không lưu cột DB)
    window.saveAvatarSettings = async function() {
        const token = getAuthToken();
        const btn = document.getElementById('btnSaveAvatar');
        const fileInput = document.getElementById('avatarFileInput');
        const urlInput = document.getElementById('avatarUrlInput');

        if (!token) {
            showAvatarAlert('Phiên làm việc hết hạn. Vui lòng đăng nhập lại!', false);
            return;
        }

        btn.disabled = true;
        btn.innerText = 'Đang lưu...';

        try {
            // Trường hợp 1: Có file upload từ máy tính
            if (fileInput && fileInput.files && fileInput.files.length > 0) {
                const formData = new FormData();
                formData.append('file', fileInput.files[0]);

                const res = await fetch('/api/auth/avatar/upload', {
                    method: 'POST',
                    headers: { 'Authorization': 'Bearer ' + token },
                    credentials: 'include',
                    body: formData
                });
                const data = await res.json();

                if (res.ok && data.success) {
                    const newUrl = data.data.avatarUrl;
                    localStorage.setItem('userAvatarUrl', newUrl);
                    if (currentUserProfile) currentUserProfile.avatarUrl = newUrl;
                    renderAvatars(newUrl);
                    loadAccountInfo();
                    showAvatarAlert('✓ Đã tải lên và lưu ảnh đại diện thành công!', true);
                    setTimeout(closeAccountSettingsModal, 900);
                } else {
                    showAvatarAlert(data.message || 'Lỗi khi tải ảnh đại diện lên máy chủ!', false);
                }
            }
            // Trường hợp 2: Có link URL ảnh
            else if (urlInput && urlInput.value.trim()) {
                const linkUrl = urlInput.value.trim();
                const res = await fetch('/api/auth/avatar/link', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + token
                    },
                    credentials: 'include',
                    body: JSON.stringify({ avatarUrl: linkUrl })
                });
                const data = await res.json();

                if (res.ok && data.success) {
                    const newUrl = data.data.avatarUrl;
                    localStorage.setItem('userAvatarUrl', newUrl);
                    if (currentUserProfile) currentUserProfile.avatarUrl = newUrl;
                    renderAvatars(newUrl);
                    loadAccountInfo();
                    showAvatarAlert('✓ Đã lưu link ảnh đại diện thành công!', true);
                    setTimeout(closeAccountSettingsModal, 900);
                } else {
                    showAvatarAlert(data.message || 'Lỗi khi cập nhật link ảnh đại diện!', false);
                }
            } else {
                showAvatarAlert('Vui lòng chọn ảnh từ máy tính hoặc dán link ảnh trước khi lưu!', false);
            }
        } catch (err) {
            showAvatarAlert('Lỗi kết nối máy chủ! Vui lòng thử lại sau.', false);
        } finally {
            btn.disabled = false;
            btn.innerText = '✓ Lưu Avatar';
        }
    };

    // Xóa ảnh đại diện (trở về mặc định) trên Backend
    window.removeAvatar = async function() {
        const token = getAuthToken();
        if (!token) {
            showAvatarAlert('Phiên làm việc hết hạn. Vui lòng đăng nhập lại!', false);
            return;
        }

        try {
            const res = await fetch('/api/auth/avatar/remove', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                credentials: 'include'
            });
            const data = await res.json();

            if (res.ok && data.success) {
                localStorage.removeItem('userAvatarUrl');
                if (currentUserProfile) currentUserProfile.avatarUrl = null;
                setModalPreview(null);
                const urlInput = document.getElementById('avatarUrlInput');
                if (urlInput) urlInput.value = '';
                const fileInput = document.getElementById('avatarFileInput');
                if (fileInput) fileInput.value = '';

                renderAvatars(null);
                loadAccountInfo();
                showAvatarAlert('✓ Đã xóa ảnh đại diện về mặc định!', true);
                setTimeout(closeAccountSettingsModal, 900);
            } else {
                showAvatarAlert(data.message || 'Lỗi khi xóa ảnh đại diện!', false);
            }
        } catch (err) {
            showAvatarAlert('Lỗi kết nối máy chủ khi xóa ảnh!', false);
        }
    };

    function showAvatarAlert(msg, isSuccess) {
        const box = document.getElementById('avatarAlert');
        if (!box) return;
        box.style.display = 'block';
        box.style.background = isSuccess ? '#DCFCE7' : '#FEE2E2';
        box.style.color = isSuccess ? '#15803D' : '#B91C1C';
        box.innerText = msg;
    }

    function showPasswordAlert(msg, isSuccess) {
        const box = document.getElementById('passwordAlert');
        if (!box) return;
        box.style.display = 'block';
        box.style.background = isSuccess ? '#DCFCE7' : '#FEE2E2';
        box.style.color = isSuccess ? '#15803D' : '#B91C1C';
        box.innerText = msg;
    }

    // 3. Xử lý Đổi Mật Khẩu
    window.handleChangePassword = async function(e) {
        e.preventDefault();
        const oldPassword = document.getElementById('oldPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmNewPassword = document.getElementById('confirmNewPassword').value;
        const btn = document.getElementById('btnSubmitChangePass');

        if (newPassword.length < 6) {
            showPasswordAlert('Mật khẩu mới phải có ít nhất 6 ký tự!', false);
            return;
        }

        if (newPassword !== confirmNewPassword) {
            showPasswordAlert('Mật khẩu xác nhận không khớp với mật khẩu mới!', false);
            return;
        }

        const token = getAuthToken();
        if (!token) {
            showPasswordAlert('Phiên làm việc hết hạn. Vui lòng đăng nhập lại!', false);
            return;
        }

        btn.disabled = true;
        btn.innerText = 'Đang xử lý...';

        try {
            const res = await fetch('/api/auth/change-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                credentials: 'include',
                body: JSON.stringify({
                    matKhauCu: oldPassword,
                    matKhauMoi: newPassword,
                    xacNhanMatKhau: confirmNewPassword
                })
            });

            const data = await res.json();
            if (res.ok && data.success) {
                showPasswordAlert('✓ Đổi mật khẩu thành công!', true);
                document.getElementById('changePasswordForm').reset();
                setTimeout(closeAccountSettingsModal, 1500);
            } else {
                showPasswordAlert(data.message || 'Mật khẩu hiện tại không đúng hoặc lỗi cập nhật!', false);
            }
        } catch (err) {
            showPasswordAlert('Lỗi kết nối máy chủ! Vui lòng thử lại sau.', false);
        } finally {
            btn.disabled = false;
            btn.innerText = '🔒 Đổi mật khẩu';
        }
    };

    // Khởi tạo khi DOM sẵn sàng
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            injectAccountModal();
            applyUserAvatarAndName();
        });
    } else {
        injectAccountModal();
        applyUserAvatarAndName();
    }
})();
