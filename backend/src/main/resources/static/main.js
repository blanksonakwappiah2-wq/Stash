// Global Error Handler for Diagnostics
console.log("QuickBite: main.js is loading...");

window.onerror = function(message, source, lineno, colno, error) {
    const errorMsg = `CRITICAL JS ERROR: ${message}\nAt: ${source}:${lineno}:${colno}`;
    console.error(errorMsg, error);
    if (typeof logToScreen === 'function') logToScreen(errorMsg, true);
    alert(errorMsg + "\n\nPlease try 'Clear Cache & Reset' on the login screen.");
    return false;
};

// Log to Screen for easier debugging on Render
function logToScreen(msg, isError = false) {
    console.log(`[LOG] ${msg}`);
    const content = document.getElementById('debug-logs-content');
    if (!content) return;
    const time = new Date().toLocaleTimeString();
    content.innerHTML += `<div style="margin-bottom: 5px; color: ${isError ? '#ef4444' : '#22c55e'}">[${time}] ${msg}</div>`;
    content.scrollTop = content.scrollHeight;
}

window.logToScreen = logToScreen;

function toggleDebugConsole() {
    const consoleEl = document.getElementById('debug-console');
    if (consoleEl) {
        consoleEl.style.display = (consoleEl.style.display === 'none') ? 'block' : 'none';
        if (consoleEl.style.display === 'block') {
            logToScreen("--- Debug Console Opened ---");
        }
    }
}
window.toggleDebugConsole = toggleDebugConsole;

// Logic Heartbeat
function startHeartbeat() {
    const hb = document.getElementById('heartbeat');
    const statusText = document.getElementById('status-text');
    let state = false;
    setInterval(() => {
        state = !state;
        if (hb) hb.style.background = state ? '#22c55e' : '#94a3b8';
        if (statusText) statusText.textContent = "Logic: OK";
    }, 1000);
}

const AUTH_URL = "/api/users/";
const ORDER_URL = "/api/orders";
const RESTAURANT_URL = "/api/restaurants";
const DELIVERY_URL = "/api/delivery/";
const PERMISSION_URL = "/api/delivery/permissions";
const EMAIL_PATTERN = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;
const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

// Proactive Logging for Diagnostics
console.log("QuickBite: main.js loaded and executing...");
logToScreen("main.js module reached.");

// Outer Layout Containers (Harden these better in getters)
function getElement(id) {
    const el = document.getElementById(id);
    if (!el) {
        // Only log if it's a critical scene
        if (id.includes('scene') || id.includes('layout')) {
            console.warn(`CRITICAL ELEMENT MISSING: ${id}`);
        }
    }
    return el;
}

let currentUser = null;
let authToken = localStorage.getItem('authToken') || null;

try {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser && savedUser !== "undefined" && savedUser !== "null") {
        currentUser = JSON.parse(savedUser);
    }
} catch (e) {
    console.error("Session parse error:", e);
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
    currentUser = null;
    authToken = null;
}
let mgrMap = null;
let simulationInterval = null;
let agentMarkers = {}; // To track and move agent markers
let cart = []; // Shopping cart: [{menuItemId, name, price, quantity}]
let selectedRestaurantId = null;

// Secure Fetch Wrapper
async function secureFetch(url, options = {}) {
    const headers = options.headers || {};
    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }
    
    const response = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...headers
        }
    });

    if (response.status === 401) {
        // Token expired or invalid
        logout();
        return null;
    }
    
    return response;
}

// Switch between Top-Level Outer Containers
function switchOuterLayout(targetLayout) {
    const scenes = ['login-scene', 'register-scene', 'main-layout', 'verify-scene'];
    scenes.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.classList.remove('active');
    });
    if (targetLayout) {
        const el = (typeof targetLayout === 'string') ? document.getElementById(targetLayout) : targetLayout;
        if (el) el.classList.add('active');
    }
}

function toggleMobileSidebar(show) {
    const sb = document.getElementById('sidebar');
    const sbo = document.getElementById('sidebar-overlay');
    if (!sb || !sbo) return;
    if (show) {
        sb.classList.add('mobile-active');
        sbo.classList.add('active');
    } else {
        sb.classList.remove('mobile-active');
        sbo.classList.remove('active');
    }
}

function setupSidebar() {
    logToScreen("Setting up sidebar listeners...");
    const sidebar = document.getElementById('sidebar');
    const sidebarOverlay = document.getElementById('sidebar-overlay');
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');

    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener('click', () => toggleMobileSidebar(true));
    }
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', () => toggleMobileSidebar(false));
    }
    const logoutBtn = document.getElementById('sidebar-logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
}

// Switch between Inner Dashboard Panes
function switchPane(paneId, navBtnId) {
    // Hide all panes
    document.querySelectorAll('.content-pane').forEach(p => p.classList.remove('active'));
    // Show target pane
    const targetPane = document.getElementById(paneId);
    if (targetPane) targetPane.classList.add('active');
    
    // Update nav item active state
    document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
    if (navBtnId) {
        const btn = document.getElementById(navBtnId);
        if (btn) btn.classList.add('active');
    }

    // Mobile: Close sidebar after selection
    toggleMobileSidebar(false);

    // Special logic for restaurants pane
    if (paneId === 'restaurants-content') {
        fetchAndShowRestaurants();
    }

    // Special logic for manager sub-panes
    if (paneId === 'mgr-customers-content') {
        fetchAndShowCustomers();
    } else if (paneId === 'mgr-agents-content') {
        fetchAndShowAgents();
    } else if (paneId === 'mgr-feedback-content') {
        fetchAndShowFeedbacks();
    } else if (paneId === 'mgr-locations-content') {
        initManagerMap();
    } else if (paneId === 'agent-orders-content') {
        fetchAgentOrders();
    } else if (paneId === 'agent-history-content') {
        fetchAgentHistory();
    }

    // Clear simulation if not on map
    if (paneId !== 'mgr-locations-content' && paneId !== 'owner-tracking-content' && paneId !== 'tracking-content' && simulationInterval) {
        clearInterval(simulationInterval);
        simulationInterval = null;
    }

    // Special logic for owner pane
    if (paneId === 'owner-content') {
        fetchAndShowOwnerRestaurant();
    }

    // Special logic for tracking pane
    if (paneId === 'tracking-content') {
        initCustomerMap();
    } else if (paneId === 'owner-tracking-content') {
        initOwnerMap();
    }

    // Special logic for orders pane
    if (paneId === 'orders-content') {
        fetchAndShowOrders();
    }

    // Special logic for agent portal
    if (paneId === 'agent-content') {
        fetchAndShowAgentOrders();
        setTimeout(initAgentMap, 100);
    }

    if (paneId === 'availabilities-content') {
        updateAgentStatusUI();
    }

    if (paneId === 'mgr-permissions-content') {
        fetchAndShowPermissions();
    }
}

function setupNavListeners() {
    logToScreen("Setting up navigation listeners...");
    const navConfigs = [
        { id: 'nav-menu-btn', pane: 'home-content' },
        { id: 'nav-browse-btn', pane: 'restaurants-content' },
        { id: 'nav-owner-btn', pane: 'owner-content' },
        { id: 'nav-owner-tracking-btn', pane: 'owner-tracking-content' },
        { id: 'nav-agent-btn', pane: 'agent-content' },
        { id: 'nav-mgr-customers-btn', pane: 'mgr-customers-content' },
        { id: 'nav-mgr-owners-btn', pane: 'mgr-owners-content' },
        { id: 'nav-mgr-agents-btn', pane: 'mgr-agents-content' },
        { id: 'nav-mgr-locations-btn', pane: 'mgr-locations-content' },
        { id: 'nav-mgr-feedback-btn', pane: 'mgr-feedback-content' },
        { id: 'nav-mgr-account-btn', pane: 'mgr-account-content' },
        { id: 'nav-orders-btn', pane: 'orders-content' },
        { id: 'nav-tracking-btn', pane: 'tracking-content' },
        { id: 'nav-feedback-btn', pane: 'feedback-content' },
        { id: 'nav-account-btn', pane: 'account-content' },
        { id: 'nav-availabilities-btn', pane: 'availabilities-content' },
        { id: 'nav-mgr-permissions-btn', pane: 'mgr-permissions-content' }
    ];

    navConfigs.forEach(config => {
        const btn = document.getElementById(config.id);
        if (btn) {
            btn.addEventListener('click', () => switchPane(config.pane, config.id));
        }
    });

    const toReg = document.getElementById('go-to-register-btn');
    if (toReg) toReg.addEventListener('click', () => {
        switchOuterLayout('register-scene');
        const msg = document.getElementById('reg-message');
        if (msg) msg.style.display = 'none';
    });

    const backToLog = document.getElementById('back-to-login-btn');
    if (backToLog) backToLog.addEventListener('click', () => {
        switchOuterLayout('login-scene');
        const msg = document.getElementById('login-message');
        if (msg) msg.style.display = 'none';
    });
}

const clearLoginInputs = () => {
    document.getElementById('login-email').value = '';
    document.getElementById('login-password').value = '';
    document.getElementById('login-message').style.display = 'none';
};

const clearRegisterInputs = () => {
    document.getElementById('reg-name').value = '';
    document.getElementById('reg-email').value = '';
    document.getElementById('reg-password').value = '';
    document.getElementById('reg-confirm-password').value = '';
    document.getElementById('reg-message').style.display = 'none';
};

