const BACKEND_URL = "/api/users/";
const RESTAURANT_URL = "/api/restaurants";
const DELIVERY_URL = "/api/delivery/";
const EMAIL_PATTERN = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;
const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

// Outer Layout Containers
const loginScene = document.getElementById('login-scene');
const registerScene = document.getElementById('register-scene');
const mainLayout = document.getElementById('main-layout');

// Inner Content Panes
const homeContent = document.getElementById('home-content');
const restaurantsContent = document.getElementById('restaurants-content');
const managerContent = document.getElementById('manager-content');
const ordersContent = document.getElementById('orders-content');
const trackingContent = document.getElementById('tracking-content');

// Nav Buttons
const navItems = document.querySelectorAll('.nav-item');
const managerNavBtn = document.getElementById('nav-manager-btn');
const ownerNavBtn = document.getElementById('nav-owner-btn');
const agentNavBtn = document.getElementById('nav-agent-btn');
const browseNavBtn = document.getElementById('nav-browse-btn');
const ordersNavBtn = document.getElementById('nav-orders-btn');
const trackingNavBtn = document.getElementById('nav-tracking-btn');
const feedbackNavBtn = document.getElementById('nav-feedback-btn');
const accountNavBtn = document.getElementById('nav-account-btn');
const homeNavBtn = document.getElementById('nav-menu-btn');

let currentUser = JSON.parse(localStorage.getItem('currentUser')) || null;
let authToken = localStorage.getItem('authToken') || null;

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
function switchOuterLayout(layout) {
    loginScene.classList.remove('active');
    registerScene.classList.remove('active');
    mainLayout.classList.remove('active');
    layout.classList.add('active');
}

// Sidebar Mobile Toggle Logic
const sidebar = document.getElementById('sidebar');
const sidebarOverlay = document.getElementById('sidebar-overlay');
const mobileMenuBtn = document.getElementById('mobile-menu-btn');

function toggleMobileSidebar(show) {
    if (show) {
        sidebar.classList.add('mobile-active');
        sidebarOverlay.classList.add('active');
    } else {
        sidebar.classList.remove('mobile-active');
        sidebarOverlay.classList.remove('active');
    }
}

if (mobileMenuBtn) {
    mobileMenuBtn.addEventListener('click', () => toggleMobileSidebar(true));
}

if (sidebarOverlay) {
    sidebarOverlay.addEventListener('click', () => toggleMobileSidebar(false));
}

// Switch between Inner Dashboard Panes
function switchPane(paneId, navBtnId) {
    // Hide all panes
    document.querySelectorAll('.content-pane').forEach(p => p.classList.remove('active'));
    // Show target pane
    const targetPane = document.getElementById(paneId);
    if (targetPane) targetPane.classList.add('active');
    
    // Update nav item active state
    navItems.forEach(item => item.classList.remove('active'));
    if (navBtnId) {
        document.getElementById(navBtnId).classList.add('active');
    }

    // Mobile: Close sidebar after selection
    toggleMobileSidebar(false);

    // Special logic for restaurants pane
    if (paneId === 'restaurants-content') {
        fetchAndShowRestaurants();
    }

    // Special logic for manager pane
    if (paneId === 'manager-content') {
        fetchAndShowAgents();
    }

    // Special logic for tracking pane
    if (paneId === 'tracking-content') {
        initMap();
    }
}

// Sidebar Navigation Listeners
document.getElementById('nav-menu-btn').addEventListener('click', () => switchPane('home-content', 'nav-menu-btn'));
document.getElementById('nav-browse-btn').addEventListener('click', () => switchPane('restaurants-content', 'nav-browse-btn'));
document.getElementById('nav-owner-btn').addEventListener('click', () => switchPane('owner-content', 'nav-owner-btn'));
document.getElementById('nav-agent-btn').addEventListener('click', () => switchPane('agent-content', 'nav-agent-btn'));
document.getElementById('nav-manager-btn').addEventListener('click', () => switchPane('manager-content', 'nav-manager-btn'));
document.getElementById('nav-orders-btn').addEventListener('click', () => switchPane('orders-content', 'nav-orders-btn'));
document.getElementById('nav-tracking-btn').addEventListener('click', () => switchPane('tracking-content', 'nav-tracking-btn'));
document.getElementById('nav-feedback-btn').addEventListener('click', () => switchPane('feedback-content', 'nav-feedback-btn'));
document.getElementById('nav-account-btn').addEventListener('click', () => switchPane('account-content', 'nav-account-btn'));

