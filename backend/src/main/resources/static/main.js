// Global Error Handler for Diagnostics
window.onerror = function(message, source, lineno, colno, error) {
    const errorMsg = `CRITICAL JS ERROR: ${message}\nAt: ${source}:${lineno}:${colno}`;
    console.error(errorMsg, error);
    logToScreen(errorMsg, true);
    alert(errorMsg + "\n\nPlease try 'Clear Cache & Reset' on the login screen.");
    return false;
};

// Log to Screen for easier debugging on Render
function logToScreen(msg, isError = false) {
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

const BACKEND_URL = "/api/users/";
const RESTAURANT_URL = "/api/restaurants";
const DELIVERY_URL = "/api/delivery/";
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
    const scenes = ['login-scene', 'register-scene', 'main-layout'];
    scenes.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.classList.remove('active');
    });
    if (targetLayout) {
        // targetLayout can be an element or an ID
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
    }

    // Clear simulation if not on map
    if (paneId !== 'mgr-locations-content' && paneId !== 'owner-tracking-content' && paneId !== 'tracking-content' && simulationInterval) {
        clearInterval(simulationInterval);
        simulationInterval = null;
    }

    // Special logic for owner tracking
    if (paneId === 'owner-tracking-content') {
        initOwnerMap();
    }

    // Special logic for tracking pane
    if (paneId === 'tracking-content') {
        initMap();
    }

    // Special logic for orders pane
    if (paneId === 'orders-content') {
        fetchAndShowOrders();
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
        { id: 'nav-availabilities-btn', pane: 'availabilities-content' }
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

async function fetchAndShowCustomers() {
    try {
        const response = await secureFetch(BACKEND_URL + 'users');
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
async function handleLogin() {
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!email || !password) {
        showMessage('login-message', "Both email and password are required.", false);
        return;
    }

    const loginBtn = document.getElementById('login-btn');
    loginBtn.disabled = true;
    loginBtn.textContent = "Logging in...";

    console.log("Attempting login for:", email);
    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 15000); // 15s timeout

        const response = await fetch(BACKEND_URL + 'login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
            signal: controller.signal
        });
        clearTimeout(timeoutId);
        
        console.log("Login response status:", response.status);
        if (response.ok) {
            const data = await response.json();
            console.log("Login successful, user role:", data.role);
            currentUser = {
                id: data.id,
                name: data.name,
                email: data.email,
                role: data.role
            };
            authToken = data.token;
            
            // Save to localStorage
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            localStorage.setItem('authToken', authToken);

            showMessage('login-message', "Login successful! Welcome back.", true);
            
            updateNavigationForRole(currentUser.role);
            
            setTimeout(() => {
                switchOuterLayout(mainLayout);
                // Redirect to role-specific default pane
                if (currentUser.role === 'MANAGER' || currentUser.role === 'ADMIN') {
                    switchPane('mgr-customers-content', 'nav-mgr-customers-btn');
                } else if (currentUser.role === 'RESTAURANT_OWNER') {
                    switchPane('owner-content', 'nav-owner-btn');
                } else if (currentUser.role === 'DELIVERY_AGENT') {
                    switchPane('agent-content', 'nav-agent-btn');
                } else {
                    switchPane('home-content', 'nav-menu-btn');
                }
                
                const welcomeTitle = document.querySelector('.welcome-title');
                if (welcomeTitle) welcomeTitle.textContent = `Welcome back, ${currentUser.name}!`;
            }, 600);
        } else {
            const errorData = await response.json();
            showMessage('login-message', errorData.message || "Invalid credentials. Please try again.", false);
        }
    } catch (e) {
        console.error("Login Error:", e);
        if (e.name === 'AbortError') {
            showMessage('login-message', "Login timed out. The server might be waking up (Render Free Tier can take 1-2 minutes). Please try again in a moment.", false);
        } else {
            showMessage('login-message', "Connection error. Please check your internet or try again later.", false);
        }
    }
}

