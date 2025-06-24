document.addEventListener('DOMContentLoaded', function() {
    // --- LOGIC CHUNG CHO NAVBAR ---
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
    const currentPath = window.location.pathname;

    navLinks.forEach(link => {
        const linkPath = new URL(link.href, window.location.origin).pathname;
        const normalizedCurrentPath = (currentPath !== '/' && currentPath.endsWith('/')) ? currentPath.slice(0, -1) : currentPath;
        const normalizedLinkPath = (linkPath !== '/' && linkPath.endsWith('/')) ? linkPath.slice(0, -1) : linkPath;

        if (link.getAttribute('href') === '/') { // Trang chủ là trường hợp đặc biệt
            if (normalizedCurrentPath === '' || normalizedCurrentPath === '/index.html' || normalizedCurrentPath === '/') {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        } else {
            if (normalizedCurrentPath.startsWith(normalizedLinkPath) && normalizedLinkPath !== '') {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        }
    });
// Navigation effect on scroll
    window.addEventListener('scroll', function () {
        const navbar = document.querySelector('.navbar');
        if (navbar) {
            if (window.scrollY > 50) {
                navbar.style.padding = '8px 0';
                navbar.style.boxShadow = '0 2px 20px rgba(139, 69, 19, 0.1)';
            } else {
                navbar.style.padding = '12px 0';
                navbar.style.boxShadow = '0 2px 10px rgba(139, 69, 19, 0.07)';
            }
        }
    });

// Smooth scrolling for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const targetElement = document.querySelector(this.getAttribute('href'));
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        });
    });

    const notificationDropdown = document.querySelector('.notification-dropdown');
    const notificationList = document.getElementById('notificationList');
    const notificationBadge = document.getElementById('notificationBadge');
    const notificationDropdownToggle = document.getElementById('notificationDropdownToggle');
    const markAllReadBtn = document.getElementById('markAllReadBtn');
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    const emptyState = document.getElementById('notificationEmptyState');

    // Biến trạng thái
    let currentNotificationPage = 0;
    let isLastPage = false;
    let isLoading = false;

    // === CÁC HÀM XỬ LÝ ===

    function formatTimeAgo(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        const now = new Date();
        const seconds = Math.floor((now - date) / 1000);
        let interval = seconds / 31536000;
        if (interval > 1) return Math.floor(interval) + " năm trước";
        interval = seconds / 2592000;
        if (interval > 1) return Math.floor(interval) + " tháng trước";
        interval = seconds / 86400;
        if (interval > 1) return Math.floor(interval) + " ngày trước";
        interval = seconds / 3600;
        if (interval > 1) return Math.floor(interval) + " giờ trước";
        interval = seconds / 60;
        if (interval > 1) return Math.floor(interval) + " phút trước";
        return "Vừa xong";
    }

    function createNotificationElement(notification) {
        const listItem = document.createElement('li');
        listItem.className = 'notification-item';
        listItem.classList.toggle('unread', !notification.read);
        listItem.dataset.id = notification.id;
        const iconDiv = document.createElement('div');
        iconDiv.className = 'icon';
        iconDiv.innerHTML = '<i class="fas fa-bell"></i>';
        const contentDiv = document.createElement('div');
        contentDiv.className = 'content';
        const titleDiv = document.createElement('div');
        titleDiv.className = 'title';
        if (notification.link) {
            const titleLink = document.createElement('a');
            titleLink.href = notification.link;
            titleLink.textContent = notification.title;
            titleLink.className = 'text-decoration-none text-reset';
            titleDiv.appendChild(titleLink);
        } else {
            titleDiv.textContent = notification.title;
        }
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message';
        messageDiv.textContent = notification.message;
        const timestampDiv = document.createElement('div');
        timestampDiv.className = 'timestamp';
        timestampDiv.textContent = formatTimeAgo(notification.createdAt);
        contentDiv.appendChild(titleDiv);
        contentDiv.appendChild(messageDiv);
        contentDiv.appendChild(timestampDiv);
        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'delete-btn';
        deleteBtn.innerHTML = '&times;';
        deleteBtn.title = 'Xóa thông báo';
        deleteBtn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            deleteNotification(notification.id);
        });
        listItem.appendChild(iconDiv);
        listItem.appendChild(contentDiv);
        listItem.appendChild(deleteBtn);
        return listItem;
    }

    function checkEmptyState() {
        if (notificationList) {
            const hasItems = notificationList.children.length > 0;
            if (emptyState) emptyState.style.display = hasItems ? 'none' : 'block';
            if (loadMoreBtn) loadMoreBtn.style.display = hasItems && !isLastPage ? 'block' : 'none';
        }
    }

    function renderNotifications(notifications, append = false) {
        if (!notificationList) return;
        if (!append) {
            notificationList.innerHTML = '';
        }
        notifications.forEach(n => {
            notificationList.appendChild(createNotificationElement(n));
        });
        checkEmptyState();
    }

    async function fetchNotifications(page) {
        if (isLoading || isLastPage) return;
        isLoading = true;
        if (loadMoreBtn) {
            loadMoreBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
            loadMoreBtn.disabled = true;
        }
        try {
            const response = await fetch(`/api/notifications?page=${page}&size=5`);
            if (!response.ok) throw new Error('Failed to fetch notifications');
            const pageData = await response.json();
            renderNotifications(pageData.content, page > 0);
            isLastPage = pageData.last;
            if (isLastPage && loadMoreBtn) {
                loadMoreBtn.style.display = 'none';
            }
        } catch (err) {
            console.error('Lỗi tải thông báo:', err);
            if (loadMoreBtn) loadMoreBtn.innerHTML = 'Không thể tải thêm';
        } finally {
            isLoading = false;
            if (!isLastPage && loadMoreBtn) {
                loadMoreBtn.innerHTML = 'Tải thêm';
                loadMoreBtn.disabled = false;
            }
            checkEmptyState();
        }
    }

    async function deleteNotification(id) {
        if (!confirm('Bạn có chắc muốn xóa thông báo này?')) return;
        try {
            const response = await fetch(`/api/notifications/${id}`, {method: 'DELETE'});
            if (response.ok) {
                const itemToRemove = document.querySelector(`.notification-item[data-id='${id}']`);
                if (itemToRemove) itemToRemove.remove();
                checkEmptyState();
            } else {
                alert('Lỗi khi xóa thông báo.');
            }
        } catch (err) {
            console.error('Lỗi xóa thông báo:', err);
        }
    }

    async function markAllAsRead() {
        if (notificationBadge && parseInt(notificationBadge.textContent) > 0) {
            try {
                await fetch('/api/notifications/mark-all-as-read', {method: 'POST'});
                notificationBadge.style.display = 'none';
                document.querySelectorAll('.notification-item.unread').forEach(item => {
                    item.classList.remove('unread');
                });
            } catch (err) {
                console.error('Lỗi đánh dấu đã đọc:', err);
            }
        }
    }

    if (notificationDropdownToggle) {
        fetchNotifications(0);
        if (loadMoreBtn) {
            loadMoreBtn.addEventListener('click', function () {
                currentNotificationPage++;
                fetchNotifications(currentNotificationPage);
            });
        }
        notificationDropdownToggle.addEventListener('show.bs.dropdown', markAllAsRead);
        if (markAllReadBtn) {
            markAllReadBtn.addEventListener('click', function (e) {
                e.preventDefault();
                markAllAsRead();
            });
        }
    }
    // === JAVASCRIPT HOÀN CHỈNH CUỐI CÙNG CHO CHAT WIDGET ===
    const chatWidgetContainer = document.querySelector('.chat-widget-container');
    const chatToggleButton = document.getElementById('chat-toggle-button');
    const chatBody = document.getElementById('chat-body');
    const chatInput = document.getElementById('chat-input');
    const chatSendBtn = document.getElementById('chat-send-btn');
    const faqContainer = document.getElementById('faq-container');
    const thinkingIndicator = document.getElementById('thinking-indicator');
    const closeChatboxBtn = document.getElementById('close-chatbox-btn'); // Đã thêm

    // --- Biến trạng thái ---
    let welcomeSequenceShown = false;
    let isAiThinking = false;

    // === CÁC HÀM TIỆN ÍCH ===
    function formatMessageTime(date) {
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        return `${hours}:${minutes}`;
    }

    function addMessage(text, type) {
        const wrapper = document.createElement('div');
        wrapper.className = `message-wrapper ${type}`;
        const bubble = document.createElement('div');
        bubble.className = 'message-content';
        bubble.innerHTML = text.replace(/\n/g, '<br>');
        const meta = document.createElement('div');
        meta.className = 'message-meta';
        const timestamp = document.createElement('span');
        timestamp.className = 'message-timestamp';
        timestamp.textContent = formatMessageTime(new Date());
        meta.appendChild(timestamp);
        wrapper.appendChild(bubble);
        wrapper.appendChild(meta);
        if (type === 'user') {
            const status = document.createElement('span');
            status.className = 'message-status';
            status.innerHTML = '<i class="fas fa-check"></i> Đã gửi';
            meta.appendChild(status);
        }
        chatBody.insertBefore(wrapper, thinkingIndicator);
        chatBody.scrollTop = chatBody.scrollHeight;
        return wrapper;
    }

    // === CÁC HÀM LOGIC CHÍNH ===
    async function sendMessage(messageText) {
        const userMessage = messageText || chatInput.value.trim();
        if (userMessage === '' || isAiThinking) return;
        isAiThinking = true;
        let userMessageElement;
        if (!messageText) {
            userMessageElement = addMessage(userMessage, 'user');
        } else {
            const userMessages = chatBody.querySelectorAll('.message-wrapper.user');
            userMessageElement = userMessages[userMessages.length - 1];
        }
        chatInput.value = '';
        if (faqContainer) faqContainer.style.display = 'none';
        setTimeout(() => {
            if (userMessageElement) {
                const statusEl = userMessageElement.querySelector('.message-status');
                if (statusEl) {
                    statusEl.innerHTML = '<i class="fas fa-check-double"></i> Đã xem';
                }
            }
        }, 800);
        thinkingIndicator.style.display = 'flex';
        chatBody.scrollTop = chatBody.scrollHeight;
        try {
            const response = await fetch('/api/ai/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: userMessage })
            });
            if (!response.ok) throw new Error('API response error');
            const data = await response.json();
            setTimeout(() => {
                thinkingIndicator.style.display = 'none';
                addMessage(data.reply, 'staff');
            }, 1200);
        } catch (error) {
            console.error('Lỗi khi gửi tin nhắn:', error);
            thinkingIndicator.style.display = 'none';
            addMessage('Xin lỗi, đã có lỗi xảy ra. Bạn thử lại nhé.', 'staff');
        } finally {
            isAiThinking = false;
        }
    }

    function showFaqButtons() {
        if (!faqContainer) return;
        faqContainer.innerHTML = '';
        faqContainer.style.display = 'flex';
        const faqs = [
            { question: "Giờ mở cửa?", prompt: "Thư viện mở cửa lúc mấy giờ?" },
            { question: "Quy định mượn sách?", prompt: "Quy định mượn sách của sinh viên là gì?" },
            { question: "Làm sao để gia hạn?", prompt: "Làm thế nào để gia hạn sách?" }
        ];
        faqs.forEach(faq => {
            const button = document.createElement('button');
            button.className = 'faq-button';
            button.textContent = faq.question;
            button.addEventListener('click', () => {
                addMessage(faq.prompt, 'user');
                sendMessage(faq.prompt);
            });
            faqContainer.appendChild(button);
        });
        chatBody.scrollTop = chatBody.scrollHeight;
    }

    function startWelcomeSequence() {
        if (welcomeSequenceShown) return;
        welcomeSequenceShown = true;
        chatBody.innerHTML = '';
        chatBody.appendChild(thinkingIndicator);
        if (faqContainer) chatBody.appendChild(faqContainer);
        thinkingIndicator.style.display = 'flex';
        setTimeout(() => {
            thinkingIndicator.style.display = 'none';
            addMessage("Dạ em là bé heo trợ lí AI thông minh của thư viện số UEF, anh chị cần em hỗ trợ gì ạ 😊", 'staff');
            setTimeout(() => {
                showFaqButtons();
            }, 2000);
        }, 1500);
    }

    // === GÁN CÁC SỰ KIỆN ===
    if (chatToggleButton) {
        chatToggleButton.addEventListener('click', (event) => {
            event.stopPropagation();
            const isActive = chatWidgetContainer.classList.toggle('active');
            if (isActive) {
                startWelcomeSequence();
            }
        });
    }

    if (closeChatboxBtn) { // Đã thêm
        closeChatboxBtn.addEventListener('click', (event) => {
            event.stopPropagation();
            chatWidgetContainer.classList.remove('active');
        });
    }

    if (chatSendBtn) {
        chatSendBtn.addEventListener('click', () => sendMessage());
    }

    if (chatInput) {
        chatInput.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault();
                sendMessage();
            }
        });
    }
});
document.addEventListener('DOMContentLoaded', function() {

    // === KHAI BÁO BIẾN CHUNG ===
    const editProfileBtn = document.getElementById('editProfileBtn');
    const editProfileModalEl = document.getElementById('editProfileModal');
    const firstLoginModalEl = document.getElementById('firstLoginModal');

    // === LOGIC CHUNG CHO NAVBAR, CHAT, NOTIFICATION... ===
    // (Toàn bộ code xử lý chung khác của bạn đặt ở đây)

    // === LOGIC CHO POPUP CHỈNH SỬA PROFILE ===
    if (editProfileBtn && editProfileModalEl) {
        const editProfileModal = new bootstrap.Modal(editProfileModalEl);
        const saveProfileChangesBtn = document.getElementById('saveProfileChangesBtn');

        editProfileBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            try {
                const response = await fetch('/reader/api/profile');
                if (!response.ok) { throw new Error('Không thể tải thông tin. Vui lòng đăng nhập lại.'); }
                const user = await response.json();

                document.getElementById('profileUsername').value = user.username;
                document.getElementById('profileEmail').value = user.userDetail.email || '';
                document.getElementById('profileFullName').value = user.userDetail.fullName || '';
                document.getElementById('profilePhone').value = user.userDetail.phone || '';
                document.getElementById('profileAddress').value = user.userDetail.address || '';
                document.getElementById('profileDob').value = user.userDetail.dob || '';
                document.getElementById('profileGender').value = user.userDetail.gender || 'Chưa xác định';

                editProfileModal.show();
            } catch (error) {
                alert('Lỗi: ' + error.message);
            }
        });

        if (saveProfileChangesBtn) {
            saveProfileChangesBtn.addEventListener('click', async () => {
                const formData = {
                    fullName: document.getElementById('profileFullName').value,
                    phone: document.getElementById('profilePhone').value,
                    address: document.getElementById('profileAddress').value,
                    dob: document.getElementById('profileDob').value,
                    gender: document.getElementById('profileGender').value,
                };

                let errors = [];
                if (/\d/.test(formData.fullName)) { errors.push("Họ và Tên không được chứa số."); }
                if (!/^\d{10}$/.test(formData.phone)) { errors.push("Số điện thoại phải gồm đúng 10 chữ số."); }

                if (errors.length > 0) {
                    alert("Vui lòng sửa các lỗi sau:\n- " + errors.join("\n- "));
                    return;
                }

                try {
                    const response = await fetch('/reader/api/update', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(formData)
                    });
                    const result = await response.json();
                    if(response.ok) {
                        editProfileModal.hide();
                        alert(result.message);
                        const nameSpan = document.getElementById('userDropdownFullName');
                        if (nameSpan) nameSpan.textContent = result.newFullName;
                    } else { alert('Lỗi: ' + (result.message || 'Không thể cập nhật.')); }
                } catch (error) { alert('Lỗi kết nối, vui lòng thử lại.'); }
            });
        }
    }


});