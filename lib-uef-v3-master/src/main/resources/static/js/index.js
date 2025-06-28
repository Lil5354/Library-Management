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

    // === LOGIC CHO ĐÁNH GIÁ SÁCH (PHIÊN BẢN HOÀN CHỈNH) ===
    const reviewModalEl = document.getElementById('reviewModal');

    if (reviewModalEl) {
        const reviewModal = new bootstrap.Modal(reviewModalEl);
        const historyList = document.getElementById('borrowHistoryList');
        const reviewBookTitle = document.getElementById('reviewBookTitle');
        const reviewBookIdInput = document.getElementById('reviewBookId');
        const starRatingContainer = reviewModalEl.querySelector('.star-rating');
        const stars = starRatingContainer.querySelectorAll('i'); // Lấy tất cả các icon sao
        const reviewComment = document.getElementById('reviewComment');
        const submitReviewBtn = document.getElementById('submitReviewBtn');
        const reviewError = document.getElementById('reviewError');

        let currentRating = 0;

        // Hàm cập nhật trạng thái hiển thị của các sao
        function updateStarDisplay(rating) {
            stars.forEach(star => {
                // Thêm/xóa class 'selected' để tô màu vàng vĩnh viễn
                star.classList.toggle('selected', parseInt(star.dataset.value) <= rating);
            });
        }

        // Gán sự kiện cho các ngôi sao
        stars.forEach(star => {
            // Khi di chuột vào một ngôi sao
            star.addEventListener('mouseover', () => {
                stars.forEach(s => {
                    // Thêm/xóa class 'hover' để tô màu vàng tạm thời
                    s.classList.toggle('hover', parseInt(s.dataset.value) <= parseInt(star.dataset.value));
                });
            });

            // Khi di chuột ra khỏi khu vực sao
            starRatingContainer.addEventListener('mouseout', () => {
                stars.forEach(s => s.classList.remove('hover'));
            });

            // Khi nhấn vào một ngôi sao
            star.addEventListener('click', () => {
                currentRating = parseInt(star.dataset.value);
                updateStarDisplay(currentRating); // Cập nhật trạng thái "đã chọn"
            });
        });

        // Mở popup khi nhấn nút "Đánh giá"
        if (historyList) {
            historyList.addEventListener('click', (e) => {
                const reviewButton = e.target.closest('.btn-write-review');
                if (reviewButton) {
                    e.preventDefault();
                    reviewBookTitle.textContent = reviewButton.dataset.bookTitle;
                    reviewBookIdInput.value = reviewButton.dataset.bookId;

                    // Reset form về trạng thái ban đầu
                    currentRating = 0;
                    updateStarDisplay(0); // Tắt hết sao
                    reviewComment.value = '';
                    reviewError.textContent = '';

                    reviewModal.show();
                }
            });
        }

        // Sự kiện nút "Gửi đánh giá"
        submitReviewBtn.addEventListener('click', async () => {
            if (currentRating === 0) {
                reviewError.textContent = "Vui lòng chọn số sao để đánh giá.";
                return;
            }
            reviewError.textContent = '';
            submitReviewBtn.disabled = true;
            submitReviewBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang gửi...';

            const reviewData = {
                bookId: reviewBookIdInput.value,
                rating: currentRating,
                comment: reviewComment.value
            };

            try {
                const response = await fetch('/api/reviews', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(reviewData)
                });
                const result = await response.json();

                if(response.ok) {
                    alert(result.message);
                    reviewModal.hide();

                    // === LOGIC CẬP NHẬT GIAO DIỆN TỨC THÌ ===
                    const reviewedBookId = reviewBookIdInput.value;
                    const buttonToUpdate = historyList.querySelector(`.btn-write-review[data-book-id="${reviewedBookId}"]`);
                    if (buttonToUpdate) {
                        const newElement = document.createElement('span');
                        newElement.className = 'text-success small';
                        newElement.innerHTML = '<i class="fas fa-check-circle me-1"></i>Đã đánh giá';
                        buttonToUpdate.parentElement.replaceWith(newElement);
                    }
                    // ===========================================

                } else {
                    reviewError.textContent = result.message || 'Có lỗi xảy ra.';
                }
            } catch (error) {
                reviewError.textContent = 'Lỗi kết nối. Vui lòng thử lại.';
            } finally {
                submitReviewBtn.disabled = false;
                submitReviewBtn.innerHTML = 'Gửi đánh giá';
            }
        });
    }

    // === LOGIC CHO ĐỔI MẬT KHẨU ===
    const changePasswordBtn = document.getElementById('changePasswordBtn');
    const changePasswordModalEl = document.getElementById('changePasswordModal');

    if (changePasswordBtn && changePasswordModalEl) {
        const changePasswordModal = new bootstrap.Modal(changePasswordModalEl);

        // Lấy tất cả các panel và các nút
        const requestPanel = document.getElementById('requestPanel');
        const validatePanel = document.getElementById('validatePanel');
        const resetPanel = document.getElementById('resetPanel');
        const sendOtpBtn = document.getElementById('sendOtpBtn');
        const validateOtpBtn = document.getElementById('validateOtpBtn');
        const resetPasswordBtn = document.getElementById('resetPasswordBtn');
        const countdownTimerEl = document.getElementById('countdownTimer');
        const resendOtpBtn = document.getElementById('resendOtpBtn');

        let countdownTimer;
        let validToken = '';

        // Hàm bắt đầu đếm ngược
        function startCountdown() {
            // Ẩn nút "Gửi lại" và hiện đồng hồ đếm ngược
            resendOtpBtn.classList.add('d-none');
            countdownTimerEl.classList.remove('d-none');

            let timeLeft = 60;
            countdownTimerEl.textContent = `Bạn có thể gửi lại mã sau ${timeLeft}s`;

            clearInterval(countdownTimer); // Dừng bộ đếm cũ nếu có

            countdownTimer = setInterval(() => {
                timeLeft--;
                countdownTimerEl.textContent = `Bạn có thể gửi lại mã sau ${timeLeft}s`;
                if (timeLeft <= 0) {
                    clearInterval(countdownTimer);
                    countdownTimerEl.classList.add('d-none'); // Ẩn đồng hồ
                    resendOtpBtn.classList.remove('d-none'); // Hiện nút "Gửi lại"
                }
            }, 1000);
        }

        // Hàm xử lý việc gửi mã (dùng cho cả nút đầu và nút gửi lại)
        async function handleSendOtp() {
            sendOtpBtn.disabled = true;
            sendOtpBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang gửi...';
            resendOtpBtn.classList.add('d-none'); // Ẩn nút gửi lại trong lúc xử lý

            try {
                const response = await fetch('/api/password/request', { method: 'POST' });
                const result = await response.json();

                if (response.ok) {
                    alert(result.message);
                    requestPanel.classList.add('d-none');
                    validatePanel.classList.remove('d-none');
                    startCountdown(); // Bắt đầu đếm ngược
                } else {
                    alert('Lỗi: ' + (result.error === 'NO_EMAIL' ? 'Tài khoản chưa có email. Vui lòng cập nhật thông tin.' : 'Lỗi không xác định.'));
                    changePasswordModal.hide();
                }
            } catch (error) {
                alert('Lỗi kết nối. Vui lòng thử lại.');
            } finally {
                // Dù thành công hay thất bại, trả lại trạng thái cho nút gửi ban đầu
                sendOtpBtn.disabled = false;
                sendOtpBtn.innerHTML = 'Gửi mã đến Email';
            }
        }

        // Sự kiện khi mở popup: Reset về trạng thái ban đầu
        changePasswordBtn.addEventListener('click', e => {
            e.preventDefault();
            clearInterval(countdownTimer);
            requestPanel.classList.remove('d-none');
            validatePanel.classList.add('d-none');
            resetPanel.classList.add('d-none');
            changePasswordModal.show();
        });

        // Gán sự kiện cho các nút
        sendOtpBtn.addEventListener('click', handleSendOtp);
        resendOtpBtn.addEventListener('click', (e) => {
            e.preventDefault();
            handleSendOtp(); // Nút gửi lại cũng gọi hàm này
        });

        validateOtpBtn.addEventListener('click', async () => {
            const token = otpInput.value;
            otpError.textContent = '';

            if (!token || token.length !== 6) {
                otpError.textContent = 'Vui lòng nhập đủ 6 chữ số.';
                return;
            }

            validateOtpBtn.disabled = true;
            validateOtpBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

            try {
                const response = await fetch('/api/password/validate-token', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ token: token })
                });
                const result = await response.json();
                if (response.ok) {
                    validToken = token;
                    validatePanel.classList.add('d-none');
                    resetPanel.classList.remove('d-none');
                } else {
                    otpError.textContent = 'Mã không hợp lệ hoặc đã hết hạn.';
                }
            } catch (e) {
                otpError.textContent = 'Lỗi kết nối. Vui lòng thử lại.';
            } finally {
                validateOtpBtn.disabled = false;
                validateOtpBtn.textContent = 'Xác nhận';
            }
        });
        resetPasswordBtn.addEventListener('click', async () => {
            const newPassword = document.getElementById('newPasswordInput').value;
            const confirmPassword = document.getElementById('confirmPasswordInput').value;
            if (newPassword !== confirmPassword) {
                document.getElementById('resetError').textContent = 'Mật khẩu xác nhận không khớp.';
                return;
            }
            const response = await fetch('/api/password/reset', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ token: validToken, newPassword: newPassword })
            });
            const result = await response.json();
            if (response.ok) {
                alert(result.message);
                window.location.href = '/auth/login';
            } else {
                document.getElementById('resetError').textContent = 'Có lỗi xảy ra, vui lòng bắt đầu lại.';
            }
        });
    }

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
    const cartKey = `borrowCart_${loggedInUsername}`;

