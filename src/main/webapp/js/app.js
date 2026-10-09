/**
 * Library Management System Frontend Logic
 * Full AJAX / Fetch API integration with Servlets
 */

const API = {
  auth: {
    login: (params) => postForm('api/auth/login', params),
    register: (params) => postForm('api/auth/register', params),
    logout: () => fetch('api/auth/logout', { method: 'POST' }).then(r => r.json()),
    current: () => fetch('api/auth/current').then(r => r.json())
  },
  dashboard: {
    getStats: () => fetch('api/dashboard/stats').then(r => r.json())
  },
  books: {
    list: (search = '', category = '') => {
      const q = new URLSearchParams({ action: 'list', search, category });
      return fetch(`api/books?${q.toString()}`).then(r => r.json());
    },
    categories: () => fetch('api/books?action=categories').then(r => r.json()),
    get: (id) => fetch(`api/books?action=get&id=${id}`).then(r => r.json()),
    add: (params) => postForm('api/books', { action: 'add', ...params }),
    update: (params) => postForm('api/books', { action: 'update', ...params }),
    delete: (id) => postForm('api/books', { action: 'delete', id })
  },
  transactions: {
    list: (filter = '') => {
      const q = new URLSearchParams({ filter });
      return fetch(`api/transactions?${q.toString()}`).then(r => r.json());
    },
    issue: (params) => postForm('api/transactions', { action: 'issue', ...params }),
    borrow: (bookId, days = 14) => postForm('api/transactions', { action: 'borrow', bookId, days }),
    returnBook: (params) => postForm('api/transactions', { action: 'return', ...params })
  },
  users: {
    list: (filter = '') => fetch(`api/users?filter=${filter}`).then(r => r.json()),
    delete: (id) => postForm('api/users', { action: 'delete', id })
  }
};

// Helper to post standard x-www-form-urlencoded data to Servlets
function postForm(url, data) {
  const body = new URLSearchParams();
  for (const [key, value] of Object.entries(data)) {
    if (value !== undefined && value !== null) {
      body.append(key, value);
    }
  }
  return fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
    body: body.toString()
  }).then(async res => {
    const json = await res.json();
    if (!res.ok && !json.message) {
      throw new Error(`HTTP Error ${res.status}`);
    }
    return json;
  });
}

// Toast Notifications
function showToast(message, type = 'info') {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  if (type === 'error') icon = '⚠️';

  toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function escapeHtml(text) {
  if (!text) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// Tab Switching
function switchTab(tabId, btnElement) {
  document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));
  document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
  
  const pane = document.getElementById(tabId);
  if (pane) pane.classList.add('active');
  if (btnElement) btnElement.classList.add('active');
}

// Modal Helpers
function openModal(modalId) {
  const m = document.getElementById(modalId);
  if (m) m.classList.add('active');
}

function closeModal(modalId) {
  const m = document.getElementById(modalId);
  if (m) m.classList.remove('active');
}

// Logout handler
async function handleLogout() {
  try {
    await API.auth.logout();
    window.location.href = 'login.html';
  } catch (err) {
    window.location.href = 'login.html';
  }
}