// Logic for Register
async function handleRegister() {
    const name = document.getElementById('reg-name').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const confirm = document.getElementById('reg-confirm-password').value;
    const role = document.getElementById('reg-role').value;

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

        const response = await fetch(BACKEND_URL + 'register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role }),
            signal: controller.signal
        });
        clearTimeout(timeoutId);

        console.log("Registration response status:", response.status);
        if (response.ok) {
            const data = await response.json();
            console.log("Registration successful, user role:", data.role);
            currentUser = {
                id: data.id,
                name: data.name,
                email: data.email,
                role: data.role
            };
            authToken = data.token;
            
            // Save to localStorage
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            localStorage.setItem('authToken', authToken);

            showMessage('reg-message', "Registration successful! Welcome to QuickBite.", true);
            
            updateNavigationForRole(currentUser.role);
            
            setTimeout(() => {
                switchOuterLayout(mainLayout);
                // Redirect to role-specific default pane
                if (currentUser.role === 'MANAGER' || currentUser.role === 'ADMIN') {
                    switchPane('mgr-customers-content', 'nav-mgr-customers-btn');
                } else if (currentUser.role === 'RESTAURANT_OWNER') {
                    switchPane('owner-content', 'nav-owner-btn');
                } else if (currentUser.role === 'DELIVERY_AGENT') {
                    switchPane('agent-content', 'nav-agent-btn');
                } else {
                    switchPane('home-content', 'nav-menu-btn');
                }
                
                const welcomeTitle = document.querySelector('.welcome-title');
                if (welcomeTitle) welcomeTitle.textContent = `Welcome, ${currentUser.name}!`;
                clearRegisterInputs();
            }, 1000);
        }
    } catch (e) {
        console.error("Registration Error:", e);
        if (e.name === 'AbortError') {
            showMessage('reg-message', "Registration timed out. The server might be waking up (Render Free Tier can take 1-2 minutes). Please try again in a moment.", false);
        } else {
            showMessage('reg-message', "Connection error. Please check your internet or try again later.", false);
        }
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
        setDisplay('nav-mgr-account-btn', 'flex');
    } else if (role === 'RESTAURANT_OWNER') {
        setDisplay('nav-owner-btn', 'flex');
        setDisplay('nav-owner-tracking-btn', 'flex');
        setDisplay('nav-orders-btn', 'flex');
        setDisplay('nav-feedback-btn', 'flex');
    } else if (role === 'DELIVERY_AGENT') {
        setDisplay('nav-agent-btn', 'flex');
        setDisplay('nav-availabilities-btn', 'flex');
        setDisplay('nav-feedback-btn', 'flex');
    }
}