// Logout
function logout() {
    currentUser = null;
    authToken = null;
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
    clearLoginInputs();
    switchOuterLayout('login-scene');
}

function logoutAndReset() {
    console.log("Performing hard reset...");
    localStorage.clear();
    sessionStorage.clear();
    window.location.reload();
}

window.logoutAndReset = logoutAndReset;
document.getElementById('sidebar-logout-btn').addEventListener('click', logout);


function showAlert(message) {
    alert("QuickBite\n\n" + message);
}

// Global Password Visibility Toggle
function togglePasswordVisibility(inputId, toggleEl) {
    const input = document.getElementById(inputId);
    if (!input) return;
    
    if (input.type === 'password') {
        input.type = 'text';
        toggleEl.textContent = '🙈';
    } else {
        input.type = 'password';
        toggleEl.textContent = '👁️';
    }
}
window.togglePasswordVisibility = togglePasswordVisibility;

async function fetchAndShowCustomers() {
    try {
        const response = await secureFetch(AUTH_URL);
        if (!response || !response.ok) return;
        const allUsers = await response.json();
        const customers = allUsers.filter(u => u.role === 'CUSTOMER');
        
        const list = document.getElementById('customer-list');
        if (!list) return;
        list.innerHTML = '';
        
        if (customers.length === 0) {
            list.innerHTML = '<p class="label" style="grid-column: 1/-1; text-align: center; color: #64748b;">No customers registered yet.</p>';
        } else {
            customers.forEach(cust => {
                const card = document.createElement('div');
                card.className = 'agent-card';
                card.innerHTML = `
                    <div class="agent-avatar">👤</div>
                    <div>
                        <p class="label" style="font-weight: 700; color: #1e1b4b; margin: 0;">${cust.name}</p>
                        <p class="label" style="font-size: 0.8em; color: #64748b; margin: 0;">${cust.email}</p>
                    </div>
                `;
                list.appendChild(card);
            });
        }
    } catch (e) {
        console.error("Failed to load customers", e);
    }
}

async function fetchAndShowFeedbacks() {
    try {
        const response = await secureFetch('/api/reviews');
        if (!response || !response.ok) return;
        const reviews = await response.json();
        
        const list = document.getElementById('feedback-list');
        if (!list) return;
        list.innerHTML = '';
        
        if (reviews.length === 0) {
            list.innerHTML = '<p class="label" style="grid-column: 1/-1; text-align: center; color: #64748b; padding: 50px;">No feedback received yet.</p>';
        } else {
            reviews.forEach(rev => {
                const card = document.createElement('div');
                card.className = 'content-card';
                card.style.padding = '20px';
                card.innerHTML = `
                    <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
                        <span style="font-weight: 800; color: #4338ca;">${rev.customer ? rev.customer.name : 'Anonymous'}</span>
                        <span style="color: #f59e0b;">${'⭐'.repeat(rev.rating)}</span>
                    </div>
                    <p class="label" style="font-size: 0.9em; color: #1e1b4b; line-height: 1.5; font-style: italic;">"${rev.comment}"</p>
                    <div style="margin-top: 15px; font-size: 0.75em; color: #64748b; border-top: 1px solid #f1f5f9; pt: 10px;">
                        To: <span style="font-weight: 600;">${rev.restaurant ? rev.restaurant.name : 'System'}</span>
                    </div>
                `;
                list.appendChild(card);
            });
        }
    } catch (e) {
        console.error("Failed to load feedbacks", e);
    }
}

async function fetchAndShowRestaurants() {
    try {
        const response = await fetch(RESTAURANT_URL);
        if (!response) return;
        const restaurants = await response.json();
        
        const listContainer = document.getElementById('restaurants-list');
        listContainer.innerHTML = '';
        
        if (restaurants.length === 0) {
            listContainer.innerHTML = '<p class="label" style="text-align:center; grid-column: 1/-1; color: #64748b;">No restaurants available yet.</p>';
        } else {
            restaurants.forEach(rest => {
                const card = document.createElement('div');
                card.className = 'content-card';
                card.style.padding = '20px';
                
                card.innerHTML = `
                    ${rest.category ? `<span class="category-badge">${rest.category}</span>` : ''}
                    <h3 style="color: #6366f1; margin-bottom: 10px; font-weight: 800;">${rest.name}</h3>
                    <p class="label" style="font-size: 0.9em; margin-bottom: 5px; color: #4b5563;">📍 ${rest.address}</p>
                    <p class="label" style="font-size: 0.9em; margin-bottom: 15px; color: #4b5563;">📞 ${rest.contact}</p>
                    ${rest.website ? `<a href="${rest.website}" target="_blank" class="login-button" style="text-decoration: none; padding: 10px 18px; font-size: 0.8em; display: inline-block; text-align: center; border-radius: 10px;">Visit Website →</a>` : ''}
                `;
                listContainer.appendChild(card);
            });
        }
    } catch (e) {
        console.error("Failed to load restaurants", e);
    }
}

function showMessage(elementId, text, isSuccess) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.textContent = text;
    el.style.display = 'block';
    
    if (isSuccess) {
        el.style.color = '#10b981';
        el.style.backgroundColor = 'rgba(16, 185, 129, 0.1)';
        el.style.borderColor = 'rgba(16, 185, 129, 0.2)';
    } else {
        el.style.color = '#ef4444';
        el.style.backgroundColor = 'rgba(239, 68, 68, 0.1)';
        el.style.borderColor = 'rgba(239, 68, 68, 0.2)';
    }
}

// Logic for Login
async function handleLogin(emailOverride = null, passwordOverride = null) {
    const email = emailOverride || document.getElementById('login-email').value.trim();
    const password = passwordOverride || document.getElementById('login-password').value;

    if (!email || !password) {
        showMessage('login-message', "Both email and password are required.", false);
        return;
    }

    const loginBtn = document.getElementById('login-btn');
    if (loginBtn) {
        loginBtn.disabled = true;
        loginBtn.textContent = "Logging in...";
    }

    try {
        const response = await fetch(AUTH_URL + 'login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            currentUser = { id: data.id, name: data.name, email: data.email, role: data.role };
            authToken = data.token;
            
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            localStorage.setItem('authToken', authToken);
            localStorage.removeItem('temp_pass'); // Clean up temp pass

            updateNavigationForRole(currentUser.role);
            
            setTimeout(() => {
                switchOuterLayout('main-layout');
                if (currentUser.role === 'MANAGER' || currentUser.role === 'ADMIN') {
                    switchPane('mgr-customers-content', 'nav-mgr-customers-btn');
                } else if (currentUser.role === 'RESTAURANT_OWNER') {
                    switchPane('owner-content', 'nav-owner-btn');
                } else if (currentUser.role === 'DELIVERY_AGENT') {
                    switchPane('agent-orders-content', 'nav-agent-orders-btn');
                } else {
                    switchPane('home-content', 'nav-menu-btn');
                }
                const welcomeTitle = document.querySelector('.welcome-title');
                if (welcomeTitle) welcomeTitle.textContent = `Welcome back, ${currentUser.name}!`;
            }, 600);
        } else {
            const errorData = await response.json();
            showMessage('login-message', errorData.message || "Invalid credentials.", false);
        }
    } catch (e) {
        console.error("Login Error:", e);
        showMessage('login-message', "Connection error.", false);
    } finally {
        if (loginBtn) {
            loginBtn.disabled = false;
            loginBtn.textContent = "Login";
        }
    }
}

// Logic for Register
async function handleRegister() {
    const name = document.getElementById('reg-name').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const confirm = document.getElementById('reg-confirm-password').value;
    const role = 'CUSTOMER'; // Public registration is strictly Customer only

    if (!name || !email || !password || !confirm) {
        showMessage('reg-message', "Please fill in all fields.", false);
        return;
    }

    if (password !== confirm) {
        showMessage('reg-message', "Passwords do not match.", false);
        return;
    }

    if (!EMAIL_PATTERN.test(email)) {
        showMessage('reg-message', "Invalid email format.", false);
        return;
    }

    if (password.length < 8) {
        showMessage('reg-message', "Password must be at least 8 characters.", false);
        return;
    }

    const regBtn = document.getElementById('register-btn');
    regBtn.disabled = true;
    regBtn.textContent = "Registering...";

    console.log("Attempting registration for:", email);
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 20000); // 20s timeout

        const response = await fetch(AUTH_URL + 'register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role }),
            signal: controller.signal
        });
        clearTimeout(timeoutId);

        if (response.ok) {
            localStorage.setItem('temp_verify_email', email);
            localStorage.setItem('temp_pass', password); // Store temp pass for auto-login
            switchOuterLayout('verify-scene');
        } else {
            const err = await response.json();
            showMessage('reg-message', err.message || "Registration failed.", false);
        }
    } catch (e) {
        console.error("Registration Error:", e);
        if (e.name === 'AbortError') {
            showMessage('reg-message', "Registration timed out. The server might be waking up (Render Free Tier can take 1-2 minutes). Please try again in a moment.", false);
        } else {
            showMessage('reg-message', "Connection error. Please check your internet or try again later.", false);
        }
    } finally {
        regBtn.disabled = false;
        regBtn.textContent = "Register";
    }
}