// --- SỬA LỖI 1: LƯU VÀ TẢI GIỎ HÀNG TỪ SESSIONSTORAGE ---
    function saveCartToSession() {
        if(loggedInUsername) { // Chỉ lưu nếu đã đăng nhập
            sessionStorage.setItem(cartKey, JSON.stringify(borrowCart));
        }
    }

    function loadCartFromSession() {
        if(loggedInUsername) { // Chỉ tải nếu đã đăng nhập
            const savedCart = sessionStorage.getItem(cartKey);
            if (savedCart) {
                borrowCart = JSON.parse(savedCart);
            }
        } else {
            borrowCart = []; // Nếu không đăng nhập, giỏ hàng luôn rỗng
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
                const result = await response.json();

                if (response.ok) {
                    // Xử lý khi thành công
                    alert(result.message);
                    borrowCart = [];
                    saveCartToSession();
                    updateCartUI();
                    cartContainer.classList.remove('active');
                    window.location.reload();
                } else {
                    // Xử lý khi có lỗi nghiệp vụ từ backend (status 400, 500...)
                    // 'result.message' chính là thông báo lỗi cụ thể mà backend gửi về
                    // Ví dụ: "Bạn đã mượn cuốn sách 'Nhà Giả Kim' và chưa trả."
                    throw new Error(result.message || 'Một lỗi không xác định đã xảy ra.');
                }

            } catch(e) {
                // Bắt tất cả các lỗi (lỗi mạng hoặc lỗi từ 'throw new Error' ở trên)
                // và hiển thị cho người dùng một cách chi tiết
                console.error("Lỗi chi tiết khi mượn sách:", e);
                alert("Mượn sách thất bại!\n\nLý do: " + e.message);
            } finally {
                // Khối này đảm bảo nút bấm luôn được reset trạng thái
                borrowAllBtn.disabled = false;
                borrowAllBtn.textContent = 'Hoàn tất mượn sách';
            }
        });
    }
    // === GÁN SỰ KIỆN XÓA GIỎ HÀNG KHI ĐĂNG XUẤT ===
    const logoutForm = document.getElementById('logoutForm'); // Giả sử bạn thêm id="logoutForm" vào thẻ form

    if (logoutForm) {
        logoutForm.addEventListener('submit', function() {
            // Xóa giỏ hàng của user hiện tại khỏi sessionStorage TRƯỚC KHI gửi yêu cầu đăng xuất
            if(loggedInUsername) {
                sessionStorage.removeItem(cartKey);
                console.log(`Cleared cart for user: ${loggedInUsername}`);
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
                const response = await fetch(`/api/readers/borrowing-history?page=${page}&size=5`);
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

                        // === BẮT ĐẦU LOGIC MỚI ĐỂ HIỂN THỊ CÁC NÚT HÀNH ĐỘNG ===

                        // 1. Luôn tạo nút "Mượn lại"
                        const reborrowButtonHtml = `
                        <button class="btn btn-sm btn-outline-primary re-borrow-btn" 
                                data-book-id="${item.bookId}" 
                                data-book-title="${item.bookTitle}" 
                                data-book-cover="${item.bookCoverImage}"
                                title="Thêm sách này vào giỏ để mượn lại">
                            <i class="fas fa-cart-plus"></i> Mượn lại
                        </button>`;

                        // 2. Tạo nút "Đánh giá" hoặc chữ "Đã đánh giá" tùy điều kiện
                        let reviewButtonHtml = '';
                        if (item.status === 'RETURNED') {
                            if (item.hasReviewed) {
                                // Nếu đã trả và đã đánh giá -> Hiển thị text
                                reviewButtonHtml = `<span class="text-success small ms-2"><i class="fas fa-check-circle me-1"></i>Đã đánh giá</span>`;
                            } else {
                                // Nếu đã trả nhưng CHƯA đánh giá -> Hiển thị nút
                                reviewButtonHtml = `
                                <button class="btn btn-sm btn-outline-warning btn-write-review ms-2" 
                                        data-book-id="${item.bookId}" 
                                        data-book-title="${item.bookTitle}">
                                    <i class="fas fa-star"></i> Đánh giá
                                </button>`;
                            }
                        }

                        // =========================================================

                        const itemHTML = `
                        <div class="borrowed-item">
                            <img src="${item.bookCoverImage || 'https://source.unsplash.com/500x650/?book'}" class="borrowed-item-cover">
                            <div class="borrowed-item-info">
                                <h6>${item.bookTitle}</h6>
                                <p>Ngày mượn: ${borrowDate} | Hạn trả: ${dueDate} | Ngày trả: ${returnDate}</p>
                                <div>Trạng thái: ${statusBadge}</div>
                                ${item.lateFee > 0 ? `<div class="fee-text">Phí phạt: ${item.lateFee.toLocaleString('vi-VN')}đ</div>` : ''}
                            </div>
                            <div class="borrow-item-action d-flex flex-column gap-2">
                                ${reborrowButtonHtml} ${reviewButtonHtml}  </div>
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
                const response = await fetch('/api/readers/borrowed-books');
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

                    // === CẬP NHẬT LOGIC HIỂN THỊ NÚT ===
                    const modalFooter = document.getElementById('bookDetailModalFooter');
                    modalFooter.innerHTML = ''; // Xóa các nút cũ

                    // 1. Tạo nút Đọc thử dựa trên sự tồn tại của samplePdfUrl
                    let readSampleBtnHtml = '';
                    if (book.samplePdfUrl) {
                        // Nếu có link -> tạo thẻ <a> mở tab mới
                        readSampleBtnHtml = `
                            <a href="${book.samplePdfUrl}" target="_blank" class="btn btn-read-sample">
                                <i class="fas fa-eye me-2"></i>Đọc thử
                            </a>`;
                    } else {
                        // Nếu không có link -> tạo nút bị vô hiệu hóa
                        readSampleBtnHtml = `
                            <button type="button" class="btn btn-read-sample-disabled" disabled title="Sách này chưa có bản đọc thử">
                                <i class="fas fa-eye-slash me-2"></i>Đọc thử
                            </button>`;
                    }

                    // 3. Nút Đóng
                    const closeBtnHtml = '<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>';

                    // Gắn các nút vào footer
                    modalFooter.innerHTML = readSampleBtnHtml + closeBtnHtml;
                    // === BẮT ĐẦU CODE MỚI: TẢI VÀ HIỂN THỊ ĐÁNH GIÁ ===
                    const reviewsListEl = document.getElementById('bookReviewsList');
                    reviewsListEl.innerHTML = '<div class="text-center"><div class="spinner-border spinner-border-sm"></div></div>';

                    const reviewsResponse = await fetch(`/api/reviews/book/${bookId}`);
                    const reviews = await reviewsResponse.json();

                    reviewsListEl.innerHTML = ''; // Xóa spinner
                    if (reviews.length === 0) {
                        reviewsListEl.innerHTML = '<p class="text-muted small">Chưa có đánh giá nào cho cuốn sách này.</p>';
                    } else {
                        reviews.forEach(review => {
                            let starsHTML = '';
                            for(let i=0; i<5; i++){
                                starsHTML += `<i class="fas fa-star ${i < review.rating ? 'text-warning' : 'text-light'}"></i>`;
                            }
                            const reviewHTML = `
                    <div class="review-item mb-3">
                        <strong>${review.reviewerName}</strong>
                        <div class="d-flex justify-content-between">
                            <div class="review-stars">${starsHTML}</div>
                            <small class="text-muted">${review.reviewDate}</small>
                        </div>
                        <p class="mb-0 fst-italic">"${review.comment}"</p>
                    </div>`;
                            reviewsListEl.insertAdjacentHTML('beforeend', reviewHTML);
                        });
                    }
                    // === KẾT THÚC CODE MỚI ===
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
                const response = await fetch('/api/readers/profile');
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
                        const avatarResponse = await fetch('/api/readers/profile/avatar/upload', {
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
                    const response = await fetch('/api/readers/update', {
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
                        window.location.reload();
                    } else { alert('Lỗi: ' + (result.message || 'Không thể cập nhật.')); }
                } catch (error) { alert('Lỗi kết nối, vui lòng thử lại.'); }
            });
        }
    }
    // === LOGIC CHO POPUP CẬP NHẬT LẦN ĐẦU ===
    // Biến 'showFirstLoginPopup' được lấy từ thẻ script trong layout.html

    const firstLoginModalEl = document.getElementById('firstLoginModal');

    // Chỉ thực hiện logic nếu popup tồn tại trên trang
    if (firstLoginModalEl) {
        // 1. Logic hiển thị popup khi đăng nhập lần đầu
        if (typeof showFirstLoginPopup !== 'undefined' && showFirstLoginPopup) {
            const firstLoginModal = new bootstrap.Modal(firstLoginModalEl);
            firstLoginModal.show();
        }

        // 2. Logic xử lý nút lưu trong popup
        const saveBtn = document.getElementById('saveFirstUpdateBtn');
        if (saveBtn) {
            saveBtn.addEventListener('click', async function() {
                // Lấy dữ liệu từ các ô input trong form
                const formData = {
                    fullName: document.getElementById('ff_fullName').value,
                    phone: document.getElementById('ff_phone').value,
                    email: document.getElementById('ff_email').value,
                    address: document.getElementById('ff_address').value,
                    dob: document.getElementById('ff_dob').value,
                    gender: document.getElementById('ff_gender').value,
                };

                // Ràng buộc dữ liệu (Validation)
                let errors = [];
                if (!formData.fullName.trim()) errors.push("Họ và Tên không được để trống.");
                if (/\d/.test(formData.fullName)) errors.push("Họ và Tên không được chứa số.");
                if (!/^\d{10}$/.test(formData.phone.trim())) errors.push("Số điện thoại phải gồm đúng 10 chữ số.");
                if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) errors.push("Định dạng email không hợp lệ.");

                if (errors.length > 0) {
                    alert("Vui lòng sửa các lỗi sau:\n\n- " + errors.join("\n- "));
                    return; // Dừng lại nếu có lỗi
                }

                saveBtn.disabled = true;
                saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang lưu...';

                try {
                    const response = await fetch('/api/readers/update', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(formData)
                    });
                    const result = await response.json();

                    if (response.ok) {
                        const firstLoginModal = bootstrap.Modal.getInstance(firstLoginModalEl);
                        if (firstLoginModal) firstLoginModal.hide();

                        alert(result.message);
                        window.location.reload(); // Tải lại trang để cập nhật toàn bộ giao diện
                    } else {
                        alert('Lỗi: ' + (result.message || 'Không thể cập nhật.'));
                    }
                } catch (error) {
                    alert('Lỗi kết nối, vui lòng thử lại.');
                } finally {
                    saveBtn.disabled = false;
                    saveBtn.textContent = 'Lưu và Bắt đầu';
                }
            });
        }
    }
});