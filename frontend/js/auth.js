// Funções de autenticação: guardam token/role/email no localStorage.

function salvarSessao(token, email, role) {
    localStorage.setItem("lojacarro_token", token);
    localStorage.setItem("lojacarro_email", email);
    localStorage.setItem("lojacarro_role", role);
}

function limparSessao() {
    localStorage.removeItem("lojacarro_token");
    localStorage.removeItem("lojacarro_email");
    localStorage.removeItem("lojacarro_role");
}

function getToken() {
    return localStorage.getItem("lojacarro_token");
}

function getEmailLogado() {
    return localStorage.getItem("lojacarro_email");
}

function getRoleLogado() {
    return localStorage.getItem("lojacarro_role");
}

function isGerente() {
    return getRoleLogado() === "GERENTE";
}

// Redireciona para a tela de login se não houver sessão ativa.
function exigirLogin() {
    if (!getToken()) {
        window.location.href = "index.html";
    }
}

function logout() {
    limparSessao();
    window.location.href = "index.html";
}

async function fazerLogin(email, password) {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(data.error || "Não foi possível entrar. Confira email e senha.");
    }

    return data;
}

async function fazerRegistro(email, password, role) {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password, role })
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(data.error || "Não foi possível registrar o usuário.");
    }

    return data;
}