// Login/Register Links
document.getElementById('go-to-register-btn').addEventListener('click', () => {
    switchOuterLayout(registerScene);
    document.getElementById('reg-message').style.display = 'none';
});
document.getElementById('back-to-login-btn').addEventListener('click', () => {
    switchOuterLayout(loginScene);
    document.getElementById('login-message').style.display = 'none';
});

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
    switchOuterLayout(loginScene);
}

document.getElementById('sidebar-logout-btn').addEventListener('click', logout);

// Show password toggles
document.getElementById('login-show-password').addEventListener('change', (e) => {
    document.getElementById('login-password').type = e.target.checked ? 'text' : 'password';
});

document.getElementById('reg-show-password').addEventListener('change', (e) => {
    const type = e.target.checked ? 'text' : 'password';
    document.getElementById('reg-password').type = type;
    document.getElementById('reg-confirm-password').type = type;
});

function showAlert(message) {
    alert("QuickBite\n\n" + message);
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
document.getElementById('login-btn').addEventListener('click', async () => {
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!email || !password) {
        showMessage('login-message', "Both email and password are required.", false);
        return;
    }

    try {
        const response = await fetch(BACKEND_URL + 'login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        
        if (response.ok) {
            const data = await response.json();
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
                    switchPane('manager-content', 'nav-manager-btn');
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
            const error = await response.json();
            showMessage('login-message', error.message || "Invalid credentials. Please try again.", false);
        }
    } catch (e) {
        showMessage('login-message', "Network error. Make sure backend is running.", false);
    }
});

// Logic for Register
document.getElementById('register-btn').addEventListener('click', async () => {
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

    try {
        const response = await fetch(BACKEND_URL + 'register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role })
        });

        if (response.ok) {
            const data = await response.json();
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
                    switchPane('manager-content', 'nav-manager-btn');
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
        } else {
            const error = await response.json();
            showMessage('reg-message', error.message || "Registration failed.", false);
        }
    } catch (e) {
        showMessage('reg-message', "Network error. Make sure backend is running.", false);
    }
});

// Manager Action: Add Restaurant
document.getElementById('add-rest-btn').addEventListener('click', async () => {
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
});

// Manager Action: Add Delivery Agent
document.getElementById('add-agent-btn').addEventListener('click', async () => {
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
});

// Manager Action: Update Own Account
document.getElementById('update-manager-btn').addEventListener('click', async () => {
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
});

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

function updateNavigationForRole(role) {
    // Hide everything first
    navItems.forEach(btn => btn.style.display = 'none');
    
    // Always show Logout (it's separate, but let's be sure)
    homeNavBtn.style.display = 'flex'; // Home is common for all
    
    if (role === 'CUSTOMER') {
        browseNavBtn.style.display = 'flex';
        ordersNavBtn.style.display = 'flex';
        trackingNavBtn.style.display = 'flex';
        feedbackNavBtn.style.display = 'flex';
        accountNavBtn.style.display = 'flex';
    } else if (role === 'MANAGER' || role === 'ADMIN') {
        managerNavBtn.style.display = 'flex';
        trackingNavBtn.style.display = 'flex';
    } else if (role === 'RESTAURANT_OWNER') {
        ownerNavBtn.style.display = 'flex';
        ordersNavBtn.style.display = 'flex';
    } else if (role === 'DELIVERY_AGENT') {
        agentNavBtn.style.display = 'flex';
        trackingNavBtn.style.display = 'flex';
    }
}

// Map Simulation
function initMap() {
    console.log("Initializing map placeholder...");
    const mapDiv = document.getElementById('map');
    if (mapDiv) {
        mapDiv.innerHTML = `
            <div style="text-align: center;">
                <p class="label" style="color: #6366f1; font-weight: 700;">📍 Tracking Active Orders...</p>
                <div style="margin-top: 10px; font-size: 0.8em; color: #64748b;">(Real-time simulation active)</div>
            </div>
        `;
    }
}
// Session Initialization
function initSession() {
    console.log("Checking session...", { authToken: !!authToken, currentUser: !!currentUser });
    if (authToken && currentUser) {
        updateNavigationForRole(currentUser.role);
        switchOuterLayout(mainLayout);
        
        // Land on default pane based on role
        if (currentUser.role === 'MANAGER' || currentUser.role === 'ADMIN') {
            switchPane('manager-content', 'nav-manager-btn');
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
        switchOuterLayout(loginScene);
    }
}

// Initial Call
initSession();