// Manager Action: Add Restaurant
async function handleAddRestaurant() {
    const name = document.getElementById('rest-name').value.trim();
    const category = document.getElementById('rest-category').value.trim();
    const address = document.getElementById('rest-address').value.trim();
    const contact = document.getElementById('rest-contact').value.trim();
    const website = document.getElementById('rest-website').value.trim();
    const ownerName = document.getElementById('owner-name').value.trim();
    const ownerEmail = document.getElementById('owner-email').value.trim();
    const ownerPassword = document.getElementById('owner-password').value;

    if (!name || !address || !contact || !ownerName || !ownerEmail || !ownerPassword) {
        showMessage('manager-message', "Please fill in all fields (including owner details).", false);
        return;
    }

    try {
        const response = await secureFetch(RESTAURANT_URL, {
            method: 'POST',
            body: JSON.stringify({ 
                name, category, address, contact, website, 
                ownerName, ownerEmail, ownerPassword 
            })
        });

        if (response && response.ok) {
            showMessage('manager-message', "Restaurant and Owner account created successfully!", true);
            document.getElementById('rest-name').value = '';
            document.getElementById('rest-category').value = '';
            document.getElementById('rest-address').value = '';
            document.getElementById('rest-contact').value = '';
            document.getElementById('rest-website').value = '';
            document.getElementById('owner-name').value = '';
            document.getElementById('owner-email').value = '';
            document.getElementById('owner-password').value = '';
        } else {
            const error = await response.json();
            showMessage('manager-message', error.message || "Failed to add restaurant.", false);
        }
    } catch (e) {
        showMessage('manager-message', "Error connecting to server.", false);
    }
}

// Manager Action: Add Delivery Agent
async function handleAddAgent() {
    const name = document.getElementById('agent-name').value.trim();
    const email = document.getElementById('agent-email').value.trim();
    const password = document.getElementById('agent-password').value;

    if (!name || !EMAIL_PATTERN.test(email) || !password) {
        showMessage('agent-message', "Please fill in all fields correctly.", false);
        return;
    }

    try {
        const response = await secureFetch(DELIVERY_URL + 'agents', {
            method: 'POST',
            body: JSON.stringify({ name, email, password })
        });

        if (response && response.ok) {
            showMessage('agent-message', "Delivery agent registered successfully!", true);
            document.getElementById('agent-name').value = '';
            document.getElementById('agent-email').value = '';
            document.getElementById('agent-password').value = '';
            fetchAndShowAgents();
        } else {
            const error = await response.json();
            showMessage('agent-message', error.message || "Failed to register agent.", false);
        }
    } catch (e) {
        showMessage('agent-message', "Error connecting to server.", false);
    }
}

// Manager Action: Update Own Account
async function handleUpdateManager() {
    if (!currentUser) return;
    
    const name = document.getElementById('mgr-update-name').value.trim();
    const email = document.getElementById('mgr-update-email').value.trim();
    const password = document.getElementById('mgr-update-password').value;

    if (!name && !email && !password) {
        showMessage('mgr-update-message', "Please provide at least one field to update.", false);
        return;
    }

    const payload = {};
    if (name) payload.name = name;
    if (email) {
        if (!EMAIL_PATTERN.test(email)) {
            showMessage('mgr-update-message', "Invalid email format.", false);
            return;
        }
        payload.email = email;
    }
    if (password) {
        if (password.length < 8) {
            showMessage('mgr-update-message', "Password must be at least 8 characters.", false);
            return;
        }
        payload.password = password;
    }

    try {
        const response = await secureFetch(`/api/users/${currentUser.id}`, {
            method: 'PUT',
            body: JSON.stringify(payload)
        });

        if (response && response.ok) {
            const updatedUser = await response.json();
            currentUser = { ...currentUser, ...updatedUser };
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMessage('mgr-update-message', "Account updated successfully!", true);
            document.getElementById('mgr-update-name').value = '';
            document.getElementById('mgr-update-email').value = '';
            document.getElementById('mgr-update-password').value = '';
            
            const welcomeTitle = document.querySelector('.welcome-title');
            if (welcomeTitle) welcomeTitle.textContent = `Welcome back, ${updatedUser.name}!`;
        } else {
            const error = await response.json();
            showMessage('mgr-update-message', error.message || "Update failed.", false);
        }
    } catch (e) {
        showMessage('mgr-update-message', "Error connecting to server.", false);
    }
}

async function fetchAndShowAgents() {
    try {
        const response = await secureFetch(DELIVERY_URL + 'agents');
        if (!response || !response.ok) return;
        const agents = await response.json();
        const list = document.getElementById('agent-list');
        if (!list) return;
        list.innerHTML = '';

        if (agents.length === 0) {
            list.innerHTML = '<p class="label" style="grid-column: 1/-1; color: #64748b;">No agents registered yet.</p>';
        } else {
            agents.forEach(agent => {
                const card = document.createElement('div');
                card.className = 'agent-card';
                card.innerHTML = `
                    <div class="agent-avatar">🚴</div>
                    <div>
                        <p class="label" style="font-weight: 700; color: #1e1b4b; margin: 0;">${agent.name}</p>
                        <p class="label" style="font-size: 0.8em; color: #64748b; margin: 0;">${agent.email}</p>
                    </div>
                `;
                list.appendChild(card);
            });
        }
    } catch (e) {
        console.error("Failed to load agents", e);
    }
}

function setDisplay(id, display) {
    const el = document.getElementById(id);
    if (el) el.style.display = display;
}

function updateNavigationForRole(role) {
    // Hide everything first
    document.querySelectorAll('.nav-item').forEach(btn => btn.style.display = 'none');
    
    // Role-specific visibility
    if (role === 'CUSTOMER') {
        setDisplay('nav-menu-btn', 'flex');
        setDisplay('nav-browse-btn', 'flex');
        setDisplay('nav-orders-btn', 'flex');
        setDisplay('nav-tracking-btn', 'flex');
        setDisplay('nav-feedback-btn', 'flex');
        setDisplay('nav-account-btn', 'flex');
    } else if (role === 'MANAGER' || role === 'ADMIN') {
        setDisplay('nav-mgr-customers-btn', 'flex');
        setDisplay('nav-mgr-owners-btn', 'flex');
        setDisplay('nav-mgr-agents-btn', 'flex');
        setDisplay('nav-mgr-locations-btn', 'flex');
        setDisplay('nav-mgr-feedback-btn', 'flex');
        setDisplay('nav-mgr-permissions-btn', 'flex');
        setDisplay('nav-mgr-account-btn', 'flex');
    } else if (role === 'RESTAURANT_OWNER') {
        setDisplay('nav-owner-btn', 'flex');
        setDisplay('nav-owner-tracking-btn', 'flex');
        const ordersBtn = document.getElementById('nav-orders-btn');
        if (ordersBtn) {
            ordersBtn.style.display = 'flex';
            ordersBtn.textContent = '📋 Orders';
        }
        setDisplay('nav-feedback-btn', 'flex');
    } else if (role === 'DELIVERY_AGENT') {
        setDisplay('nav-agent-btn', 'flex');
        setDisplay('nav-availabilities-btn', 'flex');
        setDisplay('nav-feedback-btn', 'flex');
    }
}

