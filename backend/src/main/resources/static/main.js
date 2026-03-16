const BACKEND_URL = "/api/users/";
const EMAIL_PATTERN = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;
const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

const loginScene = document.getElementById('login-scene');
const registerScene = document.getElementById('register-scene');
const menuScene = document.getElementById('menu-scene');

function switchScene(scene) {
    loginScene.classList.remove('active');
    registerScene.classList.remove('active');
    menuScene.classList.remove('active');
    scene.classList.add('active');
}

document.getElementById('go-to-register-btn').addEventListener('click', () => switchScene(registerScene));
document.getElementById('back-to-login-btn').addEventListener('click', () => switchScene(loginScene));
document.getElementById('logout-btn').addEventListener('click', () => {
    // Basic logout handling
    document.getElementById('login-email').value = '';
    document.getElementById('login-password').value = '';
    document.getElementById('login-message').style.display = 'none';
    switchScene(loginScene);
});

// Show password toggles
document.getElementById('login-show-password').addEventListener('change', (e) => {
    document.getElementById('login-password').type = e.target.checked ? 'text' : 'password';
});

document.getElementById('reg-show-password').addEventListener('change', (e) => {
    const type = e.target.checked ? 'text' : 'password';
    document.getElementById('reg-password').type = type;
    document.getElementById('reg-confirm-password').type = type;
});

// Menu buttons mock actions
function showAlert(message) {
    alert("QuickBite\n\n" + message);
}

document.getElementById('browse-btn').addEventListener('click', () => showAlert('Browsing restaurants...'));
document.getElementById('order-btn').addEventListener('click', () => showAlert('Placing order...'));
document.getElementById('track-btn').addEventListener('click', () => showAlert('Tracking delivery...'));

function showMessage(elementId, text, isSuccess) {
    const el = document.getElementById(elementId);
    el.textContent = text;
    el.style.display = 'block';
    
    if (isSuccess) {
        el.style.color = '#38a169'; // Green text
        el.style.backgroundColor = 'rgba(56, 161, 105, 0.1)';
        el.style.borderColor = 'rgba(56, 161, 105, 0.2)';
    } else {
        el.style.color = '#e53e3e'; // Red text
        el.style.backgroundColor = 'rgba(229, 62, 62, 0.1)';
        el.style.borderColor = 'rgba(229, 62, 62, 0.2)';
    }
}

// Logic for Login
document.getElementById('login-btn').addEventListener('click', async () => {
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!EMAIL_PATTERN.test(email)) {
        showMessage('login-message', "Invalid entry: Please enter a valid email.", false);
        return;
    }
    if (!PASSWORD_PATTERN.test(password)) {
        showMessage('login-message', "Invalid entry: Password must be at least 8 characters with upper, lower, and digit.", false);
        return;
    }

    try {
        const response = await fetch(BACKEND_URL + 'login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });
        
        if (response.ok) {
            const data = await response.text();
            if (data && data !== "null") {
                showMessage('login-message', "Login successful!", true);
                setTimeout(() => switchScene(menuScene), 500);
            } else {
                showMessage('login-message', "Login failed. Check credentials.", false);
            }
        } else {
            showMessage('login-message', "Server error. Try again later.", false);
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

    if (!name || !EMAIL_PATTERN.test(email) || !PASSWORD_PATTERN.test(password) || password !== confirm) {
        showMessage('reg-message', "Invalid entry: Check all fields and password match.", false);
        return;
    }

    try {
        const response = await fetch(BACKEND_URL + 'register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name, email, password, role })
        });

        if (response.ok) {
            showMessage('reg-message', "Registration successful! Please login.", true);
            setTimeout(() => switchScene(loginScene), 1500);
        } else {
            showMessage('reg-message', "Registration failed. Email may already exist.", false);
        }
    } catch (e) {
        showMessage('reg-message', "Network error. Make sure backend is running.", false);
    }
});
