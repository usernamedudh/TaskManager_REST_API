const API = '';
let token = localStorage.getItem('token');
let currentUser = localStorage.getItem('username');
let taskToDelete = null;
let allTasks = [];

// ---- INIT ----
window.onload = () => {
    if (token) showApp();
    else showAuth();
};

function showAuth() {
    document.getElementById('auth-page').classList.remove('hidden');
    document.getElementById('app-page').classList.add('hidden');
}

function showApp() {
    document.getElementById('auth-page').classList.add('hidden');
    document.getElementById('app-page').classList.remove('hidden');
    document.getElementById('user-name').textContent = currentUser;
    loadTasks();
}

// ---- AUTH TABS ----
function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');
    document.getElementById('login-form').classList.toggle('hidden', tab !== 'login');
    document.getElementById('register-form').classList.toggle('hidden', tab !== 'register');
    hideError('auth-error');
    hideError('reg-error');
}

// ---- LOGIN ----
async function handleLogin(e) {
    e.preventDefault();
    hideError('auth-error');
    const body = {
        username: document.getElementById('login-username').value,
        password: document.getElementById('login-password').value
    };
    try {
        const res = await fetch(`${API}/api/auth/login`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Invalid credentials');
        saveAuth(data);
        showApp();
    } catch (err) {
        showError('auth-error', err.message);
    }
}

// ---- REGISTER ----
async function handleRegister(e) {
    e.preventDefault();
    hideError('reg-error');
    const body = {
        username: document.getElementById('reg-username').value,
        email: document.getElementById('reg-email').value,
        password: document.getElementById('reg-password').value
    };
    try {
        const res = await fetch(`${API}/api/auth/register`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Registration failed');
        saveAuth(data);
        showApp();
    } catch (err) {
        showError('reg-error', err.message);
    }
}

function saveAuth(data) {
    token = data.token;
    currentUser = data.username;
    localStorage.setItem('token', token);
    localStorage.setItem('username', currentUser);
}

function logout() {
    token = null;
    currentUser = null;
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    showAuth();
}

// ---- TASKS ----
async function loadTasks() {
    try {
        const [tasksRes, statsRes] = await Promise.all([
            authFetch(`${API}/api/tasks?size=100`),
            authFetch(`${API}/api/tasks/stats`)
        ]);
        if (!tasksRes.ok || !statsRes.ok) { if (tasksRes.status === 401) { logout(); return; } }
        const tasksData = await tasksRes.json();
        const stats = await statsRes.json();
        allTasks = tasksData.content || [];
        renderBoard(allTasks);
        updateStats(stats);
    } catch (err) {
        console.error('Failed to load tasks', err);
    }
}

function renderBoard(tasks) {
    const columns = { TODO: [], IN_PROGRESS: [], DONE: [] };
    tasks.forEach(t => { if (columns[t.status]) columns[t.status].push(t); });

    renderColumn('list-todo', 'count-todo', columns.TODO);
    renderColumn('list-inprogress', 'count-inprogress', columns.IN_PROGRESS);
    renderColumn('list-done', 'count-done', columns.DONE);
}

function renderColumn(listId, countId, tasks) {
    const list = document.getElementById(listId);
    const count = document.getElementById(countId);
    count.textContent = tasks.length;

    if (tasks.length === 0) {
        list.innerHTML = `<div class="empty-col"><div class="empty-icon">📭</div><div>No tasks here</div></div>`;
        return;
    }

    list.innerHTML = tasks.map(task => {
        const due = task.dueDate ? formatDue(task.dueDate) : null;
        const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE';
        const nextBtn = nextStatusBtn(task.status);
        return `
        <div class="task-card priority-${task.priority}" id="card-${task.id}">
            <div class="task-card-header">
                <div class="task-title">${escHtml(task.title)}</div>
                <span class="priority-badge badge-${task.priority}">${task.priority}</span>
            </div>
            ${task.description ? `<div class="task-desc">${escHtml(task.description)}</div>` : ''}
            <div class="task-meta">
                ${due ? `<span class="due-date ${isOverdue ? 'overdue' : ''}">📅 ${due}</span>` : '<span></span>'}
            </div>
            <div class="task-actions">
                ${nextBtn ? `<button class="action-btn" onclick="moveTask(${task.id},'${nextBtn.status}')">${nextBtn.label}</button>` : '<span></span>'}
                <button class="action-btn edit-btn" onclick="openEditModal(${task.id})">✏️ Edit</button>
                <button class="action-btn delete-btn" onclick="openDeleteModal(${task.id})">🗑️ Delete</button>
            </div>
        </div>`;
    }).join('');
}

function nextStatusBtn(status) {
    if (status === 'TODO') return { status: 'IN_PROGRESS', label: '⚡ Start' };
    if (status === 'IN_PROGRESS') return { status: 'DONE', label: '✅ Done' };
    return null;
}

function updateStats(stats) {
    document.getElementById('stat-todo').textContent = stats.todo || 0;
    document.getElementById('stat-progress').textContent = stats.inProgress || 0;
    document.getElementById('stat-done').textContent = stats.done || 0;
}

async function moveTask(id, newStatus) {
    const task = allTasks.find(t => t.id === id);
    if (!task) return;
    try {
        const res = await authFetch(`${API}/api/tasks/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...task, status: newStatus, dueDate: task.dueDate || null })
        });
        if (res.ok) loadTasks();
    } catch (err) { console.error(err); }
}

// ---- MODAL ----
function openModal() {
    document.getElementById('modal-title').textContent = 'New Task';
    document.getElementById('task-id').value = '';
    document.getElementById('task-title').value = '';
    document.getElementById('task-desc').value = '';
    document.getElementById('task-priority').value = 'MEDIUM';
    document.getElementById('task-status').value = 'TODO';
    document.getElementById('task-due').value = '';
    document.getElementById('task-modal').classList.remove('hidden');
    document.getElementById('task-title').focus();
}

function openEditModal(id) {
    const task = allTasks.find(t => t.id === id);
    if (!task) return;
    document.getElementById('modal-title').textContent = 'Edit Task';
    document.getElementById('task-id').value = task.id;
    document.getElementById('task-title').value = task.title;
    document.getElementById('task-desc').value = task.description || '';
    document.getElementById('task-priority').value = task.priority;
    document.getElementById('task-status').value = task.status;
    document.getElementById('task-due').value = task.dueDate ? task.dueDate.slice(0, 16) : '';
    document.getElementById('task-modal').classList.remove('hidden');
}

function closeModal() {
    document.getElementById('task-modal').classList.add('hidden');
}

function closeModalOutside(e) {
    if (e.target === document.getElementById('task-modal')) closeModal();
}

async function handleSaveTask(e) {
    e.preventDefault();
    const id = document.getElementById('task-id').value;
    const dueRaw = document.getElementById('task-due').value;
    const body = {
        title: document.getElementById('task-title').value,
        description: document.getElementById('task-desc').value,
        priority: document.getElementById('task-priority').value,
        status: document.getElementById('task-status').value,
        dueDate: dueRaw ? dueRaw + ':00' : null
    };
    try {
        const url = id ? `${API}/api/tasks/${id}` : `${API}/api/tasks`;
        const method = id ? 'PUT' : 'POST';
        const res = await authFetch(url, {
            method, headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (res.ok) { closeModal(); loadTasks(); }
    } catch (err) { console.error(err); }
}

// ---- DELETE ----
function openDeleteModal(id) {
    taskToDelete = id;
    document.getElementById('delete-modal').classList.remove('hidden');
}

function closeDeleteModal() {
    taskToDelete = null;
    document.getElementById('delete-modal').classList.add('hidden');
}

function closeDeleteOutside(e) {
    if (e.target === document.getElementById('delete-modal')) closeDeleteModal();
}

async function confirmDelete() {
    if (!taskToDelete) return;
    try {
        await authFetch(`${API}/api/tasks/${taskToDelete}`, { method: 'DELETE' });
        closeDeleteModal();
        loadTasks();
    } catch (err) { console.error(err); }
}

// ---- HELPERS ----
function authFetch(url, opts = {}) {
    return fetch(url, {
        ...opts,
        headers: { ...opts.headers, Authorization: `Bearer ${token}` }
    });
}

function showError(id, msg) {
    const el = document.getElementById(id);
    el.textContent = msg;
    el.classList.remove('hidden');
}

function hideError(id) {
    document.getElementById(id).classList.add('hidden');
}

function escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function formatDue(iso) {
    const d = new Date(iso);
    return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
}