// Manager: Fetch & Display Customers
async function fetchAndShowCustomers() {
    try {
        const response = await secureFetch(AUTH_URL);
        if (!response || !response.ok) return;
        const users = await response.json();
        const customers = users.filter(u => u.role === 'CUSTOMER');
        const list = document.getElementById('customer-list');
        if (!list) return;

        if (customers.length === 0) {
            list.innerHTML = '<p class="label" style="grid-column: 1/-1; text-align: center; padding: 40px; color: #94a3b8;">No customers registered yet.</p>';
            return;
        }

        list.innerHTML = customers.map(c => `
            <div class="content-card" style="border: 1px solid #e2e8f0; margin-bottom: 0;">
                <div style="display: flex; align-items: center; gap: 15px;">
                    <div style="width: 50px; height: 50px; background: linear-gradient(135deg, #6366f1, #a855f7); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 1.3em; flex-shrink: 0;">👤</div>
                    <div>
                        <p style="font-weight: 700; color: #1e1b4b; margin: 0;">${c.name}</p>
                        <p style="font-size: 0.85em; color: #64748b; margin: 2px 0;">${c.email}</p>
                        ${c.phone ? `<p style="font-size: 0.8em; color: #94a3b8; margin: 0;">📞 ${c.phone}</p>` : ''}
                    </div>
                    <span class="category-badge" style="margin-left: auto; background: #e0e7ff; color: #4338ca;">Customer</span>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error('Failed to load customers', e);
    }
}

// Manager: Fetch & Display Feedbacks
async function fetchAndShowFeedbacks() {
    const list = document.getElementById('feedback-list');
    if (!list) return;
    list.innerHTML = '<p class="label" style="grid-column: 1/-1; text-align: center; padding: 30px; color: #94a3b8;">Loading feedback... 🔄</p>';

    try {
        const response = await secureFetch('/api/reviews');
        if (!response || !response.ok) {
            list.innerHTML = '<p class="label" style="grid-column: 1/-1; text-align: center; padding: 40px; color: #94a3b8;">No feedback submitted yet. 🌟</p>';
            return;
        }
        const feedbacks = await response.json();

        if (!feedbacks || feedbacks.length === 0) {
            list.innerHTML = '<p class="label" style="grid-column: 1/-1; text-align: center; padding: 40px; color: #94a3b8;">No feedback submitted yet. 🌟</p>';
            return;
        }

        list.innerHTML = feedbacks.map(f => `
            <div class="content-card" style="border: 1px solid #e2e8f0; margin-bottom: 0;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <div style="width: 40px; height: 40px; background: linear-gradient(135deg, #f59e0b, #ef4444); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white;">💬</div>
                        <div>
                            <p style="font-weight: 700; color: #1e1b4b; margin: 0;">${f.customer?.name || 'Anonymous'}</p>
                            <p style="font-size: 0.8em; color: #94a3b8; margin: 0;">${f.createdAt ? new Date(f.createdAt).toLocaleDateString() : 'Recent'}</p>
                        </div>
                    </div>
                    <div style="color: #f59e0b;">${'⭐'.repeat(Math.min(f.rating || 5, 5))}</div>
                </div>
                <p style="color: #475569; font-size: 0.95em; line-height: 1.6; background: #f8fafc; padding: 12px; border-radius: 10px; margin: 0;">"${f.comment}"</p>
            </div>
        `).join('');
    } catch (e) {
        console.error('Failed to load feedbacks', e);
        list.innerHTML = '<p class="label" style="grid-column: 1/-1; text-align: center; padding: 40px; color: #ef4444;">Error loading feedback. Please try again.</p>';
    }
}

async function initManagerMap() {
    if (mgrMap) return;
    mgrMap = L.map('mgr-map').setView([5.6037, -0.1870], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(mgrMap);
    
    // Periodically fetch active agents and update markers
    setInterval(async () => {
        try {
            const resp = await secureFetch(DELIVERY_URL + "active-agents");
            if (!resp.ok) return;
            const agents = await resp.json();
            
            // Clear old markers
            Object.values(agentMarkers).forEach(m => mgrMap.removeLayer(m.marker));
            agentMarkers = {};

            agents.forEach(agent => {
                const marker = L.marker([agent.latitude, agent.longitude], {
                    icon: L.divIcon({ html: '🚴', className: 'agent-icon', iconSize: [30, 30] })
                }).addTo(mgrMap).bindPopup(`<b>Agent: ${agent.name}</b><br>Status: Active`);
                agentMarkers[agent.id] = { marker, lat: agent.latitude, lng: agent.longitude };
            });
        } catch (e) { console.error("Map update error", e); }
    }, 3000);
}

function initOwnerMap() {
    if (typeof L === 'undefined') {
        logToScreen("Leaflet (L) is MISSING. Owner Map cannot initialize.", true);
        return;
    }
    const mapDiv = document.getElementById('owner-map');
    if (!mapDiv) return;

    if (mgrMap) {
        mgrMap.remove();
    }

    mgrMap = L.map('owner-map').setView([51.505, -0.09], 14);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(mgrMap);

    fetchMapData(mgrMap, 'RESTAURANT_OWNER');
}
// ── Map Location Search (Nominatim / OpenStreetMap) ──────────────────────────
let searchMarker = null;

async function searchOnMap(target) {
    const inputId = target === 'mgr' ? 'mgr-map-search' : 'customer-map-search';
    const query = (document.getElementById(inputId)?.value || '').trim();
    if (!query) return;

    const resultEl = document.getElementById('map-search-result');
    if (resultEl) {
        resultEl.style.display = 'block';
        resultEl.textContent = 'Searching...';
    }

    try {
        const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&limit=1`;
        const res = await fetch(url, { headers: { 'Accept-Language': 'en' } });
        const data = await res.json();

        if (!data.length) {
            if (resultEl) resultEl.textContent = `❌ No results found for "${query}". Try a different search.`;
            return;
        }

        const place = data[0];
        const lat = parseFloat(place.lat);
        const lng = parseFloat(place.lon);
        const name = place.display_name;

        // Pan the correct map
        const targetMap = mgrMap;
        if (!targetMap) {
            if (resultEl) resultEl.textContent = '⚠️ Map not loaded yet. Open the map first.';
            return;
        }

        targetMap.setView([lat, lng], 14);

        // Remove old search marker if present
        if (searchMarker) {
            searchMarker.remove();
        }

        searchMarker = L.marker([lat, lng])
            .addTo(targetMap)
            .bindPopup(`<strong>📍 ${name.split(',').slice(0, 3).join(', ')}</strong>`)
            .openPopup();

        if (resultEl) {
            resultEl.style.display = 'block';
            resultEl.textContent = `✅ Found: ${name.split(',').slice(0, 3).join(', ')}`;
        }
    } catch (e) {
        console.error('Geocoding error:', e);
        if (resultEl) resultEl.textContent = '❌ Search failed. Check your connection and try again.';
    }
}

window.searchOnMap = searchOnMap;

 
function initCustomerMap() {
    if (typeof L === 'undefined') {
        logToScreen("Leaflet (L) is MISSING. Customer Map cannot initialize.", true);
        return;
    }
    const mapDiv = document.getElementById('map');
    if (!mapDiv) return;

    if (mgrMap) {
        mgrMap.remove();
    }

    mgrMap = L.map('map').setView([51.505, -0.09], 14);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(mgrMap);

    fetchMapData(mgrMap, 'CUSTOMER');
}

async function fetchMapData(targetMap, role) {
    try {
        const [usersRes, restsRes] = await Promise.all([
            secureFetch(AUTH_URL),
            fetch(RESTAURANT_URL)
        ]);

        if (!usersRes.ok || !restsRes.ok) return;

        const allUsers = await usersRes.json();
        const restaurants = await restsRes.json();

        // Filter data if Owner
        let filteredRests = restaurants;
        let filteredAgents = allUsers.filter(u => u.role === 'DELIVERY_AGENT');
        let filteredCusts = allUsers.filter(u => u.role === 'CUSTOMER');

        // Note: For real filtering, we'd need to know which agents/customers belong to this owner's orders.
        // For demonstration, we'll show a subset.

        // Plot Restaurants
        filteredRests.forEach(rest => {
            const lat = rest.latitude || 51.505 + (Math.random() - 0.5) * 0.05;
            const lng = rest.longitude || -0.09 + (Math.random() - 0.5) * 0.05;
            
            L.circleMarker([lat, lng], {
                color: '#6366f1',
                fillColor: '#6366f1',
                fillOpacity: 0.8,
                radius: 8
            }).addTo(targetMap)
              .bindPopup(`<strong>🏪 ${rest.name}</strong>`);
        });

        // Plot Agents + Start Simulation
        agentMarkers = {};
        filteredAgents.forEach(agent => {
            const lat = agent.latitude || 51.505 + (Math.random() - 0.5) * 0.1;
            const lng = agent.longitude || -0.09 + (Math.random() - 0.5) * 0.1;
            
            const marker = L.circleMarker([lat, lng], {
                color: '#10b981',
                fillColor: '#10b981',
                fillOpacity: 1,
                radius: 10,
                weight: 2
            }).addTo(targetMap)
              .bindPopup(`<strong>🚴 Agent: ${agent.name}</strong>`);
               
            agentMarkers[agent.id] = {
                marker: marker,
                lat: lat,
                lng: lng,
                dx: (Math.random() - 0.5) * 0.0005,
                dy: (Math.random() - 0.5) * 0.0005
            };
        });

        // Plot Customers
        filteredCusts.forEach(cust => {
            const lat = cust.latitude || 51.505 + (Math.random() - 0.5) * 0.08;
            const lng = cust.longitude || -0.09 + (Math.random() - 0.5) * 0.08;
            
            L.circleMarker([lat, lng], {
                color: '#f59e0b',
                fillColor: '#f59e0b',
                fillOpacity: 0.8,
                radius: 6
            }).addTo(targetMap)
              .bindPopup(`<strong>👤 Customer: ${cust.name}</strong>`);
        });

        startSimulation();

    } catch (e) {
        console.error("Map data fetch failed", e);
    }
}

// --- Customer Portal Functionalization ---

async function fetchAndShowRestaurants() {
    try {
        const response = await fetch(RESTAURANT_URL);
        let restaurants = await response.json();
        
        // Filter: If user is an OWNER, only show THEIR restaurant
        if (currentUser && currentUser.role === 'RESTAURANT_OWNER') {
            restaurants = restaurants.filter(r => r.ownerId === currentUser.id);
        }
        
        const list = document.getElementById('restaurants-list');
        list.innerHTML = restaurants.map(rest => {
            const displayImage = rest.imageUrl || `https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80&sig=${rest.id}`;
            const locationText = rest.address ? `📍 ${rest.address.split(',')[0]}` : '📍 Nearby - 1.2km';
            
            return `
                <div class="restaurant-card" onclick="showRestaurantMenu(${rest.id}, '${rest.name.replace(/'/g, "\\'")}')">
                    <div class="restaurant-image" style="background-image: url('${displayImage}')"></div>
                    <div class="restaurant-info">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 5px;">
                            <h3 class="restaurant-name">${rest.name}</h3>
                            <span class="category-badge" style="font-size: 0.6em; padding: 4px 8px;">${rest.category}</span>
                        </div>
                        <div class="location-tag">${locationText}</div>
                        <div class="restaurant-meta">
                            <span style="color: #f59e0b; font-weight: 700;">⭐ 4.8</span>
                            <span style="color: #64748b; font-size: 0.85em;">Free Delivery over $20</span>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        // Show cart if logged as CUSTOMER
        const cartSidebar = document.getElementById('customer-cart-sidebar');
        if (currentUser && currentUser.role === 'CUSTOMER') {
            cartSidebar.style.display = 'flex';
            updateCartUI();
        } else {
            cartSidebar.style.display = 'none';
        }
    } catch (e) {
        console.error("Failed to load restaurants", e);
    }
}

async function fetchAndShowOwnerRestaurant() {
    try {
        const response = await secureFetch(RESTAURANT_URL);
        if (!response || !response.ok) return;
        const allRestaurants = await response.json();
        const myRestaurant = allRestaurants.find(r => r.ownerId === currentUser.id);
        
        const detailsDiv = document.getElementById('owner-restaurant-details');
        const noRestMsg = document.getElementById('owner-no-restaurant-msg');
        const viewPublicBtn = document.getElementById('owner-view-public-btn');
        
        if (myRestaurant) {
            detailsDiv.style.display = 'block';
            noRestMsg.style.display = 'none';
            viewPublicBtn.style.display = 'block';
            document.getElementById('owner-rest-img').src = myRestaurant.imageUrl || 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80';
            document.getElementById('owner-rest-name').textContent = myRestaurant.name;
            document.getElementById('owner-rest-address').textContent = myRestaurant.address;
            document.getElementById('owner-rest-contact').textContent = myRestaurant.contact;
            document.getElementById('owner-rest-category').textContent = myRestaurant.category;
            
            viewPublicBtn.onclick = () => {
                showRestaurantMenu(myRestaurant.id, myRestaurant.name.replace(/'/g, "\\'"));
            };
        } else {
            detailsDiv.style.display = 'none';
            noRestMsg.style.display = 'block';
            viewPublicBtn.style.display = 'none';
        }
    } catch (e) {
        console.error("Failed to load owner's restaurant", e);
    }
}

async function showRestaurantMenu(restaurantId, restaurantName) {
    selectedRestaurantId = restaurantId;
    switchPane('restaurant-menu-content', 'nav-browse-btn');
    
    // Header
    const header = document.getElementById('menu-header');
    header.innerHTML = `
        <h1 class="title-label" style="text-align: left; color: #1e1b4b; margin: 0;">${restaurantName}</h1>
        <p class="label" style="color: #64748b;">Browse our fresh menu items and build your order.</p>
    `;

    try {
        const response = await fetch(`/api/menu/restaurant/${restaurantId}`);
        const items = await response.json();
        
        const list = document.getElementById('menu-items-list');
        if (items.length === 0) {
            list.innerHTML = `<p class="label" style="grid-column: 1/-1; text-align: center; padding: 40px; color: #94a3b8;">No items found for this restaurant yet. 🥙</p>`;
            return;
        }

        list.innerHTML = items.map(item => `
            <div class="restaurant-card" style="cursor: default;">
                <div class="restaurant-image" style="background-image: url('https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300&q=80&sig=${item.id}')"></div>
                <div class="restaurant-info">
                    <h3 class="restaurant-name">${item.name}</h3>
                    <p class="restaurant-category" style="height: 40px; overflow: hidden; margin-bottom: 10px;">${item.description || 'Delicious meal prepared with fresh ingredients.'}</p>
                    <div class="restaurant-meta">
                        <span style="font-weight: 700; color: #1e1b4b; font-size: 1.1em;">$${item.price.toFixed(2)}</span>
                        <button class="login-button" style="padding: 8px 15px; font-size: 0.8em; margin: 0; width: auto;" onclick="addToCart(${item.id}, '${item.name.replace(/'/g, "\\'")}', ${item.price})">Add to Cart</button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error("Failed to load menu", e);
    }
}

function addToCart(id, name, price) {
    const existing = cart.find(i => i.menuItemId === id);
    if (existing) {
        existing.quantity += 1;
    } else {
        cart.push({ menuItemId: id, name, price, quantity: 1 });
    }
    updateCartUI();
}

function removeFromCart(id) {
    cart = cart.filter(i => i.menuItemId !== id);
    updateCartUI();
}

function updateCartUI() {
    const list = document.getElementById('cart-items');
    const totalEl = document.getElementById('cart-total');
    const checkoutBtn = document.getElementById('checkout-btn');

    if (cart.length === 0) {
        list.innerHTML = `<p class="label" style="text-align: center; color: #94a3b8; padding-top: 20px;">Your cart is empty.</p>`;
        totalEl.innerText = '$0.00';
        checkoutBtn.disabled = true;
        return;
    }

    let total = 0;
    list.innerHTML = cart.map(item => {
        total += item.price * item.quantity;
        return `
            <div style="display: flex; justify-content: space-between; margin-bottom: 15px; background: white; padding: 10px; border-radius: 8px; border: 1px solid #e2e8f0;">
                <div>
                    <p style="font-weight: 600; font-size: 0.9em; color: #1e1b4b; margin: 0;">${item.name}</p>
                    <p style="font-size: 0.8em; color: #64748b; margin: 0;">$${item.price.toFixed(2)} x ${item.quantity}</p>
                </div>
                <button onclick="removeFromCart(${item.menuItemId})" style="background: none; border: none; color: #ef4444; cursor: pointer; font-size: 1.1em;">×</button>
            </div>
        `;
    }).join('');

    totalEl.innerText = `$${total.toFixed(2)}`;
    checkoutBtn.disabled = false;
}

function openOrderModal() {
    if (!currentUser || cart.length === 0 || !selectedRestaurantId) return;
    
    const overlay = document.getElementById('order-confirm-overlay');
    if (overlay) {
        overlay.style.display = 'flex';
        updateOrderModalValues();
    }
}

function closeOrderModal() {
    const overlay = document.getElementById('order-confirm-overlay');
    if (overlay) overlay.style.display = 'none';
}

function updateOrderModalValues() {
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const select = document.getElementById('delivery-method-select');
    const deliveryFee = parseFloat(select.options[select.selectedIndex].dataset.fee);
    
    document.getElementById('confirm-subtotal').innerText = `$${subtotal.toFixed(2)}`;
    document.getElementById('confirm-delivery-fee').innerText = `$${deliveryFee.toFixed(2)}`;
    document.getElementById('confirm-total').innerText = `$${(subtotal + deliveryFee).toFixed(2)}`;
}

document.getElementById('delivery-method-select').addEventListener('change', updateOrderModalValues);
document.getElementById('final-confirm-btn').addEventListener('click', confirmFinalOrder);

async function confirmFinalOrder() {
    const select = document.getElementById('delivery-method-select');
    const deliveryOptionId = parseInt(select.value);
    const confirmBtn = document.getElementById('final-confirm-btn');
    
    confirmBtn.disabled = true;
    confirmBtn.innerText = 'Processing...';

    const orderRequest = {
        restaurantId: selectedRestaurantId,
        deliveryAddress: currentUser.address || 'Standard Delivery Address',
        deliveryOptionId: deliveryOptionId,
        items: cart.map(i => ({ menuItemId: i.menuItemId, quantity: i.quantity }))
    };

    try {
        const response = await secureFetch('/api/orders', {
            method: 'POST',
            body: JSON.stringify(orderRequest)
        });

        if (response.ok) {
            alert('🎉 Order placed successfully!');
            cart = [];
            updateCartUI();
            closeOrderModal();
            switchPane('orders-content', 'nav-orders-btn');
            fetchAndShowOrders();
        } else {
            const err = await response.json();
            alert('Failed to place order: ' + (err.message || 'Unknown error'));
        }
    } catch (e) {
        console.error("Order placement failed", e);
        alert('An error occurred while placing your order.');
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.innerText = 'Confirm & Pay';
    }
}

// Feedback Logic
document.getElementById('submit-feedback-btn').addEventListener('click', submitFeedback);

async function submitFeedback() {
    const comment = document.getElementById('feedback-comment').value.trim();
    if (!comment) return;

    const btn = document.getElementById('submit-feedback-btn');
    btn.disabled = true;
    btn.innerText = 'Sending...';

    try {
        const response = await secureFetch('/api/reviews', {
            method: 'POST',
            body: JSON.stringify({
                comment: comment,
                customer: { id: currentUser.id },
                rating: 5 // Default for general feedback
            })
        });

        if (response.ok) {
            showMessage('feedback-message', "Thank you! Your feedback has been sent to the manager.", true);
            document.getElementById('feedback-comment').value = '';
        } else {
            showMessage('feedback-message', "Failed to send feedback. Please try again.", false);
        }
    } catch (e) {
        console.error("Feedback error:", e);
        showMessage('feedback-message', "Connection error. Please try again.", false);
    } finally {
        btn.disabled = false;
        btn.innerText = 'Submit Feedback';
    }
}

// Profile Logic
document.getElementById('save-profile-btn').addEventListener('click', saveProfile);

async function saveProfile() {
    const name = document.getElementById('profile-name').value.trim();
    const address = document.getElementById('profile-address').value.trim();
    const phone = document.getElementById('profile-phone').value.trim();
    const password = document.getElementById('profile-password').value;

    if (!name) return;

    const btn = document.getElementById('save-profile-btn');
    btn.disabled = true;
    btn.innerText = 'Saving...';

    const updateRequest = {
        name: name,
        email: currentUser.email,
        address: address,
        phone: phone
    };
    if (password) updateRequest.password = password;

    try {
        const response = await secureFetch(`/api/users/${currentUser.id}`, {
            method: 'PUT',
            body: JSON.stringify(updateRequest)
        });

        if (response.ok) {
            const updatedUser = await response.json();
            currentUser.name = updatedUser.name;
            currentUser.address = updatedUser.address;
            currentUser.phone = updatedUser.phone;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showMessage('profile-message', "Profile updated successfully!", true);
            const welcomeTitle = document.querySelector('.welcome-title');
            if (welcomeTitle) welcomeTitle.textContent = `Welcome back, ${currentUser.name}!`;
        } else {
            showMessage('profile-message', "Update failed. Please try again.", false);
        }
    } catch (e) {
        console.error("Profile update error:", e);
        showMessage('profile-message', "Connection error.", false);
    } finally {
        btn.disabled = false;
        btn.innerText = 'Save Changes';
    }
}

function updateNavigationForRole(role) {
    const navItems = {
        'CUSTOMER': ['nav-menu-btn', 'nav-browse-btn', 'nav-orders-btn', 'nav-tracking-btn', 'nav-account-btn'],
        'MANAGER': ['nav-menu-btn', 'nav-mgr-customers-btn', 'nav-mgr-owners-btn', 'nav-mgr-agents-btn', 'nav-mgr-feedback-btn', 'nav-mgr-locations-btn', 'nav-mgr-permissions-btn', 'nav-account-btn'],
        'RESTAURANT_OWNER': ['nav-menu-btn', 'nav-owner-btn', 'nav-owner-tracking-btn', 'nav-orders-btn', 'nav-account-btn'],
        'DELIVERY_AGENT': ['nav-menu-btn', 'nav-agent-orders-btn', 'nav-agent-history-btn', 'nav-agent-avail-btn', 'nav-account-btn']
    };

    document.querySelectorAll('.nav-item').forEach(item => item.style.display = 'none');
    const activeNavs = navItems[role] || navItems['CUSTOMER'];
    activeNavs.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'flex';
    });
}

async function fetchAgentOrders() {
    try {
        const response = await secureFetch(ORDER_URL + `/agent/${currentUser.id}`);
        if (!response.ok) return;
        const orders = await response.json();
        const list = document.getElementById('agent-orders-list');
        if (orders.length === 0) {
            list.innerHTML = '<p class="label" style="text-align: center; margin-top: 20px; color: #94a3b8;">No active deliveries. 😴</p>';
            return;
        }
        list.innerHTML = orders.filter(o => o.status !== 'DELIVERED').map(order => `
            <div class="content-card" style="border-left: 5px solid #6366f1;">
                <h3 style="color: #1e1b4b;">Order #${order.id} - ${order.status}</h3>
                <p class="label" style="color: #64748b;">🏪 ${order.restaurantName} ➔ 📍 ${order.customerName}</p>
                <p class="label" style="color: #64748b; font-size: 0.9em;">🏠 Address: ${order.deliveryAddress}</p>
                <div style="margin-top: 15px; display: flex; gap: 10px;">
                    ${order.status === 'DELIVERING' ? 
                        `<button class="login-button" style="margin:0; background:#10b981;" onclick="updateOrderStatus(${order.id}, 'DELIVERED')">Complete</button>` :
                        `<button class="login-button" style="margin:0;" onclick="updateOrderStatus(${order.id}, 'DELIVERING')">Start</button>`
                    }
                    <button class="register-button" style="margin:0;" onclick="initAgentMapForOrder(${order.id})">Map</button>
                </div>
            </div>
        `).join('');
    } catch (e) { console.error(e); }
}

async function fetchAgentHistory() {
    try {
        const response = await secureFetch(ORDER_URL + `/agent-history`);
        if (!response.ok) return;
        const orders = await response.json();
        const list = document.getElementById('agent-history-list');
        if (orders.length === 0) {
            list.innerHTML = '<p class="label" style="text-align: center; margin-top: 20px; color: #94a3b8;">No history found. 🧺</p>';
            return;
        }
        list.innerHTML = orders.map(order => `
            <div class="content-card" style="opacity: 0.8;">
                <div style="display: flex; justify-content: space-between;">
                    <span style="font-weight:700;">Order #${order.id}</span>
                    <span class="category-badge">DELIVERED</span>
                </div>
                <p style="font-size: 0.9em; color: #64748b; margin-top: 10px;">${order.restaurantName} to ${order.customerName}</p>
            </div>
        `).join('');
    } catch (e) { console.error(e); }
}

function loadProfileToUI() {
    if (!currentUser) return;
    document.getElementById('profile-name').value = currentUser.name || '';
    document.getElementById('profile-email').value = currentUser.email || '';
    document.getElementById('profile-address').value = currentUser.address || '';
    document.getElementById('profile-phone').value = currentUser.phone || '';
    document.getElementById('profile-password').value = '';
}

// Expose to window
window.closeOrderModal = closeOrderModal;
window.submitFeedback = submitFeedback;
window.saveProfile = saveProfile;
window.loadProfileToUI = loadProfileToUI;

// Update navigation click listeners to handle extra work
document.getElementById('nav-account-btn').addEventListener('click', loadProfileToUI);
const checkoutBtn = document.getElementById('checkout-btn');
if (checkoutBtn) {
    checkoutBtn.addEventListener('click', openOrderModal);
}

async function fetchAndShowOrders() {
    try {
        const titleEl = document.getElementById('orders-title');
        const descEl = document.getElementById('orders-message');
        
        if (currentUser.role === 'CUSTOMER') {
            if (titleEl) titleEl.textContent = "My Orders";
            if (descEl) descEl.textContent = "You haven't placed any orders yet. Start exploring now!";
        } else if (currentUser.role === 'RESTAURANT_OWNER') {
            if (titleEl) titleEl.textContent = "Orders Received";
            if (descEl) descEl.textContent = "History of orders ordered by customers from your restaurant.";
        }
        
        let url = ORDER_URL;
        if (currentUser.role === 'CUSTOMER') {
            url = ORDER_URL + `/customer/${currentUser.id}`;
        } else if (currentUser.role === 'RESTAURANT_OWNER') {
            const restResp = await secureFetch(RESTAURANT_URL);
            if (!restResp || !restResp.ok) return;
            const allRests = await restResp.json();
            const myRest = allRests.find(r => r.ownerId === currentUser.id);
            if (!myRest) {
                const list = document.getElementById('orders-list');
                const message = document.getElementById('orders-message');
                message.textContent = "You don't have a restaurant assigned to you.";
                message.style.display = 'block';
                list.innerHTML = '';
                return;
            }
            url = ORDER_URL + `/restaurant/${myRest.id}`;
        }

        const response = await secureFetch(url);
        if (!response.ok) return;
        const orders = await response.json();
        
        const list = document.getElementById('orders-list');
        const message = document.getElementById('orders-message');
        
        if (orders.length === 0) {
            message.style.display = 'block';
            if (currentUser.role === 'RESTAURANT_OWNER') {
                message.textContent = "No orders received from customers yet. 📥";
            } else {
                message.textContent = "You haven't placed any orders yet. Start exploring now!";
            }
            list.innerHTML = '';
            return;
        }

        message.style.display = 'none';
        list.innerHTML = orders.map(order => `
            <div class="content-card" style="border: 1px solid #e2e8f0; margin-bottom: 0;">
                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; margin-bottom: 15px;">
                    <span style="font-weight: 700; color: #1e1b4b;">Order #${order.id}</span>
                    <span class="category-badge" style="background: ${getStatusColor(order.status).bg}; color: ${getStatusColor(order.status).text};">${order.status}</span>
                </div>
                <div style="font-size: 0.9em; color: #64748b;">
                    <p style="margin: 5px 0;">🏪 From: <strong>${order.restaurantName}</strong></p>
                    <p style="margin: 5px 0;">💰 Total: <strong>$${order.totalAmount.toFixed(2)}</strong></p>
                    <p style="margin: 5px 0;">🕒 Placed: ${new Date(order.orderTime).toLocaleString()}</p>
                </div>
                <div style="margin-top: 15px; display: flex; gap: 10px;">
                    <button class="login-button" style="margin: 0; padding: 8px 15px; font-size: 0.8em; flex: 1;" onclick="initTrackingForOrder(${order.id})">Track Order</button>
                    <button class="register-button" style="margin: 0; padding: 8px 15px; font-size: 0.8em; flex: 1; border-color: #cbd5e1;">Review</button>
                </div>
            </div>
        `).reverse().join('');

    } catch (e) {
        console.error("Failed to load orders", e);
    }
}

function getStatusColor(status) {
    switch (status) {
        case 'PLACED': return { bg: '#e0e7ff', text: '#4338ca' };
        case 'PREPARING': return { bg: '#fffbeb', text: '#d97706' };
        case 'READY_FOR_PICKUP': return { bg: '#dcfce7', text: '#166534' };
        case 'OUT_FOR_DELIVERY': return { bg: '#fdf2f2', text: '#dc2626' };
        case 'DELIVERED': return { bg: '#f1f5f9', text: '#475569' };
        default: return { bg: '#f1f5f9', text: '#475569' };
    }
}

function initTrackingForOrder(orderId) {
    switchPane('tracking-content', 'nav-tracking-btn');
    // For now, the generic map initializes via startSimulation/initMap
}

function startSimulation() {
    if (simulationInterval) clearInterval(simulationInterval);
    
    simulationInterval = setInterval(() => {
        Object.keys(agentMarkers).forEach(id => {
            const data = agentMarkers[id];
            data.lat += data.dx;
            data.lng += data.dy;
            
            if (Math.abs(data.lat - 51.505) > 0.1) data.dx *= -1;
            if (Math.abs(data.lng + 0.09) > 0.1) data.dy *= -1;
            
            data.marker.setLatLng([data.lat, data.lng]);
        });
    }, 1000); 
}

// Session Initialization
function initSession() {
    logToScreen("Initializing session check...");
    try {
        console.log("Checking session...", { authToken: !!authToken, currentUser: !!currentUser });
        if (authToken && currentUser) {
            logToScreen(`Session found for: ${currentUser.email} (${currentUser.role})`);
            updateNavigationForRole(currentUser.role);
            switchOuterLayout('main-layout');
            
            // Land on default pane based on role
            if (currentUser.role === 'MANAGER' || currentUser.role === 'ADMIN') {
                switchPane('mgr-customers-content', 'nav-mgr-customers-btn');
            } else if (currentUser.role === 'RESTAURANT_OWNER') {
                switchPane('owner-content', 'nav-owner-btn');
            } else if (currentUser.role === 'DELIVERY_AGENT') {
                switchPane('agent-orders-content', 'nav-agent-orders-btn');
            } else {
                switchPane('home-content', 'nav-menu-btn');
            }

            const welcomeTitle = document.querySelector('.welcome-title');
            if (welcomeTitle) welcomeTitle.textContent = `Welcome back, ${currentUser.name}!`;
        } else {
            logToScreen("No active session. Showing login.");
            switchOuterLayout('login-scene');
        }
    } catch (e) {
        console.error("Initialization Failed:", e);
        logToScreen("Initialization CRASHED: " + e.message, true);
        switchOuterLayout('login-scene');
    }
}

// Global initialization call on window load
window.addEventListener('load', () => {
    console.log("QuickBite: Window load event fired.");
    logToScreen("Initializing application...");
    try {
        logToScreen("-> Initializing Sidebar...");
        setupSidebar();
        
        logToScreen("-> Initializing Navigation...");
        setupNavListeners();
        
        logToScreen("-> Binding Authentication Buttons...");
        const loginBtn = document.getElementById('login-btn');
        if (loginBtn) {
            loginBtn.addEventListener('click', handleLogin);
            logToScreen("   [OK] Login button bound.");
        } else {
            logToScreen("   [WARN] Login button NOT FOUND in DOM.", true);
        }

        const regBtn = document.getElementById('register-btn');
        if (regBtn) {
            regBtn.addEventListener('click', handleRegister);
            logToScreen("   [OK] Register button bound.");
        } else {
            logToScreen("   [WARN] Register button NOT FOUND in DOM.", true);
        }

        logToScreen("-> Binding Manager Action Buttons...");
        const addRestBtn = document.getElementById('add-rest-btn');
        if (addRestBtn) addRestBtn.addEventListener('click', handleAddRestaurant);
        const addAgentBtn = document.getElementById('add-agent-btn');
        if (addAgentBtn) addAgentBtn.addEventListener('click', handleAddAgent);
        const updateMgrBtn = document.getElementById('update-manager-btn');
        if (updateMgrBtn) updateMgrBtn.addEventListener('click', handleUpdateManager);

        logToScreen("-> Binding Delivery Agent Action Buttons...");
        const toggleStatusBtn = document.getElementById('toggle-status-btn');
        if (toggleStatusBtn) toggleStatusBtn.addEventListener('click', toggleAgentOnlineStatus);
        
        const submitPermBtn = document.getElementById('submit-perm-btn');
        if (submitPermBtn) submitPermBtn.addEventListener('click', submitPermissionRequest);

        const verifyBtn = document.getElementById('verify-btn');
        if (verifyBtn) verifyBtn.addEventListener('click', handleVerifyCode);

        logToScreen("Initialization complete.");
    } catch (e) {
        logToScreen("Initialization FAILED at: " + e.stack, true);
        console.error("Setup Error:", e);
    }
    startHeartbeat();
    initSession();
});

// Manager: Fetch & Display Agents
async function fetchAndShowAgents() {
    try {
        const response = await secureFetch(AUTH_URL);
        if (!response || !response.ok) return;
        const users = await response.json();
        const agents = users.filter(u => u.role === 'DELIVERY_AGENT');
        const list = document.getElementById('agent-list');
        if (!list) return;

        if (agents.length === 0) {
            list.innerHTML = '<p class="label" style="grid-column: 1/-1; text-align: center; padding: 40px; color: #94a3b8;">No delivery agents registered yet.</p>';
            return;
        }

        list.innerHTML = agents.map(a => `
            <div class="content-card" style="border: 1px solid #e2e8f0; margin-bottom: 0;">
                <div style="display: flex; align-items: center; gap: 15px;">
                    <div style="width: 50px; height: 50px; background: linear-gradient(135deg, #10b981, #059669); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 1.3em; flex-shrink: 0;">🚴</div>
                    <div>
                        <p class="label" style="font-weight: 700; color: #1e1b4b; margin: 0;">${a.name}</p>
                        <p class="label" style="font-size: 0.8em; color: #64748b; margin: 0;">${a.email}</p>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error('Failed to load agents', e);
    }
}

// ── Delivery Agent Portal Logic ──────────────────────────────────────────────
let agentMap = null;
let agentLocationWatcher = null;
let agentMarker = null;

async function fetchAndShowAgentOrders() {
    fetchAgentOrders();
}

let agentOrderMap = null;
let agentMarkersGroup = null;

async function initAgentMapForOrder(orderId) {
    document.getElementById('agent-tracking-map-container').style.display = 'block';
    
    if (!agentOrderMap) {
        agentOrderMap = L.map('agent-map').setView([5.6037, -0.1870], 13);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(agentOrderMap);
        agentMarkersGroup = L.featureGroup().addTo(agentOrderMap);
    }

    try {
        const resp = await secureFetch(ORDER_URL + "/" + orderId);
        if (!resp.ok) return;
        const order = await resp.json();

        agentMarkersGroup.clearLayers();

        const agentIcon = L.divIcon({ html: '<div class="map-marker-icon">🚴</div>', className: '', iconSize: [35, 35] });
        const restIcon = L.divIcon({ html: '<div class="map-marker-icon">🏠</div>', className: '', iconSize: [35, 35] });
        const custIcon = L.divIcon({ html: '<div class="map-marker-icon">📍</div>', className: '', iconSize: [35, 35] });

        let agentLat = 5.6037, agentLng = -0.1870;
        const agentMarker = L.marker([agentLat, agentLng], { icon: agentIcon }).addTo(agentMarkersGroup).bindPopup("<b>Me (Agent)</b>");

        if (navigator.geolocation) {
            navigator.geolocation.watchPosition(pos => {
                agentLat = pos.coords.latitude;
                agentLng = pos.coords.longitude;
                agentMarker.setLatLng([agentLat, agentLng]);
                updateAgentLiveLocation(agentLat, agentLng);
            });
        }

        const restLat = agentLat + 0.005;
        const restLng = agentLng + 0.008;
        L.marker([restLat, restLng], { icon: restIcon }).addTo(agentMarkersGroup).bindPopup(`<b>Restaurant: ${order.restaurantName}</b>`);

        const custLat = agentLat + 0.012;
        const custLng = agentLng - 0.005;
        L.marker([custLat, custLng], { icon: custIcon }).addTo(agentMarkersGroup).bindPopup(`<b>Deliver to: ${order.customerName}</b><br>${order.deliveryAddress}`);

        agentOrderMap.fitBounds(agentMarkersGroup.getBounds(), { padding: [50, 50] });

    } catch (e) { console.error("Agent map error", e); }
}

async function updateAgentLiveLocation(lat, lng) {
    await secureFetch(DELIVERY_URL + "update-location", {
        method: 'POST',
        body: JSON.stringify({ latitude: lat, longitude: lng })
    });
}

function updateOrderStatus(orderId, status) {
    secureFetch(ORDER_URL + "/" + orderId + "/status?status=" + status, { method: 'PUT' })
        .then(res => {
            if (res.ok) {
                fetchAgentOrders();
                if (status === 'DELIVERED') {
                    document.getElementById('agent-tracking-map-container').style.display = 'none';
                    fetchAgentHistory();
                }
            }
        });
}

async function transferOrderToFleet(orderId) {
    if (!confirm("Are you sure you want to release this order back to the fleet? Another agent will need to claim it.")) return;
    
    try {
        const response = await secureFetch(`/api/delivery/orders/${orderId}/transfer`, { method: 'POST' });
        if (response && response.ok) {
            logToScreen(`Order #${orderId} released to fleet.`);
            fetchAndShowAgentOrders();
        }
    } catch (e) {
        console.error('Failed to transfer order', e);
    }
}

async function toggleAgentOnlineStatus() {
    const isCurrentlyOnline = currentUser.isOnline || false;
    const newStatus = !isCurrentlyOnline;
    
    try {
        const response = await secureFetch('/api/delivery/status', {
            method: 'PUT',
            body: JSON.stringify({ agentId: currentUser.id, isOnline: newStatus })
        });
        
        if (response && response.ok) {
            currentUser.isOnline = newStatus;
            updateAgentStatusUI();
            logToScreen(`You are now ${newStatus ? 'ONLINE' : 'OFFLINE'}.`);
        }
    } catch (e) {
        console.error('Failed to update status', e);
    }
}

function updateAgentStatusUI() {
    const textEl = document.getElementById('agent-status-text');
    const btnEl = document.getElementById('toggle-status-btn');
    if (!textEl || !btnEl) return;
    
    const isOnline = currentUser.isOnline || false;
    if (isOnline) {
        textEl.innerHTML = 'You are currently <strong style="color: #10b981;">Online</strong> and ready for orders.';
        btnEl.textContent = 'Go Offline';
        btnEl.style.background = '#ef4444';
        if (!agentLocationWatcher) startLocationTracking();
    } else {
        textEl.innerHTML = 'You are currently <strong>Offline</strong>.';
        btnEl.textContent = 'Go Online';
        btnEl.style.background = '#6366f1';
        stopLocationTracking();
    }
}

function initAgentMap() {
    const mapDiv = document.getElementById('agent-delivery-map');
    if (!mapDiv) return;

    if (agentMap) {
        agentMap.remove();
        agentMap = null;
    }

    agentMap = L.map('agent-delivery-map').setView([5.6, -0.2], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(agentMap);
    
    logToScreen("Agent portal map initialized.");
}

async function submitPermissionRequest() {
    const start = document.getElementById('perm-start-date').value;
    const end = document.getElementById('perm-end-date').value;
    const reason = document.getElementById('perm-reason').value;
    const msgEl = document.getElementById('perm-message');
    
    if (!start || !end || !reason) {
        msgEl.style.display = 'block';
        msgEl.style.color = '#ef4444';
        msgEl.textContent = "Please fill in all fields.";
        return;
    }
    
    const payload = {
        agent: { id: currentUser.id },
        startDate: start,
        endDate: end,
        reason: reason
    };

    try {
        const response = await secureFetch(PERMISSION_URL, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        if (response && response.ok) {
            msgEl.style.display = 'block';
            msgEl.style.color = '#10b981';
            msgEl.textContent = "Permission request submitted to manager! ✅";
            logToScreen(`[PERMISSION] Request from ${currentUser.name} submitted.`);
            
            // Clear form
            document.getElementById('perm-start-date').value = '';
            document.getElementById('perm-end-date').value = '';
            document.getElementById('perm-reason').value = '';
        } else {
            const err = await response.json();
            msgEl.style.display = 'block';
            msgEl.style.color = '#ef4444';
            msgEl.textContent = err.message || "Failed to submit request.";
        }
    } catch (e) {
        console.error("Error submitting permission", e);
        msgEl.style.display = 'block';
        msgEl.style.color = '#ef4444';
        msgEl.textContent = "Connection error.";
    }
}

async function fetchAndShowPermissions() {
    try {
        const response = await secureFetch(PERMISSION_URL);
        if (!response || !response.ok) return;
        const requests = await response.json();
        const list = document.getElementById('mgr-permissions-list');
        if (!list) return;

        if (requests.length === 0) {
            list.innerHTML = '<p class="label" style="grid-column: 1/-1; text-align: center; padding: 40px; color: #94a3b8;">No permission requests found.</p>';
            return;
        }

        list.innerHTML = requests.map(req => `
            <div class="content-card" style="border: 1px solid #e2e8f0; margin-bottom: 0;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                    <span class="category-badge" style="background: ${getPermissionStatusColor(req.status).bg}; color: ${getPermissionStatusColor(req.status).text};">${req.status}</span>
                    <span style="font-size: 0.8em; color: #64748b;">Requested on: ${new Date().toLocaleDateString()}</span>
                </div>
                <div style="margin-bottom: 15px;">
                    <p class="label" style="font-weight: 700; color: #1e1b4b; margin: 0;">${req.agent.name}</p>
                    <p class="label" style="font-size: 0.85em; color: #64748b; margin: 2px 0;">${req.startDate} to ${req.endDate}</p>
                    <p class="label" style="font-size: 0.9em; color: #1e1b4b; margin-top: 10px; font-style: italic;">"${req.reason}"</p>
                </div>
                ${req.status === 'PENDING' ? `
                <div class="button-group" style="margin-top: 15px;">
                    <button class="login-button" style="padding: 8px 15px; font-size: 0.85em; background: #10b981;" onclick="handlePermissionAction(${req.id}, 'approve')">Approve</button>
                    <button class="register-button" style="padding: 8px 15px; font-size: 0.85em; background: #ef4444; border: none; color: white;" onclick="handlePermissionAction(${req.id}, 'reject')">Reject</button>
                </div>
                ` : ''}
            </div>
        `).join('');
    } catch (e) {
        console.error('Failed to load permissions', e);
    }
}

function getPermissionStatusColor(status) {
    switch (status) {
        case 'PENDING': return { bg: '#fffbeb', text: '#d97706' };
        case 'APPROVED': return { bg: '#dcfce7', text: '#166534' };
        case 'REJECTED': return { bg: '#fdf2f2', text: '#dc2626' };
        default: return { bg: '#f1f5f9', text: '#475569' };
    }
}

async function handlePermissionAction(id, action) {
    const confirmMsg = action === 'approve' ? 'Are you sure you want to APPROVE this request?' : 'Are you sure you want to REJECT this request?';
    if (!confirm(confirmMsg)) return;

    try {
        const response = await secureFetch(`${PERMISSION_URL}/${id}/${action}`, { method: 'PUT' });
        if (response && response.ok) {
            logToScreen(`Permission ${id} ${action}d.`);
            fetchAndShowPermissions();
        } else {
            alert(`Failed to ${action} request.`);
        }
    } catch (e) {
        console.error(`Failed to ${action} permission`, e);
    }
}

async function handleVerifyCode() {
    const code = document.getElementById('verify-code').value.trim();
    const email = localStorage.getItem('temp_verify_email');
    const msgEl = document.getElementById('verify-message');

    if (code.length !== 6) {
        showMessage('verify-message', "Please enter a valid 6-digit code.", false);
        return;
    }

    try {
        const response = await fetch('/api/users/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, code })
        });

        if (response.ok) {
            const password = localStorage.getItem('temp_pass');
            localStorage.removeItem('temp_verify_email');
            
            // Visual Success Flow
            document.getElementById('verify-input-container').style.display = 'none';
            document.getElementById('verify-success-container').style.display = 'block';
            
            setTimeout(() => {
                if (password) {
                    handleLogin(email, password);
                } else {
                    switchOuterLayout('login-scene');
                    showMessage('login-message', "Account verified! You can now login.", true);
                }
                // Reset Verify Scene for next time
                document.getElementById('verify-input-container').style.display = 'block';
                document.getElementById('verify-success-container').style.display = 'none';
                document.getElementById('verify-code').value = '';
            }, 3500);
        } else {
            const err = await response.json();
            showMessage('verify-message', err.message || "Invalid code.", false);
        }
    } catch (e) {
        console.error("Verification error", e);
        showMessage('verify-message', "Connection error.", false);
    }
}

function resendVerificationCode() {
    logToScreen("Resend code requested (Simulated). Check logs.");
    // In a real app, this would hit an endpoint to generate/send a new code
}

function startLocationTracking() {
    if (!navigator.geolocation) {
        logToScreen("Geolocation is not supported by this browser.", true);
        return;
    }

    logToScreen("Starting high-accuracy live location tracking...");
    agentLocationWatcher = navigator.geolocation.watchPosition(
        (position) => {
            const { latitude, longitude } = position.coords;
            updateAgentPosition(latitude, longitude);
        },
        (error) => {
            console.error("Geolocation error:", error);
            logToScreen(`Location error: ${error.message}`, true);
        },
        { 
            enableHighAccuracy: true, 
            maximumAge: 0, 
            timeout: 10000 
        }
    );
}

function stopLocationTracking() {
    if (agentLocationWatcher !== null) {
        navigator.geolocation.clearWatch(agentLocationWatcher);
        agentLocationWatcher = null;
        logToScreen("Location tracking stopped.");
    }
    if (agentMarker && agentMap) {
        agentMap.removeLayer(agentMarker);
        agentMarker = null;
    }
}

function updateAgentPosition(lat, lng) {
    if (!agentMap) return;

    if (!agentMarker) {
        agentMarker = L.marker([lat, lng], {
            icon: L.divIcon({
                className: 'agent-location-marker',
                html: '<div style="background: white; border-radius: 50%; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 10px rgba(0,0,0,0.2); border: 2px solid #6366f1;">🚴</div>',
                iconSize: [30, 30],
                iconAnchor: [15, 15]
            })
        }).addTo(agentMap);
        agentMap.setView([lat, lng], 15);
    } else {
        agentMarker.setLatLng([lat, lng]);
    }

    console.log(`[SYNC] Agent location: ${lat}, ${lng}`);
}
