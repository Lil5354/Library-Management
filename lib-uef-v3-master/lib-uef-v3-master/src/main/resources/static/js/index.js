// Animate elements on scroll - Intersection Observer for cards
document.addEventListener('DOMContentLoaded', function() {
    const cardElements = document.querySelectorAll('.about-card, .vision-card, .mission-card, .team-card');
    if (cardElements.length > 0) {
        const cardObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    // cardObserver.unobserve(entry.target); // Optional: unobserve after animation
                }
            });
        }, { threshold: 0.15 }); // Adjust threshold as needed

        cardElements.forEach(el => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';
            el.style.transition = 'opacity 0.6s ease-out, transform 0.6s ease-out';
            cardObserver.observe(el);
        });
    }

    // Intersection Observer for timeline items
    const timelineItems = document.querySelectorAll('.timeline-item');
    if (timelineItems.length > 0) {
        const timelineObserver = new IntersectionObserver((entries) => {
            entries.forEach((entry) => { // Removed index as direct setTimeout staggering might conflict with observer reuse
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    // timelineObserver.unobserve(entry.target); // Optional: unobserve
                }
            });
        }, { threshold: 0.1 }); // Adjust threshold

        timelineItems.forEach((item, index) => { // Use index here for initial staggered delay if desired, but ensure CSS transition handles the visual
            item.style.opacity = '0';
            item.style.transform = 'translateY(30px)';
            item.style.transition = `opacity 0.5s ease ${index * 0.1}s, transform 0.5s ease ${index * 0.1}s`; // Staggered transition delay
            timelineObserver.observe(item);
        });
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
document.querySelectorAll('a[href^="#"]:not([href="#"])').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();

        const targetId = this.getAttribute('href');
        try {
            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        } catch (error) {
            console.error("Lỗi khi cuộn đến phần tử:", targetId, error);
        }
    });
});

