
    // Navigation effect on scroll
    window.addEventListener('scroll', function() {
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
    anchor.addEventListener('click', function(e) {
        e.preventDefault();
        const targetElement = document.querySelector(this.getAttribute('href'));
        if (targetElement) {
            targetElement.scrollIntoView({
                behavior: 'smooth'
            });
        }
    });
});

    // Active menu item for navbar
    document.addEventListener('DOMContentLoaded', function() {
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
});


    /*<![CDATA[*/
    document.addEventListener('DOMContentLoaded', function() {
    // === KHAI BÁO BIẾN ===
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

    // ================================================================
    // === HÀM ĐÃ ĐƯỢC SỬA LỖI CẤU TRÚC HTML ===
    // ================================================================
    function createNotificationElement(notification) {
    // Dùng thẻ <li> làm thẻ cha thay vì <a>
    const listItem = document.createElement('li');
    listItem.className = 'notification-item';
    listItem.classList.toggle('unread', !notification.read);
    listItem.dataset.id = notification.id;

    // Tạo icon
    const iconDiv = document.createElement('div');
    iconDiv.className = 'icon';
    iconDiv.innerHTML = '<i class="fas fa-bell"></i>';

    // Tạo khối nội dung chính
    const contentDiv = document.createElement('div');
    contentDiv.className = 'content';

    const titleDiv = document.createElement('div');
    titleDiv.className = 'title';
    // Nếu có link, biến tiêu đề thành thẻ <a>
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

    // Tạo nút xóa riêng biệt
    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'delete-btn';
    deleteBtn.innerHTML = '&times;'; // Dấu 'x'
    deleteBtn.title = 'Xóa thông báo';
    deleteBtn.addEventListener('click', function(e) {
    e.preventDefault();
    e.stopPropagation();
    deleteNotification(notification.id);
});

    // Gắn các thành phần vào thẻ <li>
    listItem.appendChild(iconDiv);
    listItem.appendChild(contentDiv);
    listItem.appendChild(deleteBtn);

    return listItem;
}

    // Các hàm khác giữ nguyên
    function checkEmptyState() {
    const hasItems = notificationList.children.length > 0;
    emptyState.style.display = hasItems ? 'none' : 'block';
    loadMoreBtn.style.display = hasItems && !isLastPage ? 'block' : 'none';
}

    function renderNotifications(notifications, append = false) {
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
    loadMoreBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
    loadMoreBtn.disabled = true;

    try {
    const response = await fetch(`/api/notifications?page=${page}&size=5`);
    if (!response.ok) throw new Error('Failed to fetch notifications');

    const pageData = await response.json();
    renderNotifications(pageData.content, page > 0);

    isLastPage = pageData.last;
    if (isLastPage) {
    loadMoreBtn.style.display = 'none';
}

} catch (err) {
    console.error('Lỗi tải thông báo:', err);
    loadMoreBtn.innerHTML = 'Không thể tải thêm';
} finally {
    isLoading = false;
    if (!isLastPage) {
    loadMoreBtn.innerHTML = 'Tải thêm';
    loadMoreBtn.disabled = false;
}
    checkEmptyState();
}
}

    async function deleteNotification(id) {
    if (!confirm('Bạn có chắc muốn xóa thông báo này?')) return;
    try {
    const response = await fetch(`/api/notifications/${id}`, { method: 'DELETE' });
    if (response.ok) {
    const itemToRemove = document.querySelector(`.notification-item[data-id='${id}']`);
    if(itemToRemove) itemToRemove.remove();
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
    await fetch('/api/notifications/mark-all-as-read', { method: 'POST' });
    notificationBadge.style.display = 'none';
    document.querySelectorAll('.notification-item.unread').forEach(item => {
    item.classList.remove('unread');
});
} catch (err) {
    console.error('Lỗi đánh dấu đã đọc:', err);
}
}
}

    // === GÁN SỰ KIỆN ===

    if (notificationDropdownToggle) {
    fetchNotifications(0);

    loadMoreBtn.addEventListener('click', function() {
    currentNotificationPage++;
    fetchNotifications(currentNotificationPage);
});

    notificationDropdownToggle.addEventListener('show.bs.dropdown', markAllAsRead);

    markAllReadBtn.addEventListener('click', function(e){
    e.preventDefault();
    markAllAsRead();
});
}
});