// Manager Map & Real-time Simulation
function initManagerMap() {
    if (typeof L === 'undefined') {
        logToScreen("Leaflet (L) is MISSING. Manager Map cannot initialize.", true);
        return;
    }
    if (mgrMap) {
        mgrMap.remove();
    }

    mgrMap = L.map('mgr-map').setView([51.505, -0.09], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(mgrMap);

    // Sidebar: Delivery Progress List
    const deliveryList = document.getElementById('mgr-live-delivery-list');
    if (deliveryList) {
        deliveryList.innerHTML = `
            <div style="background: #fdf2f2; border: 1px solid #fee2e2; padding: 12px; border-radius: 10px; margin-bottom: 12px;">
                <p class="label" style="font-weight:700; color:#991b1b; margin-bottom:5px;">📦 Order #7821</p>
                <p class="label" style="font-size:0.75em; color:#64748b; margin-bottom:8px;">Grandma's Kitchen → Sarah J.</p>
                <div style="height:6px; background:#f1f5f9; border-radius:3px; overflow:hidden;">
                    <div style="width:65%; height:100%; background:#ef4444;"></div>
                </div>
                <p class="label" style="font-size:0.7em; margin-top:5px; color:#ef4444; font-weight:600;">🚴 On Route (Estimated 4m)</p>
            </div>
            <div style="background: #f0fdf4; border: 1px solid #dcfce7; padding: 12px; border-radius: 10px; margin-bottom: 12px;">
                <p class="label" style="font-weight:700; color:#166534; margin-bottom:5px;">📦 Order #7823</p>
                <p class="label" style="font-size:0.75em; color:#64748b; margin-bottom:8px;">Burger King → Mark R.</p>
                <div style="height:6px; background:#f1f5f9; border-radius:3px; overflow:hidden;">
                    <div style="width:30%; height:100%; background:#22c55e;"></div>
                </div>
                <p class="label" style="font-size:0.7em; margin-top:5px; color:#22c55e; font-weight:600;">👨‍🍳 Preparing</p>
            </div>
            <div style="background: #fffbeb; border: 1px solid #fef3c7; padding: 12px; border-radius: 10px;">
                <p class="label" style="font-weight:700; color:#92400e; margin-bottom:5px;">📦 Order #7822</p>
                <p class="label" style="font-size:0.75em; color:#64748b; margin-bottom:8px;">Sushi Master → Linda W.</p>
                <div style="height:6px; background:#f1f5f9; border-radius:3px; overflow:hidden;">
                    <div style="width:90%; height:100%; background:#f59e0b;"></div>
                </div>
                <p class="label" style="font-size:0.7em; margin-top:5px; color:#f59e0b; font-weight:600;">📍 Arriving (Estimated 1m)</p>
            </div>
        `;
    }

    fetchMapData(mgrMap, 'MANAGER');
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

async function fetchMapData(targetMap, role) {
    try {
        const [usersRes, restsRes] = await Promise.all([
            secureFetch(BACKEND_URL + 'users'),
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
        const restaurants = await response.json();
        
        const list = document.getElementById('restaurants-list');
        list.innerHTML = restaurants.map(rest => `
            <div class="restaurant-card" onclick="showRestaurantMenu(${rest.id}, '${rest.name}')">
                <div class="restaurant-image" style="background-image: url('https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80')"></div>
                <div class="restaurant-info">
                    <h3 class="restaurant-name">${rest.name}</h3>
                    <p class="restaurant-category">${rest.category}</p>
                    <div class="restaurant-meta">
                        <span>⭐ 4.8</span>
                        <span>$${rest.deliveryFee || '2.99'} Delivery</span>
                    </div>
                </div>
            </div>
        `).join('');

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
                <div class="restaurant-image" style="background-image: url('https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300&q=80')"></div>
                <div class="restaurant-info">
                    <h3 class="restaurant-name">${item.name}</h3>
                    <p class="restaurant-category" style="height: 40px; overflow: hidden;">${item.description || 'Delicious meal prepared with fresh ingredients.'}</p>
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 15px;">
                        <span style="font-weight: 700; color: #1e1b4b; font-size: 1.1em;">$${item.price.toFixed(2)}</span>
                        <button class="login-button" style="padding: 8px 15px; font-size: 0.8em; margin: 0;" onclick="addToCart(${item.id}, '${item.name.replace(/'/g, "\\'")}', ${item.price})">Add to Cart</button>
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

// Global exposure for onclick handlers
window.addToCart = addToCart;
window.removeFromCart = removeFromCart;
window.showRestaurantMenu = showRestaurantMenu;

document.getElementById('checkout-btn').addEventListener('click', placeOrder);

async function placeOrder() {
    if (!currentUser || cart.length === 0 || !selectedRestaurantId) return;

    const checkoutBtn = document.getElementById('checkout-btn');
    checkoutBtn.disabled = true;
    checkoutBtn.innerText = 'Processing...';

    const orderRequest = {
        restaurantId: selectedRestaurantId,
        deliveryAddress: currentUser.address || 'Standard Delivery Address',
        deliveryOptionId: 1, // Default option
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
        checkoutBtn.disabled = false;
        checkoutBtn.innerText = 'Place Order';
    }
}

async function fetchAndShowOrders() {
    try {
        let url = '/api/orders';
        if (currentUser.role === 'CUSTOMER') {
            url = `/api/orders/customer/${currentUser.id}`;
        } else if (currentUser.role === 'RESTAURANT_OWNER') {
             // We'd need to find the restaurant belonging to this owner
             // For now, this placeholder remains as simple as before
             return; 
        }

        const response = await secureFetch(url);
        if (!response.ok) return;
        const orders = await response.json();
        
        const list = document.getElementById('orders-list');
        const message = document.getElementById('orders-message');
        
        if (orders.length === 0) {
            message.style.display = 'block';
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
                switchPane('agent-content', 'nav-agent-btn');
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
    logToScreen("Window loaded. Starting logic...");
    try {
        setupSidebar();
        setupNavListeners();
        
        // Setup Auth Listeners
        const loginBtn = document.getElementById('login-btn');
        if (loginBtn) loginBtn.addEventListener('click', handleLogin);
        const regBtn = document.getElementById('register-btn');
        if (regBtn) regBtn.addEventListener('click', handleRegister);

        // Setup Manager Action Listeners
        const addRestBtn = document.getElementById('add-rest-btn');
        if (addRestBtn) addRestBtn.addEventListener('click', handleAddRestaurant);
        const addAgentBtn = document.getElementById('add-agent-btn');
        if (addAgentBtn) addAgentBtn.addEventListener('click', handleAddAgent);
        const updateMgrBtn = document.getElementById('update-manager-btn');
        if (updateMgrBtn) updateMgrBtn.addEventListener('click', handleUpdateManager);

        logToScreen("All listeners attached.");
    } catch (e) {
        logToScreen("Listener Setup Failed: " + e.message, true);
        console.error("Setup Error:", e);
    }
    startHeartbeat();
    initSession();
});