// ==========================================
// ADMIN DASHBOARD MODULE
// ==========================================
const AdminApp = {
  currentEditBookId: null,

  async init() {
    await this.checkAuth();
    await this.loadStats();
    await this.loadCategories();
    await this.loadBooks();
    await this.loadActiveRentals();
    await this.loadAllTransactions();
    await this.loadMembers();
  },

  async checkAuth() {
    try {
      const auth = await API.auth.current();
      if (!auth.loggedIn || auth.user.role !== 'ADMIN') {
        window.location.href = 'login.html';
        return;
      }
      const nameEl = document.getElementById('adminUserName');
      if (nameEl) nameEl.textContent = auth.user.fullName || auth.user.username;
    } catch (e) {
      window.location.href = 'login.html';
    }
  },

  async loadStats() {
    try {
      const stats = await API.dashboard.getStats();
      if (document.getElementById('statTotalTitles')) document.getElementById('statTotalTitles').textContent = stats.totalTitles || 0;
      if (document.getElementById('statTotalCopies')) document.getElementById('statTotalCopies').textContent = stats.totalCopies || 0;
      if (document.getElementById('statAvailableCopies')) document.getElementById('statAvailableCopies').textContent = stats.availableCopies || 0;
      if (document.getElementById('statActiveLoans')) document.getElementById('statActiveLoans').textContent = stats.activeLoans || 0;
      if (document.getElementById('statOverdueCount')) document.getElementById('statOverdueCount').textContent = stats.overdueCount || 0;
      if (document.getElementById('statTotalMembers')) document.getElementById('statTotalMembers').textContent = stats.totalMembers || 0;
      if (document.getElementById('statTotalFines')) document.getElementById('statTotalFines').textContent = '$' + (stats.totalFines || 0).toFixed(2);
    } catch (err) {
      console.error('Failed to load stats', err);
    }
  },

  async loadCategories() {
    try {
      const categories = await API.books.categories();
      const catSelect = document.getElementById('categoryFilter');
      if (catSelect) {
        catSelect.innerHTML = '<option value="">All Categories</option>' +
          categories.map(c => `<option value="${escapeHtml(c)}">${escapeHtml(c)}</option>`).join('');
      }
    } catch (err) {
      console.error(err);
    }
  },

  async loadBooks() {
    const search = document.getElementById('bookSearchInput')?.value || '';
    const category = document.getElementById('categoryFilter')?.value || '';
    const tbody = document.getElementById('booksTableBody');
    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Loading books...</td></tr>';

    try {
      const books = await API.books.list(search, category);
      if (books.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state"><div class="empty-icon">📚</div><div class="empty-text">No books found</div></td></tr>';
        return;
      }

      tbody.innerHTML = books.map(b => `
        <tr>
          <td><strong>#${b.id}</strong></td>
          <td>
            <div style="font-weight: 600; color: var(--dark);">${escapeHtml(b.title)}</div>
            <small style="color: var(--gray-600);">${escapeHtml(b.publisher || '')} ${b.publicationYear ? `(${b.publicationYear})` : ''}</small>
          </td>
          <td>${escapeHtml(b.author)}</td>
          <td><span style="font-size: 0.8rem; background: var(--gray-100); padding: 2px 6px; border-radius: 4px;">${escapeHtml(b.isbn)}</span></td>
          <td><span class="badge" style="background: var(--primary-light); color: var(--primary-text);">${escapeHtml(b.category)}</span></td>
          <td>
            <strong>${b.availableCopies}</strong> / ${b.quantity}
            ${b.availableCopies > 0 ? '<span class="badge badge-in-stock" style="margin-left: 6px;">Available</span>' : '<span class="badge badge-out-of-stock" style="margin-left: 6px;">Out</span>'}
          </td>
          <td>
            <div style="display: flex; gap: 0.35rem;">
              <button class="btn btn-secondary btn-sm" onclick="AdminApp.editBook(${b.id})">Edit</button>
              <button class="btn btn-danger btn-sm" onclick="AdminApp.deleteBook(${b.id}, '${escapeHtml(b.title).replace(/'/g, "\\'")}')">Delete</button>
            </div>
          </td>
        </tr>
      `).join('');
    } catch (err) {
      tbody.innerHTML = '<tr><td colspan="7" style="color: red; text-align: center;">Failed to load books</td></tr>';
    }
  },

  openAddBook() {
    document.getElementById('addBookForm').reset();
    openModal('addBookModal');
  },

  async submitAddBook(e) {
    e.preventDefault();
    const form = document.getElementById('addBookForm');
    const data = {
      title: form.title.value.trim(),
      author: form.author.value.trim(),
      isbn: form.isbn.value.trim(),
      category: form.category.value.trim(),
      publisher: form.publisher.value.trim(),
      publicationYear: form.publicationYear.value.trim(),
      quantity: form.quantity.value.trim()
    };

    try {
      const res = await API.books.add(data);
      if (res.success) {
        showToast(res.message, 'success');
        closeModal('addBookModal');
        await this.loadBooks();
        await this.loadCategories();
        await this.loadStats();
      } else {
        showToast(res.message, 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async editBook(id) {
    try {
      const book = await API.books.get(id);
      if (!book) return;

      this.currentEditBookId = id;
      const form = document.getElementById('editBookForm');
      form.id.value = book.id;
      form.title.value = book.title;
      form.author.value = book.author;
      form.isbn.value = book.isbn;
      form.category.value = book.category;
      form.publisher.value = book.publisher || '';
      form.publicationYear.value = book.publicationYear || '';
      form.quantity.value = book.quantity;

      openModal('editBookModal');
    } catch (err) {
      showToast('Failed to fetch book details', 'error');
    }
  },

  async submitEditBook(e) {
    e.preventDefault();
    const form = document.getElementById('editBookForm');
    const data = {
      id: form.id.value,
      title: form.title.value.trim(),
      author: form.author.value.trim(),
      isbn: form.isbn.value.trim(),
      category: form.category.value.trim(),
      publisher: form.publisher.value.trim(),
      publicationYear: form.publicationYear.value.trim(),
      quantity: form.quantity.value.trim()
    };

    try {
      const res = await API.books.update(data);
      if (res.success) {
        showToast(res.message, 'success');
        closeModal('editBookModal');
        await this.loadBooks();
        await this.loadStats();
      } else {
        showToast(res.message, 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async deleteBook(id, title) {
    if (!confirm(`Are you sure you want to delete "${title}"?`)) return;
    try {
      const res = await API.books.delete(id);
      if (res.success) {
        showToast(res.message, 'success');
        await this.loadBooks();
        await this.loadStats();
      } else {
        showToast(res.message, 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async openIssueModal() {
    const studentSelect = document.getElementById('issueStudentSelect');
    const bookSelect = document.getElementById('issueBookSelect');

    studentSelect.innerHTML = '<option>Loading students...</option>';
    bookSelect.innerHTML = '<option>Loading books...</option>';

    openModal('issueBookModal');

    try {
      const [students, books] = await Promise.all([
        API.users.list('students'),
        API.books.list()
      ]);

      studentSelect.innerHTML = students.map(s => `
        <option value="${s.id}">${escapeHtml(s.fullName)} (@${escapeHtml(s.username)})</option>
      `).join('');

      const availableBooks = books.filter(b => b.availableCopies > 0);
      if (availableBooks.length === 0) {
        bookSelect.innerHTML = '<option disabled>No books currently available in stock</option>';
      } else {
        bookSelect.innerHTML = availableBooks.map(b => `
          <option value="${b.id}">${escapeHtml(b.title)} (${b.availableCopies} available)</option>
        `).join('');
      }
    } catch (err) {
      showToast('Error loading students or books', 'error');
    }
  },

  async submitIssueBook(e) {
    e.preventDefault();
    const form = document.getElementById('issueBookForm');
    const data = {
      userId: form.userId.value,
      bookId: form.bookId.value,
      days: form.days.value,
      remarks: form.remarks.value
    };

    try {
      const res = await API.transactions.issue(data);
      if (res.success) {
        showToast(res.message, 'success');
        closeModal('issueBookModal');
        await this.loadActiveRentals();
        await this.loadAllTransactions();
        await this.loadBooks();
        await this.loadStats();
      } else {
        showToast(res.message, 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async loadActiveRentals() {
    const tbody = document.getElementById('activeRentalsTableBody');
    if (!tbody) return;

    try {
      const list = await API.transactions.list('active');
      if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state"><div class="empty-icon">✅</div><div class="empty-text">No active checkouts! All books are in stock.</div></td></tr>';
        return;
      }

      tbody.innerHTML = list.map(t => {
        const isOverdue = t.isOverdue || t.status === 'OVERDUE';
        const badgeClass = isOverdue ? 'badge badge-overdue' : 'badge badge-issued';
        return `
          <tr>
            <td><strong>#${t.id}</strong></td>
            <td><strong>${escapeHtml(t.bookTitle)}</strong><br><small style="color:var(--gray-600)">ISBN: ${escapeHtml(t.bookIsbn)}</small></td>
            <td>${escapeHtml(t.userName)}<br><small style="color:var(--gray-600)">${escapeHtml(t.userEmail)}</small></td>
            <td>${t.issueDate}</td>
            <td><strong>${t.dueDate}</strong></td>
            <td>
              <span class="${badgeClass}">${escapeHtml(t.status)}</span>
              ${t.fineAmount > 0 ? `<div style="color: var(--danger); font-weight: 700; margin-top: 3px;">Fine: $${t.fineAmount.toFixed(2)}</div>` : ''}
            </td>
            <td>
              <button class="btn btn-success btn-sm" onclick="AdminApp.openReturnModal(${t.id}, '${escapeHtml(t.bookTitle).replace(/'/g, "\\'")}', '${escapeHtml(t.userName).replace(/'/g, "\\'")}', ${t.fineAmount})">Return Book</button>
            </td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      tbody.innerHTML = '<tr><td colspan="7" style="color: red; text-align: center;">Failed to load active rentals</td></tr>';
    }
  },

  openReturnModal(transId, bookTitle, userName, fineAmount) {
    const form = document.getElementById('returnBookForm');
    form.transactionId.value = transId;
    document.getElementById('returnBookTitle').textContent = bookTitle;
    document.getElementById('returnUserName').textContent = userName;
    document.getElementById('returnFineAmount').value = (fineAmount || 0).toFixed(2);
    openModal('returnBookModal');
  },

  async submitReturnBook(e) {
    e.preventDefault();
    const form = document.getElementById('returnBookForm');
    const data = {
      transactionId: form.transactionId.value,
      finePaid: form.finePaid.value,
      remarks: form.remarks.value
    };

    try {
      const res = await API.transactions.returnBook(data);
      if (res.success) {
        showToast(res.message, 'success');
        closeModal('returnBookModal');
        await this.loadActiveRentals();
        await this.loadAllTransactions();
        await this.loadBooks();
        await this.loadStats();
      } else {
        showToast(res.message, 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async loadAllTransactions() {
    const tbody = document.getElementById('allTransactionsTableBody');
    if (!tbody) return;

    try {
      const list = await API.transactions.list();
      if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No transaction records found.</td></tr>';
        return;
      }

      tbody.innerHTML = list.map(t => {
        let badge = 'badge badge-issued';
        if (t.status === 'RETURNED') badge = 'badge badge-returned';
        if (t.status === 'OVERDUE') badge = 'badge badge-overdue';

        return `
          <tr>
            <td>#${t.id}</td>
            <td><strong>${escapeHtml(t.bookTitle)}</strong></td>
            <td>${escapeHtml(t.userName)}</td>
            <td>${t.issueDate}</td>
            <td>${t.dueDate}</td>
            <td>${t.returnDate ? t.returnDate : '<em style="color:var(--gray-400)">Not returned</em>'}</td>
            <td><span class="${badge}">${escapeHtml(t.status)}</span></td>
            <td><strong>$${(t.fineAmount || 0).toFixed(2)}</strong></td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      tbody.innerHTML = '<tr><td colspan="8" style="color: red; text-align: center;">Failed to load transactions</td></tr>';
    }
  },

  async loadMembers() {
    const tbody = document.getElementById('membersTableBody');
    if (!tbody) return;

    try {
      const users = await API.users.list();
      tbody.innerHTML = users.map(u => `
        <tr>
          <td>#${u.id}</td>
          <td><strong>${escapeHtml(u.fullName)}</strong></td>
          <td>${escapeHtml(u.username)}</td>
          <td>${escapeHtml(u.email)}</td>
          <td>${escapeHtml(u.phone || 'N/A')}</td>
          <td><span class="user-badge ${u.role.toLowerCase()}">${escapeHtml(u.role)}</span></td>
          <td>
            ${u.role !== 'ADMIN' ? `<button class="btn btn-danger btn-sm" onclick="AdminApp.deleteMember(${u.id}, '${escapeHtml(u.fullName).replace(/'/g, "\\'")}')">Remove</button>` : '<em style="color:var(--gray-400)">Admin</em>'}
          </td>
        </tr>
      `).join('');
    } catch (err) {
      tbody.innerHTML = '<tr><td colspan="7" style="color: red; text-align: center;">Failed to load members</td></tr>';
    }
  },

  async deleteMember(id, name) {
    if (!confirm(`Are you sure you want to remove member "${name}"?`)) return;
    try {
      const res = await API.users.delete(id);
      if (res.success) {
        showToast(res.message, 'success');
        await this.loadMembers();
        await this.loadStats();
      } else {
        showToast(res.message, 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  }
};

// ==========================================
// STUDENT DASHBOARD MODULE
// ==========================================
const StudentApp = {
  async init() {
    await this.checkAuth();
    await this.loadStats();
    await this.loadCategories();
    await this.loadCatalog();
    await this.loadMyBorrowed();
    await this.loadMyHistory();
  },

  async checkAuth() {
    try {
      const auth = await API.auth.current();
      if (!auth.loggedIn) {
        window.location.href = 'login.html';
        return;
      }
      const nameEl = document.getElementById('studentUserName');
      if (nameEl) nameEl.textContent = auth.user.fullName || auth.user.username;
    } catch (e) {
      window.location.href = 'login.html';
    }
  },

  async loadStats() {
    try {
      const stats = await API.dashboard.getStats();
      if (document.getElementById('statActiveBorrowed')) document.getElementById('statActiveBorrowed').textContent = stats.activeBorrowed || 0;
      if (document.getElementById('statOverdueCount')) document.getElementById('statOverdueCount').textContent = stats.overdueCount || 0;
      if (document.getElementById('statTotalHistory')) document.getElementById('statTotalHistory').textContent = stats.totalHistory || 0;
      if (document.getElementById('statTotalFines')) document.getElementById('statTotalFines').textContent = '$' + (stats.totalFines || 0).toFixed(2);
    } catch (err) {
      console.error(err);
    }
  },

  async loadCategories() {
    try {
      const categories = await API.books.categories();
      const catSelect = document.getElementById('studentCategoryFilter');
      if (catSelect) {
        catSelect.innerHTML = '<option value="">All Categories</option>' +
          categories.map(c => `<option value="${escapeHtml(c)}">${escapeHtml(c)}</option>`).join('');
      }
    } catch (err) {
      console.error(err);
    }
  },

  async loadCatalog() {
    const search = document.getElementById('studentSearchInput')?.value || '';
    const category = document.getElementById('studentCategoryFilter')?.value || '';
    const container = document.getElementById('studentBooksGrid');
    if (!container) return;

    container.innerHTML = '<div class="empty-state" style="grid-column: 1/-1;">Loading books...</div>';

    try {
      const books = await API.books.list(search, category);
      if (books.length === 0) {
        container.innerHTML = '<div class="empty-state" style="grid-column: 1/-1;"><div class="empty-icon">📖</div><div class="empty-text">No matching books found</div></div>';
        return;
      }

      container.innerHTML = books.map(b => {
        const canBorrow = b.availableCopies > 0;
        return `
          <div class="book-card">
            <div>
              <div class="book-category">${escapeHtml(b.category)}</div>
              <h4 class="book-title">${escapeHtml(b.title)}</h4>
              <p class="book-author">by <strong>${escapeHtml(b.author)}</strong></p>
              <p class="book-meta">ISBN: ${escapeHtml(b.isbn)} ${b.publicationYear ? `• ${b.publicationYear}` : ''}</p>
            </div>
            <div class="book-footer">
              <div>
                ${canBorrow ? `<span class="badge badge-in-stock">${b.availableCopies} Copies Available</span>` : `<span class="badge badge-out-of-stock">Out of Stock</span>`}
              </div>
              <button class="btn btn-primary btn-sm" ${!canBorrow ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''} onclick="StudentApp.borrowBook(${b.id}, '${escapeHtml(b.title).replace(/'/g, "\\'")}')">
                Borrow
              </button>
            </div>
          </div>
        `;
      }).join('');
    } catch (err) {
      container.innerHTML = '<div style="color: red; text-align: center; grid-column: 1/-1;">Error loading catalog</div>';
    }
  },

  async borrowBook(bookId, title) {
    if (!confirm(`Do you want to borrow "${title}" for 14 days?`)) return;

    try {
      const res = await API.transactions.borrow(bookId, 14);
      if (res.success) {
        showToast(res.message, 'success');
        await this.loadStats();
        await this.loadCatalog();
        await this.loadMyBorrowed();
        await this.loadMyHistory();
      } else {
        showToast(res.message, 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async loadMyBorrowed() {
    const tbody = document.getElementById('myBorrowedTableBody');
    if (!tbody) return;

    try {
      const list = await API.transactions.list();
      const active = list.filter(t => t.status !== 'RETURNED');

      if (active.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="empty-state"><div class="empty-icon">📚</div><div class="empty-text">You have no books currently borrowed. Browse the catalog to borrow one!</div></td></tr>';
        return;
      }

      tbody.innerHTML = active.map(t => {
        const isOverdue = t.isOverdue || t.status === 'OVERDUE';
        const badge = isOverdue ? 'badge badge-overdue' : 'badge badge-issued';
        return `
          <tr>
            <td><strong>#${t.id}</strong></td>
            <td><strong>${escapeHtml(t.bookTitle)}</strong><br><small style="color:var(--gray-600)">by ${escapeHtml(t.bookAuthor)}</small></td>
            <td>${t.issueDate}</td>
            <td><strong>${t.dueDate}</strong></td>
            <td>
              <span class="${badge}">${escapeHtml(t.status)}</span>
              ${t.fineAmount > 0 ? `<div style="color: var(--danger); font-weight:700;">Fine: $${t.fineAmount.toFixed(2)}</div>` : ''}
            </td>
            <td>
              <button class="btn btn-secondary btn-sm" onclick="StudentApp.returnBook(${t.id}, '${escapeHtml(t.bookTitle).replace(/'/g, "\\'")}')">Return</button>
            </td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      tbody.innerHTML = '<tr><td colspan="6" style="color: red; text-align: center;">Failed to load borrowed books</td></tr>';
    }
  },

  async returnBook(transId, title) {
    if (!confirm(`Confirm returning "${title}" to the library?`)) return;

    try {
      const res = await API.transactions.returnBook({ transactionId: transId });
      if (res.success) {
        showToast(res.message, 'success');
        await this.loadStats();
        await this.loadCatalog();
        await this.loadMyBorrowed();
        await this.loadMyHistory();
      } else {
        showToast(res.message, 'error');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  },

  async loadMyHistory() {
    const tbody = document.getElementById('myHistoryTableBody');
    if (!tbody) return;

    try {
      const list = await API.transactions.list();
      if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No transaction history yet.</td></tr>';
        return;
      }

      tbody.innerHTML = list.map(t => {
        let badge = 'badge badge-issued';
        if (t.status === 'RETURNED') badge = 'badge badge-returned';
        if (t.status === 'OVERDUE') badge = 'badge badge-overdue';

        return `
          <tr>
            <td>#${t.id}</td>
            <td><strong>${escapeHtml(t.bookTitle)}</strong></td>
            <td>${escapeHtml(t.bookAuthor)}</td>
            <td>${t.issueDate}</td>
            <td>${t.dueDate}</td>
            <td>${t.returnDate || '<em style="color:var(--gray-400)">Active</em>'}</td>
            <td><span class="${badge}">${escapeHtml(t.status)}</span></td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      tbody.innerHTML = '<tr><td colspan="7" style="color: red; text-align: center;">Failed to load history</td></tr>';
    }
  }
};