document.addEventListener('DOMContentLoaded', function() {
    // === LOGIC CHO CART MƯỢN SÁCH ===
    const bookContainer = document.querySelector('.book-list-container');
    const cartContainer = document.getElementById('borrowCartContainer');
    const cartIcon = document.getElementById('borrowCartIcon');
    const cartPanel = document.getElementById('borrowCartPanel');
    const closeCartBtn = document.getElementById('closeCartBtn');
    const cartItemList = document.getElementById('cartItemList');
    const cartCountBadge = document.getElementById('cartCountBadge');
    const borrowAllBtn = document.getElementById('borrowAllBtn');
    const clearCartBtn = document.getElementById('clearCartBtn');

// Khởi tạo giỏ hàng
    let borrowCart = [];

// === CÁC HÀM XỬ LÝ GIỎ HÀNG ===

// --- SỬA LỖI 1: LƯU VÀ TẢI GIỎ HÀNG TỪ SESSIONSTORAGE ---
    function saveCartToSession() {
        sessionStorage.setItem('borrowCart', JSON.stringify(borrowCart));
    }

    function loadCartFromSession() {
        const savedCart = sessionStorage.getItem('borrowCart');
        if (savedCart) {
            borrowCart = JSON.parse(savedCart);
        }
    }
// -----------------------------------------------------

    function updateCartUI() {
        if (!cartCountBadge || !cartItemList) return;

        cartCountBadge.textContent = borrowCart.length;
        cartCountBadge.style.display = borrowCart.length > 0 ? 'flex' : 'none';
        cartItemList.innerHTML = '';

        if (borrowCart.length === 0) {
            cartItemList.innerHTML = '<p class="text-center text-muted p-3 empty-cart-message">Giỏ sách của bạn đang trống.</p>';
            if(borrowAllBtn) borrowAllBtn.disabled = true;
            if(clearCartBtn) clearCartBtn.style.display = 'none';
        } else {
            borrowCart.forEach(book => {
                const itemHTML = `
                <div class="cart-item" data-id="${book.id}">
                    <img src="${book.cover || 'https://source.unsplash.com/500x650/?book'}" class="cart-item-img">
                    <div class="cart-item-info">
                        <div class="cart-item-title">${book.title}</div>
                    </div>
                    <button class="btn btn-sm btn-outline-danger remove-item-btn" title="Xóa khỏi giỏ">&times;</button>
                </div>`;
                cartItemList.insertAdjacentHTML('beforeend', itemHTML);
            });
            if(borrowAllBtn) borrowAllBtn.disabled = false;
            if(clearCartBtn) clearCartBtn.style.display = 'inline-block';
        }
    }

    function addToCart(book) {
        if (borrowCart.some(item => item.id === book.id)) {
            alert('Sách này đã có trong giỏ của bạn.');
            return;
        }
        if (borrowCart.length >= 5) {
            alert('Bạn chỉ có thể thêm tối đa 5 sách vào giỏ.');
            return;
        }
        borrowCart.push(book);
        saveCartToSession(); // <-- Lưu vào session
        updateCartUI();
        cartContainer.classList.add('active');
    }

    function removeFromCart(bookId) {
        borrowCart = borrowCart.filter(item => String(item.id) !== String(bookId));
        saveCartToSession(); // <-- Lưu vào session
        updateCartUI();
    }

// === GÁN SỰ KIỆN ===
    if (cartIcon) {
        cartIcon.addEventListener('click', () => cartContainer.classList.toggle('active'));
    }
    if (closeCartBtn) {
        closeCartBtn.addEventListener('click', () => cartContainer.classList.remove('active'));
    }

    if (bookContainer) {
        bookContainer.addEventListener('click', e => {
            const button = e.target.closest('.btn-add-to-cart');
            if (button) {
                e.preventDefault();
                const book = {
                    id: button.dataset.bookId,
                    title: button.dataset.bookTitle,
                    cover: button.dataset.bookCover
                };
                addToCart(book);
            }
        });
    }

    if(cartItemList){
        cartItemList.addEventListener('click', e => {
            const removeButton = e.target.closest('.remove-item-btn');
            if (removeButton) {
                e.preventDefault();
                const bookId = removeButton.closest('.cart-item').dataset.id;
                removeFromCart(bookId);
            }
        });
    }

    if(clearCartBtn) {
        clearCartBtn.addEventListener('click', () => {
            if(confirm('Bạn có chắc muốn xóa tất cả sách khỏi giỏ?')) {
                borrowCart = [];
                saveCartToSession(); // <-- Lưu vào session
                updateCartUI();
            }
        });
    }

    if(borrowAllBtn) {
        borrowAllBtn.addEventListener('click', async () => {
            if (borrowCart.length === 0) return;

            const bookIds = borrowCart.map(item => item.id);

            borrowAllBtn.disabled = true;
            borrowAllBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang xử lý...';

            try {
                // Yêu cầu fetch đã được đơn giản hóa, không cần header CSRF
                const response = await fetch('/borrow/multiple', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(bookIds)
                });

                // Phản hồi từ server có thể không phải là JSON nếu có lỗi
                if (!response.ok) {
                    // Đọc lỗi dưới dạng text để tránh lỗi parse JSON
                    const errorText = await response.text();
                    throw new Error(`Lỗi từ server: ${response.status} - ${errorText}`);
                }

                const result = await response.json();
                alert(result.message);
                borrowCart = [];
                saveCartToSession();
                updateCartUI();
                cartContainer.classList.remove('active');
                window.location.reload();

            } catch(e){
                console.error("Lỗi chi tiết khi mượn sách:", e);
                alert("Mượn sách thất bại. Vui lòng kiểm tra số lượng sách bạn đã mượn.");
            } finally {
                // Khối này đảm bảo nút bấm luôn được reset
                borrowAllBtn.disabled = false;
                borrowAllBtn.textContent = 'Hoàn tất mượn sách';
            }
        });
    }
    loadCartFromSession();
    updateCartUI();
    // === LOGIC CHO POPUP LỊCH SỬ MƯỢN SÁCH ===
    const borrowHistoryBtn = document.getElementById('borrowHistoryBtn');
    const borrowHistoryModalEl = document.getElementById('borrowHistoryModal');

    if (borrowHistoryBtn && borrowHistoryModalEl) {
        const borrowHistoryModal = new bootstrap.Modal(borrowHistoryModalEl);
        const historyList = document.getElementById('borrowHistoryList');
        const historyPagination = document.getElementById('borrowHistoryPagination');

        async function fetchHistory(page = 0) {
            historyList.innerHTML = '<div class="text-center p-5"><div class="spinner-border text-primary"></div></div>';
            historyPagination.innerHTML = ''; // Xóa phân trang cũ trong lúc tải

            try {
                const response = await fetch(`/reader/api/borrowing-history?page=${page}&size=5`);
                if (!response.ok) throw new Error('Không thể tải lịch sử.');

                const pageData = await response.json();

                historyList.innerHTML = '';
                if (pageData.empty) {
                    historyList.innerHTML = '<p class="text-center text-muted p-5">Bạn chưa có lịch sử mượn sách nào.</p>';
                } else {
                    pageData.content.forEach(item => {
                        const borrowDate = new Date(item.borrowDate).toLocaleDateString('vi-VN');
                        const dueDate = new Date(item.dueDate).toLocaleDateString('vi-VN');
                        const returnDate = item.returnDate ? new Date(item.returnDate).toLocaleDateString('vi-VN') : 'Chưa trả';

                        let statusBadge = '';
                        if (item.status === 'RETURNED') {
                            statusBadge = '<span class="status-badge status-returned">Đã trả</span>';
                        } else if (new Date() > new Date(item.dueDate)) {
                            statusBadge = '<span class="status-badge status-overdue">Quá hạn</span>';
                        } else {
                            statusBadge = '<span class="status-badge status-borrowed">Đang mượn</span>';
                        }

                        const itemHTML = `
                        <div class="borrowed-item">
                            <img src="${item.bookCoverImage || 'https://source.unsplash.com/500x650/?book'}" class="borrowed-item-cover">
                            <div class="borrowed-item-info">
                                <h6>${item.bookTitle}</h6>
                                <p>Ngày mượn: ${borrowDate} | Hạn trả: ${dueDate} | Ngày trả: ${returnDate}</p>
                                <div>Trạng thái: ${statusBadge}</div>
                                ${item.lateFee > 0 ? `<div class="fee-text">Phí phạt: ${item.lateFee.toLocaleString('vi-VN')}đ</div>` : ''}
                            </div>
                            <button class="btn btn-sm btn-outline-primary re-borrow-btn" 
                                data-book-id="${item.bookId}" data-book-title="${item.bookTitle}" data-book-cover="${item.bookCoverImage}"
                                title="Mượn lại sách này">
                                <i class="fas fa-cart-plus"></i> Mượn lại
                            </button>
                        </div>`;
                        historyList.insertAdjacentHTML('beforeend', itemHTML);
                    });
                }
                renderHistoryPagination(pageData);
            } catch (error) {
                historyList.innerHTML = `<p class="text-center text-danger p-5">${error.message}</p>`;
            }
        }

        function renderHistoryPagination(page) {
            historyPagination.innerHTML = '';
            if (page.totalPages <= 1) return;

            let paginationHtml = '<ul class="pagination justify-content-center">';
            const prevClass = page.first ? 'disabled' : '';
            paginationHtml += `<li class="page-item ${prevClass}"><a class="page-link" href="#" data-page="${page.number - 1}">&laquo;</a></li>`;

            for (let i = 0; i < page.totalPages; i++) {
                const activeClass = (i === page.number) ? 'active' : '';
                paginationHtml += `<li class="page-item ${activeClass}"><a class="page-link" href="#" data-page="${i}">${i + 1}</a></li>`;
            }

            const nextClass = page.last ? 'disabled' : '';
            paginationHtml += `<li class="page-item ${nextClass}"><a class="page-link" href="#" data-page="${page.number + 1}">&raquo;</a></li>`;
            paginationHtml += '</ul>';
            historyPagination.innerHTML = paginationHtml;
        }

        borrowHistoryBtn.addEventListener('click', (e) => {
            e.preventDefault();
            fetchHistory(0);
            borrowHistoryModal.show();
        });

        historyPagination.addEventListener('click', (e) => {
            e.preventDefault();
            if (e.target.tagName === 'A' && !e.target.parentElement.classList.contains('disabled')) {
                const page = e.target.dataset.page;
                fetchHistory(page);
            }
        });
        historyList.addEventListener('click', (e) => {
            const reborrowButton = e.target.closest('.re-borrow-btn');
            if (reborrowButton) {
                e.preventDefault();
                const bookToAdd = {
                    id: reborrowButton.dataset.bookId,
                    title: reborrowButton.dataset.bookTitle,
                    cover: reborrowButton.dataset.bookCover
                };
                addToCart(bookToAdd);
                alert(`Đã thêm sách '${bookToAdd.title}' vào giỏ.`);
                borrowHistoryModal.hide();
            }
        });
    }

    // === LOGIC CHO POPUP SÁCH ĐANG MƯỢN ===
    const borrowedBooksBtn = document.getElementById('borrowedBooksBtn');
    const borrowedBooksModalEl = document.getElementById('borrowedBooksModal');

    if (borrowedBooksBtn && borrowedBooksModalEl) {
        const borrowedBooksModal = new bootstrap.Modal(borrowedBooksModalEl);
        const borrowedBooksList = document.getElementById('borrowedBooksList');

        borrowedBooksBtn.addEventListener('click', async (e) => {
            e.preventDefault();

            borrowedBooksList.innerHTML = '<div class="text-center p-5"><div class="spinner-border text-primary" role="status"></div></div>';
            borrowedBooksModal.show();

            try {
                const response = await fetch('/reader/api/borrowed-books');
                if (!response.ok) throw new Error('Không thể tải dữ liệu mượn sách.');

                const loanItems = await response.json();

                borrowedBooksList.innerHTML = ''; // Xóa spinner
                if (loanItems.length === 0) {
                    borrowedBooksList.innerHTML = '<p class="text-center text-muted p-5">Bạn chưa mượn cuốn sách nào.</p>';
                } else {
                    loanItems.forEach(item => {
                        const borrowDate = new Date(item.borrowDate).toLocaleDateString('vi-VN');
                        const dueDate = new Date(item.dueDate).toLocaleDateString('vi-VN');

                        const itemHTML = `
                        <div class="borrowed-item">
                            <img src="${item.bookCoverImage || 'https://source.unsplash.com/500x650/?book'}" class="borrowed-item-cover">
                            <div class="borrowed-item-info">
                                <h6>${item.bookTitle}</h6>
                                <p>Tác giả: ${item.bookAuthor}</p>
                                <p>Ngày mượn: ${borrowDate}</p>
                                <p class="due-date">Ngày hẹn trả: ${dueDate}</p>
                            </div>
                        </div>
                    `;
                        borrowedBooksList.insertAdjacentHTML('beforeend', itemHTML);
                    });
                }
            } catch (error) {
                borrowedBooksList.innerHTML = `<p class="text-center text-danger p-5">${error.message}</p>`;
            }
        });
    }
    //XỬ LÝ SỰ KIỆN XEM CHI TIẾT SÁCH
    const bookDetailModalEl = document.getElementById('bookDetailModal');

    if (bookContainer && bookDetailModalEl) {
        const bookDetailModal = new bootstrap.Modal(bookDetailModalEl);

        bookContainer.addEventListener('click', async function(e) {
            // Chỉ hoạt động khi nhấn vào nút có class 'btn-show-detail'
            const detailTrigger = e.target.closest('.btn-show-detail');
            if (detailTrigger) {
                e.preventDefault();
                const bookId = detailTrigger.dataset.bookId;
                if (!bookId) return;
                try {
                    const response = await fetch(`/books/api/details/${bookId}`);
                    if (!response.ok) throw new Error('Không tìm thấy sách.');

                    const book = await response.json();

                    // === BẮT ĐẦU PHẦN CODE HOÀN THIỆN ===
                    // Điền dữ liệu vào các thẻ trong modal
                    document.getElementById('bookDetailTitle').textContent = book.title || 'Không có tiêu đề';
                    document.getElementById('bookDetailCover').src = book.coverImage || 'https://source.unsplash.com/500x650/?book';
                    document.getElementById('bookDetailAuthor').textContent = book.author || 'Chưa rõ';
                    document.getElementById('bookDetailDescription').textContent = book.description || 'Không có mô tả.';
                    document.getElementById('bookDetailCategory').textContent = book.category ? book.category.name : 'Chưa phân loại';
                    document.getElementById('bookDetailYear').textContent = book.publicationYear || 'N/A';
                    document.getElementById('bookDetailPublisher').textContent = book.publisher || 'N/A';
                    document.getElementById('bookDetailIsbn').textContent = book.isbn || 'N/A';
                    document.getElementById('bookDetailAvailable').textContent = book.availableCopies;
                    // === KẾT THÚC PHẦN CODE HOÀN THIỆN ===

                    // Xử lý nút Mượn sách
                    const modalFooter = bookDetailModalEl.querySelector('.modal-footer');
                    // Luôn có nút Đóng
                    modalFooter.innerHTML = '<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>';

                    if (book.availableCopies > 0) {
                        // Nếu còn sách, thêm nút Mượn sách vào trước nút Đóng
                        const borrowForm = `
                        <form action="/borrow/${book.id}" method="post" class="ms-auto">
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-hand-holding-heart me-2"></i>Mượn sách
                            </button>
                        </form>
                    `;
                        modalFooter.insertAdjacentHTML('afterbegin', borrowForm);
                    } else {
                        // Nếu hết sách, thêm thông báo vào trước nút Đóng
                        const outOfStockMessage = '<p class="text-danger mb-0 me-auto">Sách đã được mượn hết.</p>';
                        modalFooter.insertAdjacentHTML('afterbegin', outOfStockMessage);
                    }

                    bookDetailModal.show();

                } catch (error) {
                    alert(error.message);
                }
            }
        });
    }
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
        const avatarFileInput = document.getElementById('avatarFile');
        const avatarPreviewImg = document.getElementById('avatarPreview');
        avatarFileInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    avatarPreviewImg.src = e.target.result;
                }
                reader.readAsDataURL(file);
            }
        });

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
            avatarPreviewImg.src = user.userDetail.avatar || '/img/default-avatar.jpg';
        });

        if (saveProfileChangesBtn) {
            saveProfileChangesBtn.addEventListener('click', async () => {
                // 1. Upload ảnh trước nếu có ảnh mới được chọn
                if (avatarFileInput.files.length > 0) {
                    const avatarFormData = new FormData();
                    avatarFormData.append('avatarFile', avatarFileInput.files[0]);
                    try {
                        const avatarResponse = await fetch('/reader/api/profile/avatar/upload', {
                            method: 'POST',
                            body: avatarFormData
                            // KHÔNG set Content-Type, trình duyệt sẽ tự làm
                        });
                        if (!avatarResponse.ok) {
                            alert('Lỗi upload ảnh đại diện!');
                            saveProfileChangesBtn.disabled = false;
                            saveProfileChangesBtn.textContent = 'Lưu thay đổi';
                            return; // Dừng lại nếu upload ảnh thất bại
                        }
                        const avatarResult = await avatarResponse.json();
                        // Cập nhật ảnh trên header ngay lập tức
                        const headerAvatar = document.querySelector('.user-avatar-dropdown'); // Giả sử có class này
                        if(headerAvatar) headerAvatar.src = avatarResult.avatarUrl;
                    } catch(e) { /* Lỗi upload ảnh */ }
                }
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
                finally {
                    editProfileModal.hide();
                    window.location.reload();
                }
            });
        }
    }
});